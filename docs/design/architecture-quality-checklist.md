# Architecture Quality Checklist

Use this supporting checklist before Sprint 3 feature work and before submission. The canonical
architecture guide remains `architecture_guide.md` at the repository root.

## Rule Accuracy

- [ ] Cross-check changes against `games_rules_crosscheck.md`.
- [ ] Keep the +10 Longest Continuous Path bonus as the final bonus rule.
- [ ] Do not implement London district bonus scoring unless the design rationale is updated first.
- [ ] Preserve route scoring: 1 -> 1, 2 -> 2, 3 -> 4, 4 -> 7.
- [ ] Preserve the 3+ Bus face-up flush rule and second-draw Bus restrictions.

## Architecture

- [ ] No Swing/AWT imports in `domain`, `application`, or `infrastructure`.
- [ ] UI submits commands and renders snapshots only.
- [ ] Domain objects retain meaningful behaviour.
- [ ] No broad `GameManager` or controller owns unrelated responsibilities.
- [ ] New dependencies are passed explicitly by constructor.

## Sprint 3 Extension Readiness

- [ ] New route types use a rule abstraction instead of UI checks.
- [x] Undo work captures domain/application state without exposing mutable internals.
- [ ] New scoring rules are composed into scoring, not hard-coded into UI.
- [ ] New map data is introduced through factories or data-source adapters.

## Verification

- [ ] Run `mvn verify` or the documented IntelliJ Maven command.
- [ ] Run Javadoc generation when public APIs change.
- [ ] Add focused tests for every new rule.
- [ ] Update `docs/progress_tracker.md`.
- [ ] Add or update ADRs for significant architectural decisions.
