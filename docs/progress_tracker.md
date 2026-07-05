# Project Progress Tracker

## Current Focus

Sprint 3 extension implementation. The required Ferry extension and pick-from-the-list Undo
extension have been added with documentation, ADR coverage, UI support, and focused tests. The
packaged board background, startup loading screen, and board-edge score markers are now implemented.

## Completed Foundation Work

- [x] Audited active source against architecture and game-rule documents.
- [x] Confirmed no Swing/AWT imports in `domain`, `application`, or `infrastructure`.
- [x] Added an automated architecture boundary test for forbidden Swing/AWT imports outside `ui`.
- [x] Moved `ShuffleStrategy` to the domain boundary and added automated dependency-direction checks
  so `domain` cannot import `application`, `infrastructure`, or `ui`.
- [x] Extracted transportation-card draw sequencing from `GameApplicationService` into
  `TransportDrawCoordinator`, keeping partial draw state and second-draw Bus restrictions cohesive
  in the application layer.
- [x] Added `Game.acceptsPlayerActions()` and `Game.findPlayer(...)`.
- [x] Updated route claim, transportation draw, and destination ticket commands to use
  aggregate-level player/action queries.
- [x] Added `GameSnapshot.acceptsPlayerActions()` and updated Swing panels to render from the
  snapshot instead of duplicating phase checks.
- [x] Fixed face-up Bus flush sequencing and added loop protection for impossible supplies.
- [x] Added focused tests for corrected face-up display behaviour.
- [x] Fixed action-panel cropping by giving action controls an explicit grid layout and letting the
  destination-ticket scroller absorb side-panel height changes.
- [x] Audited active rules, factories, scoring, turn handling, and Swing boundaries against
  `games_rules_crosscheck.md`.
- [x] Confirmed the codebase intentionally uses the +10 Longest Continuous Path bonus, not London
  district bonus scoring.
- [x] Reworked `CardMarketPanel` so the blind transportation deck is an explicit visible control.
- [x] Added a Swing component test for blind deck draw intent.
- [x] Packaged `london_map.png` as a classpath resource so the board background is available from
  the executable jar.
- [x] Added a deterministic startup loading dialog before game setup.
- [x] Added a short minimum display duration for startup loading transitions so the progress screen
  is perceptible without moving timing or UI concerns into domain/application rules.
- [x] Added the packaged loading screen artwork to the loading window and standardised loading,
  setup, and destination-ticket setup pages around a shared app-sized Swing shell.
- [x] Replaced the multi-popup setup sequence with one full-size setup workflow that collects
  player configuration and all initial destination-ticket choices in the same window.
- [x] Replaced setup's numeric player-count spinner with explicit 2/3/4 player selector buttons
  and removed the second post-setup loading page.
- [x] Fixed setup CardLayout ghosting by making setup pages repaint over an opaque app panel.
- [x] Preserved setup window bounds when opening the main board so setup-to-game transition no
  longer jumps to a platform-chosen frame position.
- [x] Added generated setup background artwork at `src/main/resources/ttrlondon/ui/setup_background.png`.
- [x] Rebalanced the main side panel so Player and Rush Hour share the top row and destination
  tickets receive the expandable lower space.
- [x] Added board-edge score markers driven by immutable player snapshots.
- [x] Fixed in-game Claim Route and Draw Tickets popups so gameplay prompts use compact,
  owner-centred dialogs instead of the full setup-sized destination-ticket shell or platform-sized
  `JOptionPane` placement.
- [x] Added `docs/rules_crosscheck_audit.md`.
- [x] Split the advanced OO design note into focused files under `docs/design/`.
- [x] Added `docs/README.md` to clarify canonical root documents and supporting docs.
- [x] Replaced stale refactor-plan wording with a Sprint 3 readiness plan.
- [x] Updated `game_rules.md`, `domain_model.md`, `class_diagram.md`, `design_rationale.md`,
  `architecture_guide.md`, `README.md`, and `docs/known_defects.md` to reduce documentation drift.
- [x] Curated the supplied engineering guides into `docs/design/` without importing duplicate or
  out-of-scope lecture material.
- [x] Removed the separate supporting-docs category so refactoring, quality, and
  defence guidance lives with the rest of the design documentation.
