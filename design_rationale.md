# Design Rationale

This document explains and justifies the architectural and design decisions made in our implementation of Ticket to Ride: London. Each decision is grounded in object-oriented design principles and supported by the implemented codebase.

---

## 1. Classes

### 1.1 Player

**Role:** The `Player` class represents a participant in the game, encapsulating their transportation cards, destination tickets, bus count, and score.

**Why this class was needed:** A player in Ticket to Ride: London is a central domain concept that owns significant state and behaviour. The player holds a private hand of cards, maintains their bus inventory (starting at 17), accumulates destination tickets, and tracks their score. These responsibilities are tightly coupled—the information required to determine affordability, spend cards, and manage buses is all localised within the player.

**Encapsulated responsibility:** The `Player` class owns the responsibility of determining whether a proposed card payment can be afforded (`canAfford(CardPayment)`) and executing that payment (`spendCards(CardPayment)`). This design follows the Information Expert principle: the player has the hand, so the player should answer questions about affordability.

**Why not a method elsewhere:** If affordability logic lived in a service or controller, that external class would need to reach into the player's hand, inspect card counts, and validate the payment. This would constitute Feature Envy—the service would be more interested in the player's data than the player itself. Instead, external code simply asks `player.canAfford(payment)` and calls `player.spendCards(payment)`, respecting encapsulation. The `hand()` accessor returns `Collections.unmodifiableList(hand)`, ensuring external code cannot mutate the player's internal state.

**Support for maintainability:** If the rules for affordability change (for example, introducing card limits or special abilities), only the `Player` class needs modification. The rich domain model avoids the anaemic domain antipattern, where domain objects are mere data containers and all logic resides in bloated service classes.

### 1.2 Route

**Role:** The `Route` class represents a printed connection between two board locations, including its claim state, length, colour, and payment requirement.

**Why this class was needed:** A route is a first-class domain entity with identity and mutable state. Routes are claimed during the game, and the act of claiming involves validating complex conditions: the route must be unclaimed, the player must have sufficient buses, the card payment must satisfy the route's requirement, and double-route restrictions must be respected.

**Encapsulated responsibility:** The `Route` class owns the `canBeClaimed(...)` method, which validates all claim prerequisites. It also exposes `claim(playerId)` to transition the route into a claimed state. This design ensures that claim logic is not scattered across controllers or services.

**Why not a method elsewhere:** Placing claim validation in an external service would require that service to query the route's state, the player's buses, and other routes in the same double-group—all of which constitutes distributed knowledge. By placing `canBeClaimed(...)` on `Route`, we centralise this logic where the route's own data (length, colour, requirement, claim status, doubleGroupId) is directly available. The method signature accepts contextual information (player count, related routes, payment, buses remaining) that the route cannot know on its own, which represents a well-designed collaboration boundary.

**Support for extensibility:** The route delegates payment validation to a `RouteRequirement` strategy. This composition allows coloured routes, grey routes, and Sprint 3 ferry routes to have different validation rules without type-checking conditionals. If new route types are introduced (for example, tunnel routes in other Ticket to Ride editions), a new `RouteRequirement` implementation can be injected without modifying the route-claim command flow.

### 1.3 TurnManager

**Role:** The `TurnManager` class owns player turn order, tracks the current player, and manages final-round state.

**Why this class was needed:** Turn progression in Ticket to Ride: London involves multiple concerns: maintaining clockwise player order, identifying the current player, triggering the final round when a player finishes with 0–2 buses remaining, and counting down final turns until the game proceeds to scoring. These concerns are cohesive—they all relate to turn sequencing—but distinct from scoring, card drawing, or route claiming.

**Encapsulated responsibility:** The `TurnManager` encapsulates final-round trigger logic in `endTurn(int busesRemaining)`. When the current player's buses drop to two or fewer, the manager transitions into final-round mode and initialises a countdown equal to the number of players. This logic is entirely owned by `TurnManager`, not the `Game` aggregate or a controller.

**Why not a method elsewhere:** If turn management were embedded in `Game`, the `Game` class would risk becoming a God Class—simultaneously coordinating players, board, decks, turns, and phases. By extracting turn sequencing into `TurnManager`, we achieve better cohesion: the `Game` aggregate delegates turn decisions to a focused collaborator. Similarly, placing turn logic in the application layer would violate the principle that domain rules should live in domain objects.

