# Ticket to Ride: London — Architecture & Design Guide

> **Purpose:** Guides AI coding agents and developers implementing the Ticket to Ride: London board game for a Software Engineering: Architecture and Design assignment. The priority is clean, maintainable, testable, industry-grade architecture — not just working code.

---

## 1. Core Architectural Principle

> **The game rules must be independent from the UI. Game rules must live outside Swing UI.**

The system is a **domain-driven, layered application**. The core game must be playable and testable without Java Swing. Swing only displays game state and sends user intentions to the domain/application layer.

Do not implement the game as one large `GameManager` or `GameController`. Each class must represent a meaningful game concept and own the behaviour that naturally belongs to it.

---

## 2. Non-Negotiable Rules

1. **No game rules in Swing classes.** Panels, listeners, and renderers must not validate routes, deal cards, update scores, or determine winners. They call application/controller methods only.

2. **No God Classes.** No massive `GameManager`, `GameController`, or `BoardPanel` that knows and does everything. Controllers coordinate use cases but delegate domain decisions to domain objects.

3. **Rich domain model.** Domain classes must not be empty data containers. `Player` manages its hand and buses through meaningful methods. `Route` validates whether a claim is legal. `TurnManager` controls turn progression.

4. **Constructor dependency injection.** Pass dependencies explicitly through constructors. No hidden globals or unnecessary Singletons.

5. **Polymorphism over type-checking.** Avoid `if/else`/`switch` based on object type. Use strategies, commands, or value objects.

6. **Separate commands and queries.** Command methods change state (`claimRoute`, `drawCards`). Query methods return data without mutation (`getCurrentPlayer`, `canClaimRoute`).

7. **Use immutable value objects.** `CardColor` enum, `PlayerColor` enum, `RouteColor` enum, `DestinationTicket` (immutable once created), `CardPayment`, `RouteScoreTable`. Never expose mutable internal collections directly — return `Collections.unmodifiableList(...)`.

8. **Data-driven game data.** Board locations, routes, destination tickets, and scoring tables must not be scattered across UI code. Use factories to centralise data loading. `london_board_layout.md` and `destination_cards.md` are the authoritative data sources.

9. **Testable without GUI.** Core rules must be testable without rendering a Swing window. Inject randomness via `ShuffleStrategy` so tests use deterministic ordering.

