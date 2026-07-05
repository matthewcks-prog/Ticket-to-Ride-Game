# Rules Crosscheck Audit

Date: 2026-05-30

Scope: Active repository only. The `sprint1` folder was not read or considered.

## Result

The active implementation matches the rule baseline in `games_rules_crosscheck.md` and
`game_rules.md` for the pre-Sprint 3 foundation. The only material presentation gap was that the
blind transportation deck existed as logic but was exposed as a small clickable count label. It is
now an explicit Blind Deck control in `CardMarketPanel`.

## Data Checks

| Area | Expected | Implementation |
|---|---:|---:|
| Locations | 17 | `BoardFactory.createLondonLocations()` creates 17 |
| Printed routes | 43 | `BoardFactory.createLondonRoutes()` creates 43 |
| Double-route pairs | 8 | `doubleGroupId` values D1-D8, each with 2 routes |
| Transportation cards | 44 | `DeckFactory.createTransportDeck()` creates 6 each regular colour + 8 Bus |
| Destination tickets | 20 | `DeckFactory.createDestinationTickets()` creates T01-T20 |
| Buses per player | 17 | `Player.STARTING_BUSES = 17` |
| Route scoring | 1, 2, 4, 7 | `RouteScoreTable.pointsForLength()` |

## Rule Checks

| Rule | Status |
|---|---|
| Draw up to two transportation cards | Implemented by `GameApplicationService` draw state |
| Blind deck draw | Implemented by `DrawTransportCardCommand.blind()` and visible in `CardMarketPanel` |
| Face-up Bus ends draw action | Implemented by `GameApplicationService.shouldCompleteAfter()` |
| Face-up Bus cannot be second draw | Implemented by draw-state validation |
| Replacement Bus slot lock | Implemented by `lockedFaceUpIndex` |
| Three-Bus face-up flush | Implemented by `FaceUpDisplay.enforceBusFlush()` |
| Grey route same-colour set | Implemented by `GreyRouteRequirement` |
| Double-route restrictions | Implemented by `Route.canBeClaimed()` |
| Endgame trigger at 0-2 buses | Implemented by `TurnManager.endTurn()` |
| One final turn each, including triggering player | Implemented by `TurnManager.finalTurnsRemaining` |
| Ticket completion by connectivity | Implemented by `TicketCompletionChecker` |
| Longest Continuous Path +10, ties share | Implemented by `LongestPathCalculator` and `ScoreCalculator` |
| London district bonus scoring | Intentionally not implemented; district data is retained for rendering/future variants |

## Architecture Checks

- No Swing/AWT imports were found in `domain`, `application`, or `infrastructure`.
- Swing panels render snapshots and submit commands; they do not score routes, validate payments, or
  mutate domain objects directly.
- Rule data is centralised in factories, with board/ticket reference data mirrored in the Markdown
  specification files.
- `docs/design/` now separates OO principles, pattern rationale, and quality gates to reduce future
  documentation drift.