**Support for testability:** The `TurnManager` can be unit tested in isolation, verifying correct turn advancement and final-round behaviour without constructing an entire game. This separation supports faster, more focused tests.

---

## 2. Relationships

### 2.1 Route → RouteRequirement (Composition via Strategy)

**Classes involved:** `Route`, `RouteRequirement` (interface), `ColouredRouteRequirement`, `GreyRouteRequirement`, `FerryRouteRequirement`.

**Relationship type:** Composition with dependency injection. Each `Route` holds exactly one `RouteRequirement` instance, injected through its constructor.

**Why composition is appropriate:** The relationship is "has-a" rather than "is-a"—a route *has* a requirement, not *is* a kind of requirement. The requirement strategy is a behavioural component that determines whether a card payment satisfies the route. Different routes may share the same requirement type (for example, all 2-length blue routes use equivalent coloured requirements), but each route instance holds its own requirement reference.

**Why not inheritance:** An alternative design might create `ColouredRoute`, `GreyRoute`, and `FerryRoute` subclasses. However, the only behavioural difference is payment validation—the routes share all other behaviour (claiming, endpoint management, double-group tracking). Using inheritance for this single point of variation would violate the principle of favouring composition over inheritance. Subclassing would also complicate factory creation and reduce flexibility if new requirement types were introduced.

**How this supports the architecture:** Polymorphism eliminates type-checking conditionals. The `Route.canBeClaimed(...)` method calls `requirement.isSatisfiedBy(payment)` without knowing which concrete requirement type it holds. This adheres to the Open/Closed Principle: the system is open for extension (new requirement types) but closed for modification (no changes to `Route`).

### 2.2 Game → Board, Players, TurnManager (Composition as Aggregate Root)

**Classes involved:** `Game`, `Board`, `Player`, `TurnManager`, `TransportCardDeck`, `FaceUpDisplay`, `DestinationTicketDeck`.

**Relationship type:** Composition. The `Game` class is the aggregate root that composes all major domain components.

**Why composition is appropriate:** A game session owns its board, players, decks, and turn manager. These components have no meaningful existence outside a game—they are created together and destroyed together. Composition accurately models this lifecycle dependency.

**Why the Game is not a God Class:** Although `Game` holds references to many collaborators, it delegates behaviour to them rather than implementing everything itself. Scoring is handled by `ScoreCalculator` (injected into the application layer). Turn advancement is handled by `TurnManager`. Card logic is handled by `TransportCardDeck` and `FaceUpDisplay`. The `Game` class coordinates access to its components but does not contain rule-heavy methods.

**How this supports separation of concerns:** The aggregate root pattern establishes a clear boundary. External code (such as commands and the application service) accesses game state through the `Game` object, which ensures consistent state access and simplifies reasoning about invariants.

### 2.3 GameApplicationService → GameStateListener (Observer)

**Classes involved:** `GameApplicationService`, `GameStateListener` (interface), Swing panels (`MainFrame`, `BoardPanel`, etc.).

**Relationship type:** Observer association. The service maintains a list of listeners and publishes immutable `GameSnapshot` objects after each successful command.

**Why Observer is appropriate:** The domain and application layers must not depend on the UI layer. If the application service called `boardPanel.repaint()` directly, we would introduce a forbidden upward dependency and tightly couple domain operations to Swing. The Observer pattern inverts this dependency: Swing panels register themselves as listeners and receive snapshots reactively.

**Why not direct method calls:** Directly invoking UI methods from the domain would make the game logic untestable without Swing. With Observer, all domain and application tests run without rendering a window. The listeners receive immutable snapshots, ensuring the UI cannot accidentally mutate game state.

**How this supports extensibility:** Additional observers can be added without modifying the service—for example, a logging observer, a network synchronisation observer, or an AI player observer. This adheres to the Open/Closed Principle.

---

## 3. Inheritance

### 3.1 Where Inheritance Is Used

Inheritance appears in two controlled locations:

1. **RouteRequirement hierarchy:** `ColouredRouteRequirement`, `GreyRouteRequirement`, and `FerryRouteRequirement` implement the `RouteRequirement` interface. This represents a classic Strategy pattern where the interface defines a contract (`isSatisfiedBy(CardPayment)`) and concrete implementations provide variant behaviour. Inheritance here captures a true type hierarchy: all are kinds of route requirements.

