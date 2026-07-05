# Sprint 3 Undo Mechanism Implementation Plan

## Summary

Implement the pick-from-the-list Sprint 3 Undo extension as an application-layer Memento history.
The player can undo up to the last two completed turns, restoring the exact game state from before
those turns began.

## Rule Interpretation

- A completed turn is a successful route claim, destination-ticket draw, completed two-card
  transportation draw, immediate face-up Bus draw, or explicit end of a one-card transportation draw.
- Undo is applied one completed turn at a time.
- Undo is unavailable while a partial transportation-card draw is active.
- Undo restores exact runtime state only; it does not add save/load persistence.

## Implementation Approach

- Use immutable mementos captured by the application layer before a turn-ending action mutates the
  game.
- Keep a bounded history of two mementos.
- Restore through narrow domain restore methods that validate structural invariants and do not
  expose mutable internals.
- Keep Swing thin: the UI enables an Undo button from `GameSnapshot.canUndo()` and calls
  `GameApplicationService.undoLastTurn()`.

## Test Plan

- Verify undo after route claim restores route ownership, player hand, discard pile, buses, score,
  active player, and phase.
- Verify undo after destination-ticket draw restores player tickets and ticket deck order.
- Verify undo after completed transportation-card draws restores player hand, deck order,
  face-up display, turn, and draw state.
- Verify two consecutive undo operations work and a third fails cleanly.
- Verify failed commands do not add undo history.
- Verify undo is rejected during a partial transportation-card draw.
- Verify final-round phase and counters restore correctly.