- [x] Ran full Maven verification with IntelliJ's bundled Maven on 2026-05-30 after the
  design-docs consolidation: 54 tests passed and 0 Checkstyle violations.
- [x] Generated Javadoc successfully after the audit updates.

## Blocked

None. Plain `mvn` is still not on PATH in the current shell, but IntelliJ's bundled Maven is
available and documented in `README.md`.

## Sprint 3 Deliverables

- [x] Required extension "Introducing Ferries" implemented for selected Thames routes:
  `R28`, `R39`, `R42`, and `R43`.
  - [x] Added `FerryRouteRequirement` under the existing `RouteRequirement` Strategy.
  - [x] Added route metadata for route kind and required Bus symbols, exposed through snapshots
    and board view models for rendering.
  - [x] Added generated ferry/Bus-symbol UI marker artwork at
    `src/main/resources/ttrlondon/ui/ferry-symbol.png` and documented the Generative AI usage in
    `sprint3/ferries_implementation_plan.md`.
  - [x] Added ADR-013 for the ferry route requirement strategy decision.
  - [x] Updated `game_rules.md`, `london_board_layout.md`, `domain_model.md`,
    `architecture_guide.md`, and `design_rationale.md` for the Ferry extension.
  - [x] Added focused domain, application, infrastructure, and view-model tests for ferry rules,
    factory data, command mutation boundaries, and rendering metadata.
  - [x] Ran full Maven verification with IntelliJ's bundled Maven on 2026-05-31: 75 tests passed,
    0 Checkstyle violations, architecture boundary test green.

- [x] Pick-from-the-list extension "Undo mechanism" implemented with bounded two-turn history.
  - [x] Added `GameMemento`, `GameMementoFactory`, and `UndoHistory` as application-layer Memento
    support.
  - [x] Added narrow restore APIs to mutable domain objects while keeping Swing out of game-state
    restoration.
  - [x] Added `GameApplicationService.undoLastTurn()`, `GameApplicationService.canUndo()`, and
    `GameSnapshot.canUndo()`.
  - [x] Added a Swing Undo button that submits an application request and displays the
    `CommandResult`.
  - [x] Added ADR-014 for the undo Memento history decision.
  - [x] Added focused tests for route, ticket, transportation draw, explicit draw end, final-round,
    failed-command, partial-draw, and two-turn undo behaviour. Current test suite: 83 tests.
  - [x] Ran full Maven verification with IntelliJ's bundled Maven on 2026-05-31: 83 tests passed,
    0 Checkstyle violations, architecture boundary test green.

- [x] Object-Oriented Design deliverable "Identified design antipatterns from Sprint 2 and applied
  refactoring techniques" written to the canonical root file `antipatterns_and_refactoring.md`
  (`docs/design/antipatterns-and-refactoring.md` now points to it).
  - Part A (refactored during Sprint 2, git-backed): UI-driven business logic/shotgun surgery
    (`d4c16df`, ADR-011), feature envy/duplicated player lookup (`d4c16df`), long method/cryptic
    result (`e779588`).
  - Part A (still present after the Sprint 2 MVP; identified in review and refactored now):
    - [x] Feature Envy on the card subsystem: moved card-supply rules into `TransportCardDeck.canDraw()`,
      `FaceUpDisplay.isBusAt(int)`, and `FaceUpDisplay.hasNonBusCardOutsideSlot(int)`; inlined the
      trivial `isFaceUpBus` wrapper; removed the duplicated deck-affordance rule from
      `DrawTransportCardCommand`.
    - [x] Long Parameter List / Data Clump: introduced `application.dto.TransportDrawProgress`,
      shrinking the `GameSnapshot` constructor from 17 to 14 parameters while keeping the public
      getters stable.
    - [x] Duplicated Code in `ActionPanel`: extracted `canStartTurnAction` and
      `refreshClaimRouteButton` to remove the duplicated claim-enable predicate.
    - [x] Duplicated Code across Swing: extracted the destination-ticket "keep at least one" dialog
      loop into `UiSupport.chooseKeptTickets(...)`, shared by `ActionPanel` and `GameSetupDialog`.
    - [x] Duplicated Code (15+ copies): replaced the per-class `requireText`/`normalizeOptionalText`
      helpers with a single `ttrlondon.domain.common.Text` utility, unifying null-or-blank id
      handling to `IllegalArgumentException`.
  - Note: the deliverable now leads with these still-present anti-patterns (Part A) and keeps the
    Sprint 2 ones (Part B) for completeness.
  - [x] Added focused unit tests for the new `TransportCardDeck`/`FaceUpDisplay` queries.
  - [x] Ran `mvn verify` after the refactorings on 2026-05-30: 57 tests pass, 0 Checkstyle
    violations, architecture boundary test still green.
  - [x] Added `TextTest` to lock the unified null-or-blank identifier contract
    (`IllegalArgumentException`, not `NullPointerException`) across domain, application, and command
    layers; removed the corresponding entry from `docs/known_defects.md`. Full suite now 68 tests.

