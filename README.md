# Ticket to Ride: London

A Java 25 Swing implementation of Ticket to Ride: London. The project prioritises a maintainable, testable architecture: game rules live outside Swing, player actions enter through application commands, and UI panels render immutable snapshots.

## Architecture

The codebase follows a four-layer structure under `src/main/java/ttrlondon`:

- `domain`: board, card, player, ticket, turn, game, and scoring rules. This package has no Swing dependencies and is unit tested without UI.
- `application`: commands, command results, immutable DTO snapshots, and `GameApplicationService`, the single entry point for player actions.
- `infrastructure`: London data factories and shuffle strategies. `BoardFactory`, `DeckFactory`, and `GameFactory` centralise setup data and keep map/deck creation out of UI code.
- `ui`: Swing panels, renderers, and view models. UI classes display snapshots and forward user intent; they do not validate route claims, score tickets, or mutate domain state directly.

Key patterns are documented in `docs/adr/`:

- Command: `ClaimRouteCommand`, `DrawTransportCardCommand`, and `DrawDestinationTicketsCommand`.
- Observer: `GameStateListener` updates Swing panels from `GameSnapshot`.
- Strategy: `RouteRequirement` and `ShuffleStrategy` keep rule variation and randomness injectable.
- Factory: setup and authoritative London data are created through factory classes.

## Requirements

- Java Development Kit 25 or newer.
- Maven 3.9.x or newer. If Maven is not on PATH, IntelliJ IDEA's bundled Maven also works.
- Windows, macOS, or Linux with desktop support for Swing.

## Build

From the repository root:

```powershell
mvn verify
```

On this Windows development machine, Maven was verified with:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2023.3.4\plugins\maven\lib\maven3\bin\mvn.cmd' '-Dmaven.repo.local=.m2/repository' verify
```

`verify` compiles the project, runs the JUnit suite, packages the jar, and enforces Google Java
Style with Checkstyle.

To confirm the packaged board background is inside the generated jar:

```powershell
jar tf target/ticket-to-ride-london-1.0-SNAPSHOT.jar | Select-String 'ttrlondon/ui/london_map.png'
```

## Run

Launch the Swing game from source:

```powershell
mvn exec:java
```

If the Maven exec plugin is unavailable, compile and run the main class from your IDE, or use:

```powershell
mvn test
java -cp target/classes ttrlondon.Main
```

The game opens with a full-size deterministic loading screen before a single integrated setup flow for 2 to 4 players, player names/colours, first-player selection, and all initial destination ticket choices. Loading, setup, ticket choices, and the main board share the same application window size; the board inherits the setup window bounds so the executable feels like one integrated Swing app. The loading screen uses real startup milestones with a longer first-screen display time so the artwork is visible without moving timing into game rules; after setup, the main board opens directly. The full flow supports route claiming, transportation card draws, destination ticket draws, score markers around the board edge, final-round indication, final scoring, winner display, and tie-breaker details.

The transportation card market includes five face-up cards and an explicit Blind Deck control. The
blind deck button submits a blind draw command; it does not contain draw rules itself.

## Git Workflow

Branching and merge history for assessment evidence are documented in
[`docs/git-branching-evidence.md`](docs/git-branching-evidence.md). Sprint 3 work
was merged via feature branches using `--no-ff` merge commits:

- `feature/sprint2_refactor` — anti-pattern refactorings and design docs
- `feature/sprint3/required-extension-introducing-ferries` — required Ferry extension
- `feature/sprint3/rush-hour-events` — self-defined Rush Hour Events extension
- `feature/sprint3/pick-from-list-undo-mechanism` — pick-from-the-list Undo extension
- `feature/sprint3/game-setup-form` — reusable pre-game setup UI

See `CONTRIBUTING.md` for branch naming and commit conventions.

## Verification

Quality checks completed for the current implementation:

- 106 automated tests pass.
- `src/main/java/ttrlondon/domain`, `application`, and `infrastructure` have no Swing/AWT
  imports; this is guarded by `ArchitectureBoundaryTest`.
- Maven `verify` enforces Google Java Style with 0 current Checkstyle violations.
- Javadoc site generation succeeds.
- Recommended ADRs 001 through 009 are present, plus ADR-010 through ADR-012 for setup draft
  handling, aggregate-owned action eligibility, and automated quality gates.
- Known defects are tracked in `docs/known_defects.md`.

## Project Documents

- `architecture_guide.md`: authoritative architecture and design guide.
- `game_rules.md`: rule specification.
- `london_board_layout.md`: board layout and rendering data.
- `destination_cards.md`: destination ticket data and UI guidance.
- `domain_model.md`: domain relationships and rationale.
- `antipatterns_and_refactoring.md`: Sprint 3 deliverable on identified anti-patterns and the
  refactoring techniques applied (Sprint 2 and Sprint 3-review refactorings).
- `docs/README.md`: documentation map and canonical-file notes.
- `docs/implementation_plan.md`: phase plan.
- `docs/progress_tracker.md`: implementation status.
- `docs/design/`: focused OO principles, pattern rationale, architecture quality checklist,
  refactoring guidance, quality scenarios, and assignment-defence notes.
