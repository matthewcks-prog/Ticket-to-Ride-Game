Do NOT update or consider anything in the Sprint 1 folder — these are old work.

## Repository Documentation Authority

`AGENTS.md` is the single operational source of truth for AI coding agents working in this
repository. Supporting documents under `docs/design/` provide rationale, checklists, quality scenarios, and refactoring evidence templates, but they do not override this file or the file
priority order below.

Before writing or changing implementation code, always read `architecture_guide.md`, `docs/implementation_plan.md`, `docs/progress_tracker.md`, and the relevant rule/data files. If a proposed code change conflicts with the architecture guide, stop and update the design rationale before proceeding.

## Mandatory Pre-Implementation Checklist

Before writing any code, the AI coding agent MUST:

1. Read `architecture_guide.md` — the authoritative architecture and design guide.
2. Read `docs/implementation_plan.md` — the phased implementation plan.
3. Read `docs/progress_tracker.md` — current progress and what to work on next.
4. Read the relevant rule/data files for the feature being implemented:
   - `game_rules.md` — complete game rules specification.
   - `destination_cards.md` — destination ticket data and UI guidance.
   - `london_board_layout.md` — location coordinates, route data, and rendering guidance.
   - `domain_model.md` — domain entity relationships and rationale.

## Key Architectural Constraints

- **Game rules must live outside Swing UI.** No game logic in `javax.swing` classes. This is non-negotiable.
- **No God Classes.** No class should handle UI, logic, scoring, and data loading.
- **Rich domain model.** Domain objects must have meaningful behaviour, not just getters/setters.
- **Constructor dependency injection.** Pass dependencies explicitly. No hidden globals or unnecessary Singletons.
- **Follow Google Java Style Guide.** All code must conform to https://google.github.io/styleguide/javaguide.html
- **Javadoc on all classes and public methods.**
- **Tests for all game rules.** Core domain logic must be unit tested without Swing.

## Architecture Decision Records (ADRs)

- ADR files live in `docs/adr/` and follow the naming convention `ADR-NNN-short-title.md`.
- **Create or update an ADR whenever you introduce a design pattern, make a significant architectural choice, or deviate from a prior decision.**
- Write ADRs during implementation as decisions are made, not only at the end.
- Use the ADR format defined in Section 13 of `architecture_guide.md`.
- If a prior ADR is superseded, update its status to `Superseded by ADR-XXX`.

## After Completing Work

After completing any implementation phase or significant piece of work:

1. Update `docs/progress_tracker.md` — mark completed items with `[x]`.
2. Verify no `javax.swing` imports in `domain` package.
3. Run all tests and confirm they pass.
4. Check for Google Java Style Guide compliance.
5. Create or update any relevant ADRs in `docs/adr/`.

## File Priority (When Conflicts Arise)

If information conflicts between files, follow this priority order:

1. `marking_rubric.md` — highest priority
2. `game_rules.md` — game rule specification
3. `domain_model.md` — domain entity relationships
4. `destination_cards.md` — ticket data
5. `london_board_layout.md` — board layout data
6. `architecture_guide.md` — architecture direction

## Project-Specific Data

- 17 Locations on the London board
- 43 printed routes (27 standalone routes + 8 double-route pairs)
- 44 Transportation cards (6 colours × 6 cards + 8 Bus wild cards)
- Card colours: Blue, Green, Black, Pink, Yellow, Orange, Bus (wild)
- Player colours: Red, White, Blue, Yellow
- 20 Destination Tickets
- Route lengths: 1, 2, 3, 4
- Route scoring: 1→1, 2→2, 3→4, 4→7
- Buses per player: 17
- End-game trigger: 0–2 buses remaining at end of turn
- Longest Continuous Path bonus: +10 points (ties share)
