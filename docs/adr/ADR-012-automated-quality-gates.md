# ADR-012: Automated Quality Gates

## Status
Accepted

## Context
The architecture guide requires clean layer boundaries, Google Java Style compliance, and
repeatable evidence that tests pass. Manual review is useful, but it is easy for future changes to
accidentally add Swing imports to the domain/application layers or drift away from the style guide.

## Decision
Automate the most important quality gates:

- Add `ArchitectureBoundaryTest` to fail when `domain`, `application`, or `infrastructure` import
  Swing/AWT.
- Add layer-direction checks so `domain` cannot import `application`, `infrastructure`, or `ui`, and
  non-UI layers cannot depend on `ui`.
- Run Google Checkstyle from Maven and bind it to the `verify` phase.
- Treat `mvn verify` as the pre-merge command because it compiles, tests, packages, and checks
  style in one repeatable command.

## Alternatives Considered
1. Keep quality checks as manual review only - rejected because architectural regressions should be
   caught by the build, not by memory.
2. Add a custom shell script for checks - rejected because Maven is already the project build tool
   and keeps the verification path portable across IDE and command-line use.
3. Only check production source style - rejected because test code demonstrates design quality too
   and should remain readable and consistent.

## Consequences
Positive: Package-boundary, dependency-direction, and style regressions fail fast during normal
verification.
Positive: The architecture checklist now has executable support instead of relying only on
inspection.
Positive: Contributors have one clear command, `mvn verify`, for pre-merge confidence.

Negative: First-time verification may download Checkstyle dependencies, so the first run can be
slower than later runs.
