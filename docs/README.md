# Documentation Index

This folder contains supporting design, planning, and verification records.

## Canonical Root Documents

The following files intentionally remain at the repository root because `AGENTS.md`, the marking
workflow, and the project README refer to them directly:

- `architecture_guide.md` - authoritative architecture and design guide.
- `game_rules.md` - rule specification used by implementation and tests.
- `domain_model.md` - domain concepts, relationships, and modelling rationale.
- `class_diagram.md` - implementation-level UML view.
- `design_rationale.md` - design alternatives and trade-off discussion.
- `destination_cards.md` - destination ticket data.
- `london_board_layout.md` - board data and rendering guidance.

There is no second architecture guide or second AI-agent instruction source in `docs/`. Files under
`docs/design/` are supporting checklists, quality notes, refactoring guidance, and defence notes,
not replacements for `AGENTS.md` or `architecture_guide.md`.

## Supporting Folders

- `adr/` - Architecture Decision Records.
- `design/` - focused OO principles, pattern rationale, quality scenarios, refactoring guidance,
  and assignment-defence notes.

## Active Planning Files

- `implementation_plan.md` - current Sprint 3 readiness and implementation plan.
- `progress_tracker.md` - current status and next work.
- `known_defects.md` - known defects and residual risks.
- `rules_crosscheck_audit.md` - pre-Sprint 3 rule alignment audit.
- `git-branching-evidence.md` - Sprint 3 branch and merge history for assessment evidence.

## Supporting Guidance Policy

`AGENTS.md` is the single operational source of truth for AI coding agents. Supporting design notes
under `docs/design/` intentionally exclude raw lecture material that duplicates `AGENTS.md`,
`architecture_guide.md`, or existing ADRs. Use them when preparing future refactoring evidence,
quality evidence, and report/viva explanations.
