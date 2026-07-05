# Refactoring And Anti-Patterns Guide

Use this guide for the future item that asks for relevant design
anti-patterns , where they occurred, why they were harmful, and what refactoring technique addressed them.

This is a planning and evidence guide. It does not claim that every listed smell exists now, and it
does not authorise behaviour changes during a refactor.

## Refactoring Rules

- Refactoring changes internal structure without changing observable game behaviour.
- Do not mix refactoring with a new rule, UI feature, extension, or scoring change.
- Add or confirm tests before moving rule logic.
- Make one behaviour-preserving step at a time, then run verification.
- Keep the dependency direction intact: `ui -> application -> domain`; `infrastructure` supplies
  setup/randomness details.
- Update an ADR when the refactor changes package responsibility, command flow, state ownership, or
  pattern use.

## Evidence Standard For The Assignment

For each anti-pattern, record the evidence in this shape:

| Field | What To Capture |
|---|---|
| Anti-pattern | Name the smell precisely: God Class, UI-Driven Business Logic, Feature Envy, Primitive Obsession, Duplicate Observed Data, Long Method, Shotgun Surgery, or Speculative Generality. |
| Location | Cite the exact class/method and line reference from the Sprint 2 implementation. |
| Why it is a problem | Explain the design harm in this project: testability, coupling, cohesion, correctness, extensibility, readability, or rule duplication. |
| Refactoring applied | Name the technique: Extract Class, Move Method, Encapsulate Collection, Replace Conditional with Polymorphism, Introduce Parameter Object, Extract Method, or Method Object. |
| Improvement | Explain how the change improved maintainability, readability, extensibility, cohesion, or coupling. |
| Proof | Link tests, ADRs, package-boundary checks, or before/after code snippets. |

## Most Relevant Anti-Patterns For This Codebase

### UI-Driven Business Logic

Smell: Swing panels validate game rules, mutate domain state, score routes, advance turns, or decide
winner logic.

Why it matters here: the core architectural rule is that game rules must live outside Swing. UI-only
validation cannot be proven with headless domain tests and can be bypassed by another UI or command
source.

Preferred refactoring: Move Method or Extract Class into `domain` or a cohesive domain service, then
let Swing submit an application command and render the returned snapshot/result.

Evidence to look for:

- `javax.swing` classes checking route affordability, double-route rules, ticket completion, or
  longest-path scoring.
- UI classes holding independent copies of route ownership, player card counts, score, or phase.
- Tests that require Swing to prove a game rule.

### God Class Or Broad Manager

Smell: one class owns setup, card market, player state, route validation, scoring, turn progression,
and UI notification.

Why it matters here: it creates low cohesion and many reasons to change. Adding a Sprint 3 extension
would cause shotgun surgery or a risky edit to one overloaded class.

Preferred refactoring: Extract Class, Move Field, and Move Method into collaborators with clear
ownership such as `TurnManager`, `FaceUpDisplay`, `TransportCardDeck`, `ScoreCalculator`, and
factory classes.

Evidence to look for:

- A class with unrelated groups of fields and methods.
- Methods that switch between setup, UI, scoring, card draw, and turn concerns.
- Tests that must instantiate an oversized object graph to test one small rule.

### Anemic Domain Model

Smell: domain entities expose getters/setters while commands, services, or UI code perform the real
rules by reaching into their data.

Why it matters here: rules become scattered and duplicated, and domain objects stop protecting their
own invariants.

Preferred refactoring: Move Method into the information expert and Encapsulate Collection. Examples:
`Player.canAfford(payment)`, `Player.spendCards(payment)`, `Route.claim(playerId)`, and
`RouteRequirement.isSatisfiedBy(payment)`.

Evidence to look for:

- External code mutating a player's hand or route owner directly.
- Public setters for rule-critical state.
- Repeated external loops over `Player` hand cards or `Board` routes to decide a rule.

### Feature Envy

Smell: one class repeatedly asks another class for data and then makes decisions that naturally
belong to the other class.

Why it matters here: ownership of rules becomes unclear. A command that inspects every detail of a
`Player` hand or a Swing panel that inspects every route detail is more coupled than needed.

Preferred refactoring: Move Method to the class that owns the data, or introduce a small domain
policy when the rule spans multiple entities.

Evidence to look for:

- Long chains like `game.board().routes().stream()` inside UI or application code for rule checks.
- External payment validation that could be asked of `Player`, `Route`, or `RouteRequirement`.

### Primitive Obsession

Smell: IDs, colours, lengths, player identity, or payment selections are represented as arbitrary
strings and integers where they carry domain meaning or constraints.

Why it matters here: invalid state is easier to create, and rule code becomes harder to read.

Preferred refactoring: Replace Value with Object or use enums/value objects where they protect a
real invariant. Do not introduce a tiny class for every value unless it improves safety or clarity.

Evidence to look for:

- Raw strings for route colours or card colours instead of enums.
- Negative or out-of-range bus counts, route lengths, or ticket point values possible through public
  constructors.
- Repeated validation of the same primitive group.

### Duplicate Observed Data

Smell: UI stores its own copy of business state and tries to keep it synchronized with domain state.

Why it matters here: stale UI state can show legal actions that the domain rejects, or worse, encode
different rule assumptions from the domain.

Preferred refactoring: move authoritative state into domain/application and expose immutable
snapshots. UI caches only temporary presentation state such as hover and selected route id.

Evidence to look for:

- Swing fields for route ownership, score, current player, card counts, or destination-ticket
  completion.
- Repaint logic recomputing rules instead of rendering a snapshot.

### Speculative Generality

Smell: abstractions, factories, strategies, or registries are introduced without current variation,
test value, or a credible Sprint 3 extension need.

Why it matters here: the assignment rewards good architecture, not decorative patterns. Extra
abstraction can reduce readability.

Preferred refactoring: Inline Class or Inline Method when an abstraction adds no protection,
variation, or cohesion. Keep Strategy where variation is real, such as shuffle behaviour and route
requirements.

Evidence to look for:

- Interfaces with one implementation and no test double or planned extension.
- Distributed architecture concepts added to a local Swing game without a network requirement.

## Current Watchlist Before Sprint 3

The current codebase already records that `GameApplicationService` may deserve extraction only if
Sprint 3 adds more multi-step actions. This is not a defect by itself: the current draw orchestration
belongs in the application layer because it is temporary per-turn interaction state. Reassess it if
new extensions make the service harder to read or test.

Also watch large Swing panels such as `ActionPanel`. Size alone is not an anti-pattern, but if UI
layout, command construction, and rule decisions start mixing, split presentation helpers or add
snapshot data rather than moving rules into Swing.

## Refactoring Technique Map

| Smell | First Refactoring To Consider | Typical Target |
|---|---|---|
| UI rule checks | Move Method / Extract Class | Domain entity, domain service, or application command boundary |
| God Class | Extract Class / Move Field / Move Method | `TurnManager`, deck/display classes, scoring classes, factories |
| Anemic domain | Move Method / Encapsulate Collection | `Player`, `Route`, `Board`, `DestinationTicketDeck` |
| Feature envy | Move Method | The information expert owning the data |
| Primitive obsession | Replace Value with Object / enum | IDs, colours, route length, payment selection |
| Duplicate observed data | Introduce Snapshot / Hide Delegate | Application DTOs and query methods |
| Long algorithmic method | Replace Method with Method Object | Longest path, ticket connectivity, score breakdown |
| Type switching | Replace Conditional with Polymorphism | `RouteRequirement`, shuffle/scoring policies |

