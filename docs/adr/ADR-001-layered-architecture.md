# ADR-001: Layered Architecture

## Status
Accepted

## Context
The game must demonstrate maintainable architecture, not only a working Swing prototype. The
rules for Ticket to Ride: London include route claiming, card supply behaviour, turn sequencing,
and final scoring. These rules need to be testable without opening a Swing window and must remain
stable if the UI is replaced or extended.

The assignment also requires Sprint 3 extensibility for different boards, additional rules, and
2-4 players. A UI-centred design or a single game manager would make those extensions difficult
because rule validation, rendering, and setup data would become tightly coupled.

## Decision
Use a four-layer architecture:

1. `domain` owns game concepts, state, invariants, and rule behaviour.
2. `application` coordinates use cases and exposes immutable snapshots to the UI.
3. `infrastructure` provides setup, configuration, data loading, and randomness adapters.
4. `ui` renders state and forwards player intentions without enforcing game rules.

Dependencies flow inward: UI and application depend on domain; domain does not depend on Swing,
application services, or infrastructure details. Randomness is injected through an interface so
domain tests can use deterministic ordering.

## Alternatives Considered
1. Swing-first implementation - rejected because route validation, scoring, and turn rules would
   be difficult to unit test and would violate the requirement that game rules live outside Swing.
2. Single `GameManager` coordinating all behaviour - rejected because it concentrates unrelated
   responsibilities and risks becoming a God Class.
3. Data-only domain objects with services doing all logic - rejected because it leads to an
   anaemic domain model where external services inspect and mutate state that should be
   encapsulated by objects such as `Player`, `Route`, and `TurnManager`.

## Consequences
Positive: Domain and application code can be compiled and tested without the UI.
Positive: Future board configurations and UI changes can be added without rewriting core rules.
Positive: Responsibilities are easier to review against the architecture guide and marking rubric.

Negative: More classes and package boundaries are required up front.
Negative: Some simple workflows require translation between domain objects, commands, and snapshots.
