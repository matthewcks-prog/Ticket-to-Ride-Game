# Patterns and Design Rationale

## Command

Applied through `GameCommand` and the three concrete action commands.

Problem: Swing listeners should not contain game rules or mutate domain objects directly.

Decision: UI panels create commands and submit them to `GameApplicationService`. The commands validate
and execute against the `Game` aggregate.

Alternative considered: Put action logic in Swing event handlers. This was rejected because it would
couple UI to rules and make core behaviour hard to unit test.

Trade-off: A dedicated application-layer coordinator handles multi-step draw state, while the
command boundary keeps user intent explicit and testable.

## Observer

Applied through `GameStateListener` and immutable `GameSnapshot` publication.

Problem: Multiple Swing panels need updates after successful actions, but the domain must not know
about Swing.

Decision: `GameApplicationService` publishes snapshots to registered listeners.

Alternative considered: Direct UI calls from domain or application classes. This was rejected because
it would violate the layer boundary and block non-GUI tests.

Trade-off: Listeners must tolerate snapshot-only data, which is intentional because it prevents UI
mutation of domain objects.

## Strategy

Applied through `RouteRequirement` and `ShuffleStrategy`.

Problem: Route payment rules and shuffle behaviour vary.

Decision: Inject strategies for rule variation and deterministic testing. `ShuffleStrategy` is a
domain port; infrastructure provides concrete random and fixed-order implementations.

Alternative considered: Conditional logic in `Route` and random shuffling inside deck constructors.
This was rejected because it reduces extensibility and makes tests nondeterministic.

Trade-off: More small classes exist, but each has a clear role and removes duplicated conditionals.

## Factory

Applied through `BoardFactory`, `DeckFactory`, and `GameFactory`.

Problem: London setup has specific board data, deck composition, initial deal order, face-up flush
rules, and initial ticket choices.

Decision: Factories centralise setup and data creation.

Alternative considered: Hard-code map/deck data in UI or in the `Game` constructor. This was rejected
because it would mix data loading, rendering, and domain rules.

Trade-off: The current factories are London-specific, but they create a clear place to introduce
Sprint 3 map variants or data-source adapters.

## Memento

Applied through `GameMemento`, `GameMementoFactory`, and `UndoHistory`.

Problem: Sprint 3 undo must restore route claims, player resources, deck order, face-up cards,
destination-ticket order, turn state, phase, and temporary draw progress exactly.

Decision: `GameApplicationService` captures immutable mementos before completed turns and keeps a
bounded two-entry history.

Alternative considered: Inverse commands. This was rejected because reshuffles, face-up flushes,
ticket bottom returns, and final-round counters are easier to restore exactly than reverse.

Trade-off: Mutable domain objects expose narrow restore methods, but Swing still never receives
mutable domain state or performs undo rules.

## Deliberately Avoided: Singleton

Game state and services are not singletons. Dependencies are passed through constructors so tests can
create isolated games and deterministic decks. Singleton-style global state would hide dependencies
and make undo/save-load extensions harder.