- [x] Object-Oriented Design deliverable "Updated UML Diagram" cross-checked against the marking
  rubric.
  - [x] Added `sprint3/updated_class_diagram.md` as the Sprint 3 class-diagram hand-in wrapper.
  - [x] Linked the Sprint 2 baseline, Sprint 3 intermediate, Sprint 3 final PNG, editable Draw.io
    source, and canonical markdown UML source.
  - [x] Made Sprint 3 changes visibly comparable: Ferry routes, Undo/Memento, Rush Hour events,
    transport draw coordination, new observer panel, snapshot fields, and factory setup.
  - [x] Expanded `design_rationale.md` with explicit CRC cards, extra cardinality justification,
    Rush Hour Strategy coverage, Factory coverage for `RushHourEventFactory`, and rejected-pattern
    justification.

## Next Work

- [x] Select and design the remaining Sprint 3 self-defined extension: Rush Hour Events.
- [x] Create ADRs for extension-specific decisions before implementation.
- [x] Add initial focused tests alongside the Rush Hour extension.
- [x] Added Rush Hour lifecycle tests for empty event supplies and event discard recycling after a
  completed Peak round.
- [x] Run full verification after the Rush Hour extension: IntelliJ bundled Maven `verify` passed
  on 2026-05-31.
- [x] Run full verification after packaged map/loading/score-marker work: IntelliJ bundled Maven
  `verify` passed on 2026-05-31 with 106 tests, 0 Checkstyle violations, and the packaged jar
  contains `ttrlondon/ui/london_map.png`, `ttrlondon/ui/loading_screen.png`, and
  `ttrlondon/ui/setup_background.png`.
- [x] Run full verification after in-game dialog sizing fixes: IntelliJ bundled Maven `verify`
  passed on 2026-05-31 with 106 tests, 0 Checkstyle violations, and the architecture boundary
  test green.
- [x] Run full verification after dependency-direction hardening: IntelliJ bundled Maven `verify`
  passed on 2026-06-01 with 107 tests, 0 Checkstyle violations, and the expanded architecture
  boundary test green.
- [x] Run full verification after the transport draw coordinator refactor: IntelliJ bundled Maven
  `verify` passed on 2026-06-01 with 107 tests, 0 Checkstyle violations, and the architecture
  boundary test green.
- [x] Run full verification after the Sprint 3 UML/rationale readiness updates: IntelliJ bundled
  Maven `verify` passed on 2026-06-01 with 109 tests, 0 Checkstyle violations, packaged jar
  generated, and the architecture boundary test green.
- [x] Added deterministic Sprint 3 demo-video launch mode on branch `demo_video`.
  - `mvn exec:java -Dexec.args="--demo"` opens a prepared mid-game state with Rush Hour Peak
    active, route `R28` ready as a Ferry plus affected route claim, a one-click P2 turn, and two
    consecutive undo operations available after the demonstrated turns.
  - Added `docs/demo_video.md` with the exact recording actions and concise transcript.
  - Full Maven verification passed on 2026-06-01 with 110 tests, 0 Checkstyle violations, and the
    architecture boundary test green.
- [x] Added a manual player-viewer dropdown to the player summary panel so the demo can keep
  showing Matthew's score after a successful route claim advances the active turn to P2.
  - Full Maven verification passed on 2026-06-01 with 111 tests, 0 Checkstyle violations, and the
    architecture boundary test green.