2. **GameCommand hierarchy:** `ClaimRouteCommand`, `DrawTransportCardCommand`, and `DrawDestinationTicketsCommand` implement the `GameCommand` interface. This represents the Command pattern where each command encapsulates a player intention with its required data and execution logic.

### 3.2 Why Inheritance Is Appropriate in These Cases

In both cases, inheritance supports polymorphism over type-checking. The `Route` class calls `requirement.isSatisfiedBy(payment)` without `if/else` branches on requirement type. The `GameApplicationService` calls `command.execute(game)` without switching on command type. This design follows the principle that polymorphism should replace conditionals.

The shared abstraction is a behavioural contract. `RouteRequirement` defines what it means to validate a payment; `GameCommand` defines what it means to execute an action against a game. Concrete implementations supply the "how."

### 3.3 Where Inheritance Was Deliberately Avoided

We deliberately avoided subclassing `Route` into `ColouredRoute`, `GreyRoute`, or `FerryRoute`. The only difference between these printed routes is payment validation logic, which is cleanly handled by the injected `RouteRequirement` strategy. Creating route subclasses would introduce unnecessary class proliferation for a single point of variation.

Similarly, we avoided subclassing `Player` for different player types. All players share identical behaviour; differences (such as player colour) are represented as data fields, not subclasses.

We also avoided creating a `BusCard` subclass of a hypothetical `TransportationCard` class. Instead, all cards are represented as `CardColor` enum values, with `BUS` being a distinguished constant. The bus-specific rules (such as face-up Bus card limits) are enforced procedurally in the application layer and `FaceUpDisplay`. This simplification avoids a class hierarchy where only one enum value would require special behaviour.

### 3.4 Why Composition Was Preferred

Composition provides flexibility that inheritance cannot match. By injecting a `RouteRequirement` into a `Route`, we can change validation behaviour without altering the route's class hierarchy. By injecting a `ShuffleStrategy` into `TransportCardDeck`, we can substitute deterministic shuffling for tests without modifying the deck class.

This aligns with the principle "favour composition over inheritance" emphasised in the course materials. Inheritance creates rigid, compile-time hierarchies; composition creates flexible, runtime-configurable collaborations.

---

## 4. Cardinalities

### 4.1 Game → Player: 1 to 2..4

**Relationship:** A `Game` contains between two and four `Player` instances.

**Why this cardinality is correct:** The official Ticket to Ride: London rules define the game for two to four players. The `GameFactory` enforces this constraint by rejecting player lists outside this range. The lower bound of 2 reflects that the game cannot be played solo. The upper bound of 4 reflects the component limits (17 buses per player × 4 players = 68 buses total; the game provides exactly this many).

**Why not exactly 4:** Supporting 2–4 players provides flexibility without compromising the rules. The double-route restrictions already parameterise by player count (in 2-player games, only one route of a double pair may be claimed), demonstrating that the rules explicitly accommodate variable player counts.

**Why not 1..*:** A single-player game would be degenerate—there is no competition for routes, no race to trigger the end-game, and no meaningful longest-path comparison. The rules do not support solo play.

### 4.2 Player → DestinationTicket: 1 to 1..*

**Relationship:** Each `Player` holds one or more `DestinationTicket` instances.

**Why the lower bound is 1:** During setup, each player draws two destination tickets and must keep at least one. During the game, a player may draw additional tickets (draw two, keep at least one). Since tickets can never be discarded or lost, the player's ticket count is monotonically non-decreasing from at least one. The rules guarantee that every player holds at least one ticket at all times after setup.

**Why the upper bound is unbounded (*):** There is no rule limiting how many tickets a player may accumulate. A player could theoretically draw tickets on multiple turns, keeping all of them each time. The 20-ticket deck is finite, but the per-player maximum is not explicitly capped.

**Why not 0..*:** A zero lower bound would incorrectly imply that a player could exist with no tickets. This violates the rule that at least one ticket must be kept during setup and during any subsequent ticket draw action. Our implementation enforces this: `DrawDestinationTicketsCommand` requires at least one kept ticket, and `GameFactory` ensures each player receives tickets during setup.

### 4.3 Board -> Location and Route: 1 to 17 and 1 to 43

**Relationship:** The London `Board` contains exactly 17 `Location` instances and 43 printed
`Route` instances.

