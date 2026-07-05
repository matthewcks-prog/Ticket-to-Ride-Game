# Sprint 3 Readiness Plan

## Status

Active readiness record after the completed Ferry, Undo, and Rush Hour extension work.

## Purpose

Keep the codebase ready for Sprint 3 extension work without carrying stale refactor or sprint
checklists in the active planning documents. Historical evidence remains in ADRs, tests, and the
rules crosscheck audit.

## Completed Foundation Work

- [x] Preserved the layered architecture: `domain`, `application`, `infrastructure`, and `ui`.
- [x] Confirmed game rules remain outside Swing.
- [x] Added executable package-boundary checks for forbidden Swing/AWT imports outside `ui`.
- [x] Centralised player lookup and player-action eligibility on the `Game` aggregate.
- [x] Exposed action eligibility through immutable snapshots so Swing panels render state instead
  of duplicating lifecycle rules.
- [x] Corrected face-up Bus flush sequencing and impossible-supply edge cases.
- [x] Confirmed London setup data: 17 locations, 43 printed routes, 8 double-route pairs, 44
  transportation cards, and 20 destination tickets.
- [x] Made the blind transportation deck visible as an explicit UI control.
- [x] Split broad design notes into focused files under `docs/design/`.
- [x] Consolidated supplied guidance into `docs/design/` so `AGENTS.md` remains the
  single operational instruction source for AI agents.
- [x] Documented the rule crosscheck in `docs/rules_crosscheck_audit.md`.

## Sprint 3 Entry Criteria

- [x] Choose the Sprint 3 self-defined extension: Rush Hour Events.
- [x] Write or update ADRs before introducing new architectural mechanisms.
- [x] Keep rule/data changes aligned with `game_rules.md`, `destination_cards.md`, and
  `london_board_layout.md`.
- [x] Add focused tests for each new rule or extension workflow.
- [x] Run `mvn verify` or the documented IntelliJ Maven command before marking work complete.

## Completed Sprint 3 Required Extension

- [x] Introduced Ferries by converting selected Thames routes (`R28`, `R39`, `R42`, `R43`) into
  ferry routes.
- [x] Added `FerryRouteRequirement` as a cohesive route-payment Strategy.
- [x] Exposed ferry metadata through immutable snapshots and board view models.
- [x] Added generated ferry/Bus-symbol route marker artwork and documented the Generative AI use.
- [x] Verified on 2026-05-31 with full Maven `verify`: 75 tests passed, 0 Checkstyle violations.

## Completed Sprint 3 Pick-From-The-List Extension

- [x] Introduced the Undo mechanism using a bounded application-layer Memento history.
- [x] Added `GameMemento`, `GameMementoFactory`, and `UndoHistory` outside Swing.
- [x] Added narrow domain restore APIs for players, routes, decks, face-up display, tickets, and
  turn state.
- [x] Exposed undo availability through `GameSnapshot.canUndo()` and added a Swing Undo button that
  only submits the undo intention.
- [x] Added ADR-014 for the Memento history decision.
- [x] Added focused application tests for route, ticket, transportation draw, final-round, failed
  command, partial-draw, and two-turn undo scenarios.

## Completed Sprint 3 Self-Defined Extension

- [x] Introduced Rush Hour Events as the self-defined Sprint 3 extension.
- [x] Added `RushHourManager`, `RushHourEvent`, `RushHourClaimRule`, `RouteSelector`, and
  `RushHourPhase` as cohesive domain/application collaborators.
- [x] Added `RushHourEventFactory` for London event setup data.
- [x] Exposed forecast/peak event state through immutable snapshots and rendered it through
  `RushHourPanel`.
- [x] Added ADR-015 for the Rush Hour Events decision.
- [x] Added focused tests for event lifecycle, detour payments, bonus scoring, empty supplies, and
  discard recycling.

## Current Candidates

- Keep transportation-card draw orchestration isolated in `TransportDrawCoordinator`; add similar
  application-layer coordinators only when future multi-step actions justify them.
- Keep save/load work, if selected later, at the application/infrastructure boundary rather than
  inside Swing panels.
