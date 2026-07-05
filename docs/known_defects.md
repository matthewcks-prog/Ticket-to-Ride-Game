# Known Defects

## Status

No known critical defects are open as of 2026-06-01.

## Residual Risks

- The Swing UI has been verified by compile-time tests, component-level tests for the blind deck
  control, and architecture inspection, but automated end-to-end GUI tests are not included.
- Google Java Style Guide compliance is enforced by Maven Checkstyle during `verify`.
- Board route hit detection is data-driven and covered by view-model tests, but pixel-perfect visual regression testing is not automated.
