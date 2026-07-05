# Sprint 3 Ferry Extension Implementation Plan

## Summary

The required Sprint 3 Ferry extension is implemented by converting selected existing London
Thames routes into ferry routes. The board keeps its 43 printed routes and existing topology, but
some routes now have mandatory Bus-symbol payment requirements.

Selected ferry routes:

| Route | Endpoints | Colour | Length | Required Bus symbols |
|---|---|---|---:|---:|
| R28 | Big Ben to Waterloo | Blue | 1 | 1 |
| R39 | Globe Theatre to Tower of London | Grey | 3 | 1 |
| R42 | St Paul's to Globe Theatre | Grey | 1 | 1 |
| R43 | St Paul's to Globe Theatre | Grey | 1 | 1 |

## Rule Interpretation

- The Sprint 3 "Locomotive" rule maps to the existing London Bus wild card.
- Each required Bus symbol must be paid with either one Bus card or any three Transportation
  cards used as a substitute bundle.
- Cards used in a three-card substitute bundle satisfy only the Bus symbol. They do not also count
  toward the remaining route spaces.
- Remaining route spaces still follow the normal coloured or grey route rules.
- Extra Bus cards may be used as wild cards for the remaining route spaces after required Bus
  symbols are satisfied.
- A ferry claim places plastic buses equal to the printed route length, not the total number of
  cards paid.

## Implementation Approach

- Model ferries as another `RouteRequirement` strategy in the domain layer.
- Keep Swing thin: UI receives ferry metadata through immutable snapshots and renders indicators,
  but does not decide whether a ferry payment is valid.
- Preserve the normal `ClaimRouteCommand` flow for route availability, affordability, discarding,
  bus placement, scoring, double-route restrictions, turn advancement, and state publishing.
- Expose route metadata with `RouteKind` and `requiredBusSymbols` so future route types can be
  added without relying on route IDs in UI code.

## UI Asset Acknowledgement

A small generated bitmap icon is used for ferry/Bus-symbol markers in the route renderer:
`src/main/resources/ttrlondon/ui/ferry-symbol.png`.

The asset was created with Generative AI for Sprint 3 UI artwork and is decorative only. The game
rules remain in domain/application code.

## Test Plan

- Unit-test ferry requirements for direct Bus-symbol payment, extra Bus wild-card payment,
  three-card substitute bundles, invalid substitute counts, mixed grey-route remainder payments,
  and coloured-route mismatches.
- Command-test successful and failed ferry claims for state mutation boundaries, affordability,
  scoring, bus use, discard behaviour, and turn advancement.
- Verify double-route restrictions still apply to ferry routes `R42` and `R43`.
- Snapshot/view-model-test that ferry metadata is exposed without Swing rule checks.
- Run the architecture boundary tests and full Maven verification after implementation.
