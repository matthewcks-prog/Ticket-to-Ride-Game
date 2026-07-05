# Quality Scenarios And Verification

This guide turns the supplied software-quality notes into concrete quality evidence for this
Ticket to Ride: London implementation.

## Primary Quality Attributes

| Attribute | Project Meaning | Evidence To Maintain |
|---|---|---|
| Correctness | Game rules are enforced consistently. | Domain/application tests for route claiming, drawing, tickets, turn flow, scoring, and invalid moves. |
| Testability | Rules can be tested without the Swing UI. | No Swing/AWT imports outside `ui`; deterministic shuffle; direct domain/application tests. |
| Modifiability | Sprint 3 changes affect narrow areas. | Cohesive classes, constructor injection, factories, strategies, ADRs. |
| Maintainability | Structure is readable and responsibilities are clear. | Rich domain model, no god class, no duplicated validation, Google Java Style. |
| Integrity | Invalid state cannot be created or mutated externally. | Constructor validation, immutable value objects, unmodifiable collection returns, command failures with unchanged state. |
| Usability | The UI helps users understand legal actions and results. | Snapshots drive enabled controls, command messages are shown, manual UI smoke checks. |
| Performance | Board rendering and scoring remain responsive for the small London graph. | Longest-path and connectivity tests; no expensive rule recomputation in repaint loops. |

## Quality Attribute Scenarios

### Claiming A Route Correctly

Source: current player.

Stimulus: attempts to claim a route with selected cards.

Artefact: `Game`, `Board`, `Route`, `RouteRequirement`, `Player`, and transport discard pile.

Response: the route is claimed only if it is available, the player has enough buses, payment
satisfies coloured/grey requirements with Bus wildcards, and double-route restrictions allow it.
Invalid claims leave state unchanged and return a useful failure message.

Response measure: tests assert ownership, cards, buses, score, turn state, discard pile, and
unchanged state on failure.

### Deleting Or Ignoring The UI

Source: developer or marker.

Stimulus: compile and test domain/application/infrastructure without relying on `ui`.

Artefact: non-UI packages and tests.

Response: rules compile and pass because no rule implementation depends on Swing.

Response measure: `ArchitectureBoundaryTest` and import checks show no Swing/AWT imports in
`domain`, `application`, or `infrastructure`, and no forbidden layer imports such as
`domain -> infrastructure`.

### Adding A Sprint 3 Rule Variant

Source: developer implementing an extension.

Stimulus: add a new route rule, scoring rule, board configuration, or save/load mechanism.

Artefact: relevant domain strategy/service, factory/data adapter, application command, and tests.

Response: the change is localised. UI still sends commands and renders snapshots; domain remains
testable without Swing.

Response measure: small focused diff, new tests, ADR if a new architectural mechanism is introduced.

### Invalid User Action

Source: user double-click, out-of-turn command, insufficient cards, empty deck, or stale UI state.

Stimulus: invalid command reaches the application service.

Artefact: command, application service, and domain aggregate.

Response: command returns failure; domain state remains unchanged; UI displays the message.

Response measure: tests assert failure result and unchanged state.

### Rush Hour Event Clock

Source: completed player turns.

Stimulus: Rush Hour moves from Forecast to Peak, completes a Peak round, recycles discarded event
cards, or has no event cards available.

Artefact: `RushHourManager`, `RushHourEvent`, `RushHourClaimRule`, and application command tests.

Response: event timing remains deterministic, empty event supplies stay inactive, recycled events
return to Forecast, and affected Peak routes require separate detour payment.

Response measure: Rush Hour tests cover Forecast, Peak, detour payment, bonus accounting, undo
restoration, no-event startup, and event discard recycling.

### End-Game Scoring

Source: final round completion.

Stimulus: calculate route points, destination tickets, longest path bonus, winners, and tie-breakers.

Artefact: `ScoreCalculator`, `TicketCompletionChecker`, `LongestPathCalculator`, and final score DTOs.

Response: score is deterministic and explainable, with graph algorithms isolated from UI rendering.

Response measure: scoring tests cover completed/incomplete tickets, longest-path ties, and final
score totals.

## Verification Checklist

Run this checklist after significant docs, rule, architecture, or Sprint 3 changes:

- [ ] The change still matches `game_rules.md`, `destination_cards.md`, and
  `london_board_layout.md`.
- [ ] No Swing/AWT imports exist in `domain`, `application`, or `infrastructure`.
- [ ] `domain` has no imports from `application`, `infrastructure`, or `ui`.
- [ ] UI sends commands and renders immutable snapshots only.
- [ ] New rules have tests at the domain or application layer.
- [ ] Invalid commands leave state unchanged.
- [ ] New abstractions solve a current variation, invariant, or cohesion problem.
- [ ] ADRs are added or updated for new patterns, package responsibility changes, or major
  architectural trade-offs.
- [ ] `docs/progress_tracker.md` records completed significant work.
- [ ] `mvn verify` or the documented IntelliJ Maven command passes before submission.

## Tactics To Prefer

- Increase cohesion by extracting focused classes only when a responsibility is clear.
- Reduce coupling by hiding mutable domain state behind intention methods and snapshots.
- Defer binding with factories and injected strategies where variation is real.
- Keep command execution atomic: validate before mutation or order mutations so failure cannot leave
  partial state.
- Fail fast for invalid board data, duplicate IDs, invalid lengths, and null dependencies.
- Treat Swing input as untrusted; domain/application must revalidate it.

