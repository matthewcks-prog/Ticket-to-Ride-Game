# ADR-006: Observer Pattern for UI Refresh

## Status
Accepted

## Context
Future Swing panels will need to refresh after successful actions, but the domain must not know
about Swing classes or call repaint methods. Multiple panels may need the same update: board,
player status, card market, destination tickets, and game status.

Without an observer boundary, UI components would have to poll state after every button click, or
the domain/application code would gain direct references to concrete panels.

## Decision
Use `GameStateListener` as an application-layer observer interface. `GameApplicationService`
maintains registered listeners and publishes a fresh immutable `GameSnapshot` after each successful
command. Swing panels in later phases can implement the listener and render from snapshots only.

Validation failures do not publish snapshots because no state has changed; callers receive a
`CommandResult` message instead.

## Alternatives Considered
1. Let domain objects notify Swing panels - rejected because it violates the rule that the domain has
   no UI dependencies.
2. Require UI polling after every command - rejected because it scatters refresh coordination across
   panels and controllers.
3. Publish mutable domain objects to panels - rejected because UI code could accidentally mutate game
   state outside validated commands.

## Consequences
Positive: UI refresh is decoupled from domain rules and command execution.
Positive: Multiple panels can subscribe independently to the same state changes.
Positive: Snapshots protect the domain from accidental UI mutation.

Negative: The application service must maintain listener registration and snapshot creation.
Negative: UI code must handle asynchronous-looking updates even though current execution is
synchronous.