10. **Google Java Style Guide.** All code must conform to [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Javadoc on all classes and public methods.

11. **Document design decisions.** Create ADRs for each design pattern used, explaining why it solves a real problem. Do not add patterns decoratively.

---

## 3. Layered Architecture

```text
src/main/java/
└── ttrlondon/
    ├── Main.java
    ├── domain/          ← game rules, state, invariants (NO Swing imports)
    │   ├── board/       ← Board, Location, Route, RouteRequirement
    │   ├── card/        ← CardColor, CardPayment, TransportCardDeck, FaceUpDisplay
    │   ├── player/      ← Player, PlayerColor
    │   ├── scoring/     ← ScoreCalculator, RouteScoreTable, TicketCompletionChecker, LongestPathCalculator
    │   ├── ticket/      ← DestinationTicket, DestinationTicketDeck
    │   ├── turn/        ← TurnManager
    │   ├── random/      ← ShuffleStrategy port used by card/event rules
    │   └── game/        ← Game, GamePhase
    ├── application/     ← use case coordination
    │   ├── commands/    ← GameCommand, ClaimRouteCommand, DrawTransportCardCommand, DrawDestinationTicketsCommand
    │   ├── service/     ← GameApplicationService, TransportDrawCoordinator
    │   └── dto/         ← GameSnapshot, PlayerSnapshot, CommandResult
    ├── infrastructure/  ← factories, randomness, configuration
    │   ├── config/      ← GameFactory, BoardFactory, DeckFactory
    │   └── random/      ← RandomShuffleStrategy, FixedOrderShuffleStrategy
    └── ui/              ← rendering and input only
        ├── swing/       ← MainFrame, BoardPanel, PlayerPanel, CardMarketPanel, etc.
        ├── rendering/   ← BoardRenderer, RouteRenderer, LocationRenderer
        └── viewmodel/   ← BoardViewModel, GameViewModel
```

### Layer Rules

| Layer | Responsibility | Must Not |
|---|---|---|
| `domain` | Game rules, state, invariants | Import `javax.swing` or any UI framework |
| `application` | Coordinate use cases, translate UI intentions into domain operations | Draw UI or contain rendering logic |
| `infrastructure` | Load board data, provide randomisation | Contain core game rules |
| `ui` | Display board/cards/players, capture user input | Calculate rules, modify domain state directly, or award points |

**Acid test:** If you delete the entire `ui` package, all domain and application tests must still compile and pass.

---

## 4. Domain Model — Key Responsibilities

Names can be adjusted but responsibilities must remain clearly separated.

### Enums and Value Objects

| Type | Values | Purpose |
|---|---|---|
| `CardColor` | BLUE, GREEN, BLACK, PINK, YELLOW, ORANGE, BUS | Card types. BUS is the wild card. |
| `RouteColor` | BLUE, GREEN, BLACK, PINK, YELLOW, ORANGE, GREY | Route colours. GREY = any colour set. |
| `PlayerColor` | RED, WHITE, BLUE, YELLOW | Player bus colours (distinct from card colours). |
| `GamePhase` | SETUP, RUNNING, FINAL_ROUND, SCORING, GAME_OVER | Game lifecycle states. |

### Core Classes

| Class | Knows | Does | Key Design Rules |
|---|---|---|---|
| `Game` | Board, players, decks, turn manager, phase | Coordinate access to game components (aggregate root) | Must not become a God Class. Delegates scoring to `ScoreCalculator`, turns to `TurnManager`, card logic to deck classes. |
| `Board` | Locations, routes, connections | Find routes, query claimed routes | Must not know Swing coordinates. UI positions belong in `BoardViewModel`. |
| `Location` | ID, display name, district | Identify a place on the map (immutable) | No `x`/`y` screen coordinates. District stored for Sprint 3 extensibility. |
| `Route` | Endpoints, length, colour, kind, ferry Bus-symbol count, requirement, claim status, double group ID | Validate whether a claim is legal, record claims | `claimedBy` has no public setter — use `route.claim(playerId)`. `canBeClaimed` enforces: unclaimed, enough buses, payment satisfies requirement, double-route restrictions. |
| `RouteRequirement` | Required colour (or grey), length, optional ferry Bus-symbol rules | Validate a `CardPayment` via `isSatisfiedBy(payment)` | Interface with implementations such as `ColouredRouteRequirement`, `GreyRouteRequirement`, and `FerryRouteRequirement`. Polymorphism eliminates UI or command conditionals for route types. |
| `CardPayment` | List of cards offered | Report total count, primary colour, Bus count, uniformity | Immutable value object. |
| `Player` | Hand, buses (starts at 17), score, destination tickets, colour | Check affordability, spend cards, use buses, manage tickets | Never expose raw mutable hand. `canAfford(payment)` and `spendCards(payment)` — external code never inspects the hand directly. |
| `TransportCardDeck` | Draw pile, discard pile | Draw, discard, reshuffle when empty | Randomness injected via `ShuffleStrategy`. |
| `FaceUpDisplay` | 5 visible card slots | Take card, refill slot, detect/apply 3-Bus flush rule | After any change, evaluate flush rule. If 3+ of 5 face-up are Bus cards, discard all 5 and replace. |
| `DestinationTicket` | Two location endpoints, point value | Identify a goal (immutable) | Does not calculate its own completion — that's `TicketCompletionChecker`'s job. |
| `DestinationTicketDeck` | Draw pile | Draw tickets, return unkept to bottom | Returned tickets go to bottom (not reshuffled — different from Transport deck). |
| `TurnManager` | Player order, current index, final round state | Advance turns, validate current player, trigger/manage final round | End-game trigger: player ends turn with 0–2 buses remaining. After trigger, each player gets exactly 1 more turn. |
| `ScoreCalculator` | Scoring rules (composed) | Compute route points, ticket scores, longest path, final scores | Delegates to `RouteScoreTable`, `TicketCompletionChecker`, and `LongestPathCalculator`. |
| `RouteScoreTable` | Length→points mapping | Return points: 1→1, 2→2, 3→4, 4→7 | Single source of truth for route scoring. |
| `TicketCompletionChecker` | — | Check if player's claimed routes connect ticket endpoints (BFS/DFS/Union-Find) | Graph traversal must not be in the UI. |
| `LongestPathCalculator` | — | Compute maximum-weight trail from player's claimed routes | DFS from every location, tracking visited edges. Computationally feasible (~15 routes max per player). +10 points to winner(s); ties share. |

### Double-Route Rules

- A single player may not claim both routes in a double-route pair.
- In 2-player games, once one route is claimed, the other is closed to all players.
- Routes in a pair share a `doubleGroupId`. See `london_board_layout.md` for the 8 pairs.

---

## 5. Application Layer

### GameApplicationService

Single entry point for all game actions. Receives commands, delegates to domain, publishes state changes.

```java
public class GameApplicationService {
    private final Game game;
    private final ScoreCalculator scoreCalculator;
    private final List<GameStateListener> listeners;

    public CommandResult executeCommand(GameCommand command);
    public GameSnapshot getSnapshot();
    public void addListener(GameStateListener listener);
}
```

### Commands

```java
public interface GameCommand {
    CommandResult execute(Game game);
}
```

Three implementations: `ClaimRouteCommand`, `DrawTransportCardCommand`, `DrawDestinationTicketsCommand`. Each encapsulates the data needed for its action (player ID, route ID, payment, etc.).

**Multi-step draw sequence:** `DrawTransportCardCommand` handles a single card draw. The
application-layer `TransportDrawCoordinator` orchestrates the two-draw sequence:
1. Player draws first card (face-up or blind).
2. If a face-up Bus card was taken, action ends (1 card only).
3. Otherwise, a second draw is allowed. Second draw restrictions: face-up Bus cards cannot be taken; a replacement Bus card that appeared in the first draw's refilled slot cannot be taken.
4. After completion, the turn advances.

The coordinator tracks per-turn draw state (draws taken, locked slot) and resets after the action
completes. This state belongs in the application layer, not in Swing or domain objects.

### Snapshots / DTOs

The UI must not receive mutable domain references. The application layer provides immutable read models (`GameSnapshot`, `PlayerSnapshot`) that protect the domain from accidental mutation.

### CommandResult

Immutable result with `success`/`failure` and a descriptive message. The UI displays the message but does not decide the rule.

---

## 6. UI Layer Rules

The Swing layer must be thin — focused only on rendering and input.

**The UI may:**
- Draw locations, routes, cards, and player info from snapshot data
- Capture mouse clicks on routes, cards, and buttons
- Display validation messages from `CommandResult`
- Highlight routes on hover

**The UI must not:**
- Decide whether a route can be claimed
- Remove cards from a player's hand
- Award points or advance turns directly
- Calculate ticket completion, longest path, or winner

**Board rendering data must be separate from domain data.** `BoardViewModel` maps domain location IDs to normalised screen coordinates (from `london_board_layout.md`). Changing the visual layout must not affect domain rules.

---

## 7. Design Patterns

Only use a pattern when it solves a real design problem. Every pattern must be justified with an ADR.

### Observer (Must Apply)

**Problem:** Domain must not call `boardPanel.repaint()`. Multiple UI components need to update on state change.

**Solution:** `GameStateListener` interface. `GameApplicationService` publishes a `GameSnapshot` after each command. Swing panels implement the listener and update from the snapshot.

### Command (Must Apply)

**Problem:** Player actions need validation, execution, and logging. Embedding logic in event listeners creates tight coupling.

**Solution:** `GameCommand` interface with `ClaimRouteCommand`, `DrawTransportCardCommand`, `DrawDestinationTicketsCommand`. Commands are explicit, testable objects that work for UI, tests, and future AI players.

**Interaction flow:**

```text
User clicks route → BoardPanel identifies RouteId
  → Controller creates ClaimRouteCommand(playerId, routeId, payment)
  → GameApplicationService.executeCommand(command)
  → Command validates (turn? unclaimed? buses? payment? double-route?)
  → On success: player spends cards/buses, route claimed, score awarded
  → TurnManager checks end-game trigger, advances turn
  → GameApplicationService publishes GameSnapshot
  → All Swing panels refresh from snapshot (Observer)
```

### Strategy (Must Apply)

**Problem:** Route validation differs between coloured and grey routes. Shuffling needs to be deterministic in tests.

**Solution:**
- `RouteRequirement` interface → `ColouredRouteRequirement`, `GreyRouteRequirement`
- `ShuffleStrategy` interface → `RandomShuffleStrategy`, `FixedOrderShuffleStrategy`
- `ShuffleStrategy` lives in the domain as a port. Infrastructure supplies random and deterministic
  implementations so domain rules never depend on infrastructure packages.

### Factory (Must Apply)

**Problem:** Game setup creates board, decks, players, and face-up display with specific London data. This must be centralised.

**Solution:** `GameFactory`, `BoardFactory`, `DeckFactory` centralise creation. `TestGameFactory` creates controlled test scenarios.

### Adapter (Nice to Apply)

**Problem:** Loading board data and translating mouse clicks to domain concepts.

**Solution:** `BoardDataSource` interface (supports Sprint 3 different maps). `RouteClickAdapter` translates screen coordinates to route IDs.

### Memento (Applied for Sprint 3 Undo)

**Problem:** Undo must restore a completed turn across players, routes, decks, face-up cards,
destination tickets, turn sequencing, phase, and application draw progress.

**Solution:** `GameApplicationService` captures immutable `GameMemento` values before turn-ending
actions and stores them in a bounded `UndoHistory`. Restore is performed through narrow domain
restore methods; Swing only calls `undoLastTurn()` and renders the resulting snapshot.

### Singleton — Avoid

Do not use Singleton for `Game`, `Board`, `Player`, `ScoreCalculator`, `TurnManager`, or any service/domain object. It creates hidden global state and makes testing harder. Prefer constructor injection. Acceptable only for stateless constants holders.

### Priority Matrix

| Priority | Pattern | Where Applied |
|---|---|---|
| Must apply | Observer | `GameStateListener` → Swing panels refresh from `GameSnapshot` |
| Must apply | Command | `ClaimRouteCommand`, `DrawTransportCardCommand`, `DrawDestinationTicketsCommand` |
| Must apply | Strategy | `RouteRequirement` polymorphism; `ShuffleStrategy` for testable shuffling |
| Must apply | Factory | `GameFactory`, `BoardFactory`, `DeckFactory` |
| Applied | Memento | `GameMemento`, `GameMementoFactory`, `UndoHistory` for Sprint 3 undo |
| Nice to apply | Adapter | `BoardDataSource`, `RouteClickAdapter` |
| Avoid | Singleton | Do not use for game state or services |

---

## 8. Antipatterns to Avoid

### God Class

Bad: `GameManager` handles UI clicks, draws board, validates routes, removes cards, calculates scores, changes turns, saves files.

Fix: Distribute into `Game`, `Board`, `Player`, `TurnManager`, `ScoreCalculator`, `GameApplicationService`, `BoardPanel`, `BoardRenderer`.

### Anaemic Domain Model

Bad:
```java
player.setScore(player.getScore() + route.getPoints());
player.getCards().removeAll(paymentCards);
route.setClaimedBy(player.getId());
```

Good:
```java
route.claim(player.id());
player.spendCards(payment);
player.addScore(scoreTable.pointsForLength(route.length()));
player.useBuses(route.length());
```

### UI-Driven Business Logic

Bad: `BoardPanel` checking card counts, removing cards, and awarding points directly.

Good: `BoardPanel` creates a `ClaimRouteCommand` and sends it to `GameApplicationService`. The UI expresses user intention; the domain enforces rules.

### Feature Envy

Bad: External code filtering `player.getHand()` to count pink cards.

Good: `player.canAfford(payment)` — the player inspects its own hand.

### Primitive Obsession

Use `RouteColor.PINK`, `CardColor.GREEN`, `PlayerColor.RED` instead of raw strings or ints.

---

## 9. Key OO Principles Applied to This Project

These principles guide implementation decisions. Each is illustrated with a concrete game example.

| Principle | Game Application |
|---|---|
| **Encapsulation** | `Player.hand` is private. External code calls `player.canAfford(payment)`, never inspects the hand list. `Route.claimedBy` has no setter — use `route.claim(playerId)`. |
| **Composition over Inheritance** | `Route` *has a* `RouteRequirement` (injected), not `ColouredRoute extends Route`. `ScoreCalculator` *has a* `TicketCompletionChecker` and `LongestPathCalculator`. |
| **Polymorphism** | `route.getRequirement().isSatisfiedBy(payment)` — no `if (grey) {} else {}`. Each `RouteRequirement` handles its own validation. |
| **Low Coupling** | `ScoreCalculator` does not import `javax.swing`. `TurnManager` does not know about cards. |
| **High Cohesion** | All scoring in `domain/scoring/`. All card logic in `domain/card/`. |
| **Responsibility Assignment** | "Can this player afford?" → `Player` (has the hand). "Does payment satisfy route?" → `RouteRequirement` (knows the rules). "Whose turn?" → `TurnManager` (owns turn state). |
| **DRY** | Route scoring expressed once in `RouteScoreTable`. Locations defined once in `BoardFactory`. Flush check expressed once in `FaceUpDisplay.needsFlush()`. |
| **Separation of Concerns** | Four layers with one-way dependencies. Domain knows nothing about UI. |

---

## 10. Error Handling

Use explicit `CommandResult` for validation. Never let invalid actions silently fail or crash.

Key validation failures to handle:
- Not the player's turn
- Route already claimed
- Insufficient cards or buses for payment
- Payment doesn't satisfy route requirement (wrong colour/count)
- Double-route restriction violated
- Face-up Bus card taken as second draw
- Destination ticket deck empty
- Game already ended

---

## 11. Testing Strategy

Core domain must have unit tests before or alongside UI work. All tests use `FixedOrderShuffleStrategy` for determinism.

### Test Categories

| Category | Key Scenarios |
|---|---|
| Route Claiming | Coloured route, grey route, Bus card substitution, double-route restrictions, insufficient cards/buses, score awarded correctly |
| Card Drawing | 2 blind draws, face-up + blind, face-up Bus limits to 1 card, replacement Bus can't be second draw, 3-Bus flush, reshuffle when empty |
| Destination Tickets | Draw 2/keep 1+, return to bottom, empty deck rejected |
| Turn Management | Correct order, out-of-turn rejected, end-game trigger (0–2 buses), final round countdown |
| Scoring | Route points (1/2/4/7), completed tickets add points, incomplete tickets subtract, BFS/DFS connectivity, longest path computation, ties share +10, tie-breakers |

### Test Infrastructure

Use `TestGameFactory` to create controlled scenarios with predetermined decks and small boards. Inject `ShuffleStrategy` for deterministic tests. No test should depend on Swing or file I/O.

---

## 12. Sprint 3 Extensibility

The architecture must support future extensions without structural changes:

| Extension | How the Architecture Supports It |
|---|---|
| Different board configurations | `BoardFactory` / `BoardDataSource` adapter loads different maps. `Board`, `Route`, `Location` are not London-specific. |
| Additional rules | `RouteRequirement` strategy and `ScoreCalculator` composition support new validation/scoring rules without modifying existing code. |
| Ferry routes | `FerryRouteRequirement` composes normal route payment rules with required Bus-symbol validation while snapshots expose route metadata for rendering. |
| Undo mechanism | `GameMemento` and bounded `UndoHistory` restore completed turns without exposing mutable domain internals to Swing. |
| Rush Hour events | `RushHourManager` owns a turn-based event clock, `RouteSelector` strategies choose affected routes, and `ClaimRoutePayment` keeps detour cards separate from normal route/Ferry payment. |
| 3–4 players | Already supported. Double-route 2-player restriction is parameterised by player count. |
| Save/load games | `GameSnapshot` and immutable data model make serialisation straightforward. |
| District bonus scoring | `Location` stores `district`. A `DistrictBonusScorer` can be added to `ScoreCalculator`. |

---

## 13. Architecture Decision Records (ADRs)

ADR files live in `docs/adr/` with naming convention `ADR-NNN-short-title.md`. Write ADRs during implementation as decisions are made, not deferred to the end.

### ADR Format

```md
# ADR-NNN: [Title]

## Status
[Proposed | Accepted | Superseded by ADR-XXX]

## Context
[What problem does this address?]

## Decision
[Which option was chosen and why?]

## Alternatives Considered
1. [Option A] — [why rejected]

## Consequences
Positive: [benefits]
Negative: [trade-offs]
```

### Recommended ADRs

| ADR | Topic |
|---|---|
| ADR-001 | Layered architecture — separating domain, application, infrastructure, UI |
| ADR-002 | Domain logic outside Swing |
| ADR-003 | Command pattern for player actions |
| ADR-004 | Board data separated from rendering coordinates |
| ADR-005 | Strategy pattern for RouteRequirement polymorphism |
| ADR-006 | Observer pattern (GameStateListener) for UI refresh |
| ADR-007 | Graph traversal for destination ticket completion |
| ADR-008 | Avoid Singleton for game state; prefer constructor injection |
| ADR-009 | Factory pattern for game setup (Sprint 3 extensibility) |
| ADR-015 | Rush Hour Events self-defined extension |

Create additional ADRs as new decisions arise during implementation.

---

## 14. Quality Checklist

Before submission, verify the items below and the supporting checklist in
`docs/design/architecture-quality-checklist.md`. Current verification status is tracked in
`docs/progress_tracker.md`.

### Architecture
- [x] No `javax.swing` imports in `domain` package
- [x] No `domain` imports from `application`, `infrastructure`, or `ui`
- [x] No God Classes — responsibilities distributed across domain objects
- [x] No anaemic models — domain objects have meaningful behaviour
- [x] Four-layer architecture with clear boundaries and one-way dependencies
- [x] If `ui` package is deleted, all domain/application tests compile and pass
- [x] UI panels do not duplicate lifecycle rule checks; they render from snapshots and submit commands
- [x] Transportation card UI exposes both face-up selections and an explicit blind deck control
- [x] Application commands share aggregate/domain queries for player lookup and game phase eligibility

### OO Design
- [x] All domain fields `private`, state modified through meaningful methods
- [x] Clients depend on interfaces (`RouteRequirement`, `ShuffleStrategy`, `GameCommand`, `GameStateListener`)
- [x] Composition used for behavioural variation; inheritance reserved for true type hierarchies
- [x] No `if/else`/`switch` chains based on object type
- [x] Constructor dependency injection used throughout
- [x] Repeated validation logic is owned by the object with the needed information or extracted behind a cohesive collaborator

### Code Quality
- [x] Google Java Style Guide compliance
- [x] Javadoc on all classes and public methods
- [x] No duplicated game logic (DRY)
- [x] Collections returned as unmodifiable views
- [x] Randomness injectable for deterministic tests
- [x] Rule loops have impossible-state handling so invalid card supplies cannot hang the game

### Testing
- [x] Route claiming tested (coloured, grey, Bus, double-routes)
- [x] Card drawing tested (face-up, blind, Bus restrictions, flush, reshuffle)
- [x] Blind deck UI control tested at Swing component level
- [x] Turn management tested (order, final round trigger, countdown)
- [x] Scoring tested (route points, tickets, longest path, tie-breakers)
- [x] Undo tested (route claims, ticket draws, card draws, final-round restoration, two-turn history)
- [x] Core rule tests do not depend on Swing or file I/O; the architecture boundary test
  intentionally reads source files to enforce package rules

### Documentation
- [x] ADRs explain *why* behind key decisions with alternatives considered
- [x] README explains architecture, build, and run instructions
- [x] Known defects documented

### Design Patterns
- [x] Observer, Command, Strategy, Factory, Memento applied and justified
- [x] Each pattern documented with an ADR
- [x] No decorative patterns — every pattern solves a real problem

---

## 15. Data References

Game data is defined in dedicated files — do not duplicate it in the architecture guide or in code:

| Data | Authoritative Source |
|---|---|
| Game rules and procedures | `game_rules.md` |
| 17 locations and 43 printed routes | `london_board_layout.md` |
| 20 destination tickets | `destination_cards.md` |
| Domain entity relationships | `domain_model.md` |
| Implementation phases | `docs/implementation_plan.md` |
| Conflict resolution priority | `AGENTS.md` (File Priority section) |

---

> **Game rules must live outside Swing UI.**
>
> **This project is intended to demonstrate excellent Software Engineering: Architecture and Design.**