**Why this cardinality is correct:** These values come from the authoritative London board data.
The implementation keeps those counts in `BoardFactory`, not in Swing rendering code. That means the
board topology used by route claiming, ticket completion, and longest-path scoring is the same
topology drawn by the UI.

**Why not store coordinates in `Location`:** Screen coordinates are not game rules. They belong in
the view model and renderer so map layout changes do not affect path-finding, scoring, or command
validation.

### 4.4 Route -> Location and RouteRequirement: 1 to 2 and 1 to 1

**Relationship:** Each `Route` connects exactly two `Location` endpoints and owns exactly one
`RouteRequirement`.

**Why this cardinality is correct:** A printed route in Ticket to Ride is always an edge between two
locations. Claim validation always has exactly one payment rule: coloured, grey, or Ferry. A route
with zero requirements would be under-specified; a route with multiple competing requirements would
make validation ambiguous.

**Design consequence:** New route behaviours are added by introducing another requirement strategy,
not by changing the `Route` cardinality or subclassing route entities.

### 4.5 GameApplicationService -> GameStateListener: 1 to 0..*

**Relationship:** One `GameApplicationService` may notify zero or more `GameStateListener`
observers.

**Why this cardinality is correct:** The game must remain testable without Swing, so zero listeners
is valid in automated tests. At runtime, multiple UI panels observe the same immutable
`GameSnapshot` so the board, card market, player panel, status panel, and Rush Hour panel refresh
from one source of truth.

### 4.6 UndoHistory -> GameMemento: 1 to 0..2

**Relationship:** One `UndoHistory` stores zero, one, or two `GameMemento` entries.

**Why this cardinality is correct:** Sprint 3 requires undo for the last two completed turns. A
bounded collection prevents unbounded memory growth and makes the feature's rule boundary explicit.
The absence of mementos is also valid at the start of a game or immediately after failed commands.

---

## 5. CRC Cards

The CRC view below complements the UML class diagram by showing each major class's responsibility
and collaborators. It is intentionally focused on architectural classes rather than every DTO or
Swing helper.

| Class | Responsibilities | Collaborators |
|---|---|---|
| `Game` | Act as aggregate root; expose board, players, decks, turn state, phase, and Rush Hour state; provide player lookup and action eligibility | `Board`, `Player`, `TransportCardDeck`, `FaceUpDisplay`, `DestinationTicketDeck`, `TurnManager`, `RushHourManager` |
| `Player` | Own hand, tickets, buses, and score; answer affordability; spend cards; use buses; receive route points | `CardPayment`, `DestinationTicket` |
| `Route` | Represent a claimable board edge; enforce unclaimed state, bus count, payment requirement, and double-route restrictions; record and restore claims | `Location`, `RouteRequirement`, `CardPayment`, related double-group `Route` objects |
| `RouteRequirement` implementations | Validate coloured, grey, and Ferry payments polymorphically | `CardPayment`, `RouteColor` |
| `Board` | Own route/location lookup and double-route grouping queries | `Location`, `Route` |
| `TurnManager` | Own current-player sequencing, final-round trigger, and final-turn countdown | Player ids supplied by `Game` |
| `TransportCardDeck` | Own draw/discard piles, draw availability, reshuffle, and discard handling | `ShuffleStrategy`, `CardColor` |
| `FaceUpDisplay` | Own five visible card slots, refill behaviour, face-up Bus checks, and three-Bus flush rule | `TransportCardDeck`, `CardColor` |
| `DestinationTicketDeck` | Own ticket draw pile and bottom-return semantics | `DestinationTicket` |
| `ScoreCalculator` | Calculate final route, ticket, longest-path, and winner results | `RouteScoreTable`, `TicketCompletionChecker`, `LongestPathCalculator`, `Board`, `Player` |
| `RushHourManager` | Own Rush Hour event lifecycle, forecast/peak phase, affected event ids, and bonus-point tracking | `RushHourEvent`, `RushHourPhase`, `ShuffleStrategy` |
| `RushHourClaimRule` | Translate active Rush Hour state into detour-card and bonus-point claim rules | `RushHourManager`, `Route`, `CardPayment` |
| `GameCommand` implementations | Encapsulate player actions and validate/execute them against the aggregate | `Game`, `CommandResult`, `RouteScoreTable`, `RushHourClaimRule` |
| `GameApplicationService` | Provide the application entry point; execute commands; manage listeners; publish snapshots; coordinate scoring and undo | `Game`, `GameCommand`, `GameStateListener`, `ScoreCalculator`, `TransportDrawCoordinator`, `UndoHistory`, `GameMementoFactory` |
| `TransportDrawCoordinator` | Own two-card transportation draw progress and second-draw Bus restrictions | `Game`, `GameMementoFactory`, `UndoHistory`, `DrawTransportCardCommand` |
| `GameMementoFactory` and `UndoHistory` | Capture, store, and restore bounded completed-turn snapshots | `GameMemento`, `Game`, `TransportDrawProgress` |
| `GameFactory`, `BoardFactory`, `DeckFactory`, `RushHourEventFactory` | Centralise construction of London board data, decks, game setup, and Rush Hour event data | Domain constructors, `ShuffleStrategy` |
| Swing panels | Render immutable snapshots and submit user intent as commands; never enforce game rules | `GameApplicationService`, `GameStateListener`, `GameSnapshot`, view models |

