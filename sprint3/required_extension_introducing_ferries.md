# Sprint 3 Ferry Extension Plan

## Summary
Implement the required Ferry extension as a first-class route-payment rule in the domain model, with no Swing-owned game logic.

Use the chosen approach: convert selected existing London Thames routes into ferry routes, preserving the 43-route board topology while adding ferry payment behaviour. Use a generated transparent ferry/Bus-symbol icon for UI route slots and acknowledge it in Sprint 3 documentation.

Selected ferry routes:
- `R28` Big Ben to Waterloo, Blue length 1, `1` required Bus symbol
- `R39` Globe Theatre to Tower of London, Grey length 3, `1` required Bus symbol
- `R42` St Paul’s to Globe Theatre, Grey length 1, `1` required Bus symbol
- `R43` St Paul’s to Globe Theatre, Grey length 1, `1` required Bus symbol

## Documentation First
- Add `sprint3/ferries_implementation_plan.md` with the final implementation plan, rule interpretation, test plan, and UI asset acknowledgement.
- Amend `game_rules.md` to define ferry route payment:
  - Each ferry has `requiredBusSymbols`.
  - A player must satisfy each symbol with either `1` Bus card or any `3` non-dedicated cards.
  - Extra Bus cards may still act as wild cards for remaining coloured/grey spaces.
- Amend `london_board_layout.md` to mark the selected route IDs as ferries and list their Bus-symbol counts.
- Amend `domain_model.md`, `architecture_guide.md`, and `design_rationale.md` only enough to reflect Ferry Route / Ferry Route Requirement as an extension of the existing `RouteRequirement` Strategy.
- Add `docs/adr/ADR-013-ferry-route-requirement-strategy.md` before implementation. Decision: model ferries as another `RouteRequirement` strategy, not as Swing conditionals, route subclasses, or duplicated command logic.
- Update `docs/progress_tracker.md` after implementation to mark the Ferry extension completed and record verification results.

## Key Implementation Changes
- Extend route metadata without replacing the current model:
  - Add a route category/type such as `RouteKind.STANDARD` / `FERRY`, or equivalent immutable metadata on `Route`.
  - Add `requiredBusSymbols` for ferry routes, defaulting to `0` for standard routes.
  - Expose ferry metadata through `RouteSnapshot` and `BoardRouteViewModel` so Swing can render it without deciding rules.
- Add `FerryRouteRequirement implements RouteRequirement`.
  - It should compose/reuse the same coloured/grey payment concepts instead of duplicating all existing route logic.
  - It must support variable payment sizes because fallback payments can exceed route length.
  - It must validate remaining route spaces after mandatory Bus-symbol requirements are satisfied.
- Refine `CardPayment` with query methods needed by ferry validation, such as safe counts, removable copies, or grouped counts. Keep it immutable.
- Update `ClaimRouteCommand` validation and messages:
  - Stop assuming every valid payment has exactly `route.length()` cards.
  - Keep all affordability, discard, bus-piece, score, turn, and double-route handling in the same command path.
  - Add specific failure messages for missing required Bus symbols and invalid 3-card substitutes.
- Update `BoardFactory` so only the selected route IDs use `FerryRouteRequirement`; all other routes remain unchanged.
- Update Swing rendering only from snapshots:
  - Draw generated ferry/Bus icons on ferry route slots.
  - Show ferry metadata in route selection/payment UI so players understand required Bus symbols.
  - Do not validate ferry payments in Swing beyond collecting the player’s selected cards and submitting a command.

## Test Plan
- Domain tests for `FerryRouteRequirement`:
  - `R39`-style grey length 3 ferry accepts `BUS + two same-colour cards`.
  - Accepts `BUS + BUS + colour` where the extra Bus substitutes for a remaining route space.
  - Accepts `three substitute cards + two same-colour remaining cards` for one missing required Bus symbol.
  - Rejects no Bus and fewer than three substitute cards.
  - Rejects mixed non-Bus colours in remaining grey/coloured route spaces.
  - Rejects payments that satisfy Bus symbols but not remaining route colour rules.
- Command/application tests:
  - Successful ferry claim discards all paid cards, uses only `route.length()` plastic buses, awards normal route score, claims route, and advances turn.
  - Failed ferry claim leaves player hand, route ownership, discard pile, score, buses, and turn unchanged.
  - Double-route restrictions still apply to ferry routes `R42`/`R43`.
  - Player affordability checks include all fallback cards, not only route-length cards.
- Snapshot/UI tests:
  - Ferry route metadata appears in `RouteSnapshot` and `BoardRouteViewModel`.
  - Board rendering can identify ferry routes without importing domain logic into Swing.
- Quality gates:
  - Run architecture boundary test confirming no Swing/AWT imports outside `ui`.
  - Run full Maven verification using the documented IntelliJ Maven command.
  - Check Google Java Style and Javadoc on new public classes/methods.

## Assumptions
- “Locomotive” in the Sprint 3 spec maps to the existing London `CardColor.BUS` wild card.
- Ferry conversion preserves the London board’s existing 43 printed routes; route count does not change.
- Ferry fallback cards are discarded as part of the same route claim payment.
- Generated UI artwork is limited to a small transparent ferry/Bus-symbol marker and must be acknowledged in Sprint 3 documentation.
