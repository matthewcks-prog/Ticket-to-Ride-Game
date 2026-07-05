# ADR-015: Rush Hour Events

## Status
Accepted

## Context
Sprint 3 requires a self-defined extension that changes gameplay rather than only changing the UI.
The extension must preserve the existing layered architecture, keep game rules outside Swing, remain
testable without GUI code, and integrate with the existing Command, Strategy, Factory, Observer, and
Memento decisions.

## Decision
Implement Rush Hour Events as a turn-based event clock owned by the domain aggregate. A
`RushHourManager` owns event sequencing, forecast/peak phases, event deck recycling, countdowns, and
per-player bonus accounting. `RouteSelector` strategies define which routes each event affects.
`ClaimRoutePayment` separates normal route/Ferry payment from Rush Hour detour cards, while
`RushHourClaimRule` applies only the Rush Hour-specific claim modifier.

Swing displays Rush Hour state from immutable snapshots and highlights affected routes, but it does
not decide route effects, detour validity, or bonus scoring. Undo captures and restores Rush Hour
state through the existing Memento mechanism.

## Alternatives Considered
1. Real-time Swing timer -- rejected because it would make a turn-based board game less fair and
   harder to test deterministically.
2. Put event checks directly in `ClaimRouteCommand` -- rejected because it would make the command a
   growing rule hub as more event types are added.
3. Add detour cards into the existing `CardPayment` only -- rejected because Rush Hour cards must be
   separate from normal route/Ferry payment and must not satisfy both requirements.
4. Landmark Contracts, Passenger Delivery, and Borough Influence -- rejected for this sprint because
   Rush Hour better exercises route timing, payment, scoring, snapshots, undo, and route rendering
   while staying scoped to the existing London board.

## Consequences
Positive: Rush Hour changes actual player decisions, keeps rules outside Swing, uses explicit
extension points, and remains unit-testable with deterministic event order.

Negative: Route claiming now has a split payment value object, and the game memento must capture
additional event-clock state.
