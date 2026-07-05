# ADR-014: Undo Memento History

## Status
Accepted

## Context
Sprint 3 requires an undo mechanism for the last two completed turns. A completed turn may mutate
many parts of the model: route ownership, player hand, player score, bus count, ticket ownership,
transport draw and discard piles, face-up cards, destination-ticket deck order, phase, and
final-round turn counters.

The existing architecture keeps Swing thin and routes all game actions through
`GameApplicationService`, making the application layer the natural place to coordinate undo without
letting UI code inspect or mutate domain internals.

## Decision
Use a bounded application-layer Memento history. Before a turn-ending action mutates the game, the
service captures an immutable `GameMemento`. After the action succeeds and the turn completes, the
memento is pushed into a two-entry `UndoHistory`. Undo restores the most recent memento through
narrow domain restore APIs and publishes a fresh `GameSnapshot`.

## Alternatives Considered
1. Inverse commands - rejected because route claims, deck reshuffles, face-up flushes,
   destination-ticket bottom returns, final-round transitions, and future extensions make reliable
   reversal fragile.
2. Swing-side snapshots - rejected because it would move game-state knowledge into UI classes and
   violate the domain/application boundary.
3. Full serialization to disk - rejected because Sprint 3 undo only needs in-memory turn history,
   not save/load persistence.

## Consequences
Positive: undo restores exact runtime state, including deck order and final-round counters.
Positive: Swing remains a passive client that submits an undo intention and renders snapshots.
Positive: future extensions can join undo by extending the memento state.

Negative: mutable domain objects need narrow restoration methods that must be kept aligned with
new mutable fields.