---

## 6. Design Patterns

### 6.1 Command Pattern

**Where applied:** The `GameCommand` interface with three implementations: `ClaimRouteCommand`, `DrawTransportCardCommand`, and `DrawDestinationTicketsCommand`.

**Why appropriate:** Player actions need validation, execution, and result reporting. Embedding this logic in Swing event listeners would tightly couple UI code to game rules, making the system untestable and brittle. The Command pattern encapsulates each action as a first-class object with its required data (player ID, route ID, payment) and an `execute(Game)` method.

**Problem solved:** Commands decouple player intent from execution context. The UI creates a command and submits it to the application service; the command validates and executes against the game aggregate. This enables testing without Swing, supports future AI players (which generate commands programmatically), and provides a clear audit trail of actions.

**How it improves maintainability:** Each command is a focused class with a single responsibility. Adding a new action type (for example, a "pass" action for rule variants) requires only a new command class without modifying existing commands or the application service.

### 6.2 Strategy Pattern

**Where applied:** Three strategy-style hierarchies exist in the codebase.

1. **RouteRequirement:** `ColouredRouteRequirement`, `GreyRouteRequirement`, and `FerryRouteRequirement` implement the `RouteRequirement` interface. Each route holds a requirement strategy that determines whether a card payment is acceptable.

2. **ShuffleStrategy:** `RandomShuffleStrategy` and `FixedOrderShuffleStrategy` implement the `ShuffleStrategy` interface. Decks accept a shuffle strategy through constructor injection.

3. **RouteSelector:** Rush Hour events use route selectors to decide which routes are affected by a
forecast or peak event.

**Why appropriate:** Route validation differs between coloured routes (must use cards matching the printed colour), grey routes (may use any single colour set), and ferry routes (must satisfy printed Bus symbols before normal route spaces). Without Strategy, the `Route` class or claim command would contain conditionals for every route variant. Similarly, randomness must be controllable for testing; injecting a fixed-order strategy ensures deterministic test behaviour. Rush Hour selectors keep event targeting data-driven instead of embedding event-specific conditionals in route-claim code.

**Problem solved:** Strategy eliminates type-checking conditionals and enables behavioural variation through composition. The `Route` class calls `requirement.isSatisfiedBy(payment)` polymorphically, and the deck calls `shuffleStrategy.shuffle(cards)` without knowing which concrete strategy it holds.

**How it improves testability:** Test code injects `FixedOrderShuffleStrategy` to produce deterministic card sequences, enabling precise verification of draw behaviour without randomness.

### 6.3 Observer Pattern

**Where applied:** The `GameStateListener` interface and its implementation by Swing panels. The `GameApplicationService` maintains a list of listeners and calls `onGameStateChanged(GameSnapshot)` after each successful command.

**Why appropriate:** The domain and application layers must not import `javax.swing`. However, multiple UI components (board panel, player panel, card market panel, status panel) need to refresh when game state changes. Observer inverts the dependency: the UI registers as a listener and receives updates reactively.

**Problem solved:** Observer decouples state publication from state consumption. The application service publishes an immutable snapshot without knowing how many listeners exist or what they do with the data. This enables UI updates, logging, network synchronisation, and testing observers to coexist without modification to the publisher.

**How it improves separation of concerns:** The domain layer contains zero UI references. If the entire `ui` package were deleted, all domain and application tests would still compile and pass—a key architectural invariant verified by our test suite.

### 6.4 Factory Pattern

