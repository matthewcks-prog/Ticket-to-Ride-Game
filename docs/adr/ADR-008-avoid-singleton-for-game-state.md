# ADR-008: Avoid Singleton for Game State

## Status
Accepted

## Context
The game aggregate, application service, decks, turn manager, and scoring helpers all carry or
coordinate state for one specific game session. A Singleton game or global service would make tests
order-dependent and would prevent multiple game sessions or deterministic fixtures from coexisting.

The architecture guide requires constructor dependency injection and warns against hidden globals.

## Decision
Use constructor injection for game-session dependencies. `GameApplicationService` receives a `Game`
in its constructor. Commands receive their action data through constructors or named factory methods.
Factories create configured game aggregates explicitly, and tests can create independent deterministic
games through `TestGameFactory`.

No Singleton is used for `Game`, `GameApplicationService`, `Player`, decks, `TurnManager`, or
scoring collaborators. Stateless value objects and enums remain normal objects or enum constants.

## Alternatives Considered
1. Singleton `Game` - rejected because it introduces hidden shared mutable state and makes tests
   interfere with each other.
2. Static application service methods accessing global state - rejected because dependencies become
   implicit and hard to replace for tests or alternate setups.
3. Service locator for domain objects - rejected because it hides construction requirements and
   weakens package boundaries.

## Consequences
Positive: Tests can create isolated games with deterministic decks and player orders.
Positive: Future UI setup can create, discard, or restart game sessions without clearing globals.
Positive: Dependencies are visible in constructors and easier to review.

Negative: Callers must wire objects explicitly through factories or constructors.
Negative: More setup code is required than a global static access pattern.
