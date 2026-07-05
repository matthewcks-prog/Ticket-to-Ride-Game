# ADR-011: Aggregate-Owned Player Action Eligibility

## Status
Accepted

## Context
Player-action commands and Swing panels both needed to know whether the game was accepting turn
actions. Before this refactor, commands duplicated phase checks and player lookup loops, while UI
panels duplicated the same phase rule to enable or disable controls.

This duplication increased the chance of shotgun surgery: adding a new action-accepting phase or
changing player lookup semantics would require edits across several command and UI classes.

## Decision
Move player lookup and player-action phase eligibility onto the `Game` aggregate:

- `Game.findPlayer(playerId)` owns lookup within the game session.
- `Game.acceptsPlayerActions()` owns the lifecycle query for turn-action eligibility.
- `GameSnapshot.acceptsPlayerActions()` exposes the same read-model decision to Swing panels.

Commands still own their command-specific validation messages, but they ask the aggregate for
shared lifecycle and player identity facts instead of reimplementing those checks.

## Alternatives Considered
1. Keep duplicated checks in each command - rejected because it repeats lifecycle knowledge and
   makes later phase changes error-prone.
2. Add a separate application validator service - rejected for now because the `Game` aggregate
   already owns the phase and player collection needed to answer these queries.
3. Let UI panels check `GamePhase` directly - rejected because panels should render snapshots and
   submit intentions, not duplicate rule/lifecycle decisions.

## Consequences
Positive: Player lookup and action eligibility have a single source of truth.
Positive: Swing controls depend on snapshot intent rather than re-encoding domain phases.
Positive: Commands are smaller and more cohesive around command-specific validation.

Negative: `Game` exposes two more query methods, so future changes must keep them query-only and
free of side effects.