**Where applied:** `GameFactory`, `BoardFactory`, `DeckFactory`, and `RushHourEventFactory` centralise game setup.

**Why appropriate:** Creating a new game involves constructing the board (17 locations, 43 routes), populating decks (44 transportation cards, 20 destination tickets), dealing starting cards and tickets to players, and initialising the face-up display with flush-rule enforcement. This setup logic is complex and London-specific.

**Problem solved:** Factory classes isolate data loading and setup orchestration from domain classes. The `Route` class does not know it lives on a London board; the `TransportCardDeck` class does not know the deck contains 36 coloured cards and 8 bus cards. Factories inject this London-specific configuration, keeping domain classes generic.

**How it supports extensibility:** For Sprint 3 extensibility to other board configurations, only the factory classes need modification. A `NewYorkBoardFactory` could create different locations and routes without touching `Board`, `Route`, or `Location`. This aligns with the Open/Closed Principle.

### 6.5 Memento Pattern

**Where applied:** `GameMemento`, `GameMementoFactory`, and `UndoHistory` support the Sprint 3 Undo extension.

**Why appropriate:** Undo must restore a whole completed turn, including player resources, route claims, deck order, face-up cards, destination-ticket order, phase, and final-round counters. Reversing each command manually would be fragile because transportation draws can trigger reshuffles and face-up Bus flushes.

**Problem solved:** The application service captures an immutable memento before a turn-ending action and stores at most two entries. `undoLastTurn()` restores the most recent memento and publishes a normal `GameSnapshot`, so Swing remains a thin client.

**How it improves reliability:** The exact pre-turn state is restored instead of reconstructed through inverse side effects. Future extensions can join undo by adding their mutable state to the memento.

### 6.6 Pattern Cross-Check and Rejected Patterns

| Pattern | Status | ADR evidence | Reasoning |
|---|---|---|---|
| Command | Applied | ADR-003 | Appropriate because player actions are explicit, testable objects with validation and result reporting |
| Strategy | Applied | ADR-005, ADR-013, ADR-015 | Appropriate for route payment variation, deterministic shuffling, and Rush Hour route selection |
| Observer | Applied | ADR-006 | Appropriate because multiple Swing panels must refresh without domain/application depending on Swing |
| Factory | Applied | ADR-009, ADR-015 | Appropriate because London setup data and event data are construction concerns, not domain-rule concerns |
| Memento | Applied | ADR-014 | Appropriate because undo needs exact state restoration across decks, routes, players, turns, and draw progress |
| Singleton | Rejected | ADR-008 | Rejected for game state and services because hidden global state would harm tests and multi-game setup |
| Route subclass hierarchy | Rejected | ADR-005, ADR-013 | Rejected because route type variation is payment behaviour only; composition via `RouteRequirement` is simpler |
| Full persistence for undo | Rejected | ADR-014 | Rejected because Sprint 3 undo is in-memory and turn-limited; serialisation would add unnecessary infrastructure |

---

## 7. Scoring Rule Decision

### 7.1 Longest Continuous Path over London District Bonus

**Decision:** Final scoring uses route points, destination ticket scores, and the +10 Longest Continuous Path bonus. The implementation does not award Ticket to Ride: London's printed district bonus.

**Why this decision was needed:** The project rule baseline explicitly selects the +10 longest-path bonus to strengthen graph traversal and scoring design. It also avoids mixing two different final bonus systems.

**Alternative considered:** Implement London district bonus scoring by checking whether a player connects every location in a district and awarding the district value. This was rejected for the current baseline because it conflicts with the chosen scoring requirement and would introduce a second endgame bonus.

**Consequences:** `Location.district()` remains in the model and view model because the district circles are visible on the board and useful for future variants. Scoring remains cohesive in `ScoreCalculator`, `TicketCompletionChecker`, and `LongestPathCalculator`, with no district scoring in Swing.

---

## Summary

Our architecture is grounded in object-oriented design principles taught in the course. We created rich domain classes (`Player`, `Route`, `TurnManager`) that own meaningful behaviour rather than serving as anaemic data containers. We applied composition over inheritance, using Strategy for behavioural variation and Command for action encapsulation. We enforced separation of concerns through a four-layer architecture where domain logic is entirely independent of Swing. We documented and justified each pattern application, ensuring no decorative pattern usage. The result is a maintainable, extensible, and testable implementation of Ticket to Ride: London.
