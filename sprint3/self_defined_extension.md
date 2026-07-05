Plan

1.3 Self-Defined Extension: Rush Hour Events
Summary
Implement Rush Hour Events as the self-defined extension. This changes actual turn decisions: players can see a coming congestion event, rush to claim affected routes before peak, avoid the area, or pay extra during peak for bonus points.

Use a turn-based Rush Hour Clock, not a real-time timer. Real-time Swing timers would be harder to test and less fair in a turn-based board game.

Core Gameplay
The game alternates between:
Forecast phase: one full round warning. A future Rush Hour event is visible but has no effect.
Peak phase: one full round active. Affected routes require 1 extra any-colour Transportation card to claim, but award +2 Rush Hour bonus points if claimed successfully.
A “full round” means playerCount completed turns, so it works for 2, 3, and 4 players.
The Rush Hour clock advances only when a complete turn ends, including multi-step card draws.
Newly added Ferry routes can also be affected by Rush Hour if their route id/location matches an event.

Refined Architecture Rules
Rush Hour must remain a domain/application feature. Swing may display Rush Hour state and submit
commands, but it must not decide whether a route is affected, whether detour payment is valid, or
whether bonus points are awarded.

RushHourPhase includes INACTIVE, FORECAST, and PEAK. INACTIVE represents the initial/no-active-event
state and gives tests, snapshots, undo, and future save/load a clear null-object lifecycle state.

RushHourManager remains cohesive: it owns event lifecycle, event deck/discard, current phase,
forecast event, active event, countdown, and the per-player bonus ledger. It does not perform full
route-claim validation.

Add a small RushHourClaimRule/RushHourClaimModifier collaborator that answers:
- whether the selected route is affected now;
- what extra detour payment is required;
- what bonus points are awarded on a successful claim.

This prevents ClaimRouteCommand and GameApplicationService from becoming Rush Hour rule hubs.

Key Implementation Changes
Add domain.rushhour:
RushHourPhase: INACTIVE, FORECAST, PEAK
RushHourEvent: immutable id, title, description, affected-route selector, extra-card cost, bonus points
RouteSelector: strategy for route matching by route id, endpoint location, colour, or route type
RushHourManager: owns event deck/discard, current phase, forecast event, active event, countdown, and bonus ledger
RushHourClaimRule/RushHourClaimModifier: validates Rush Hour-specific detour requirements and bonus
eligibility without owning normal route/Ferry validation
Add ClaimRoutePayment value object:
routePayment: cards satisfying normal route/Ferry requirement
rushHourDetourPayment: separate extra cards required by active Rush Hour
combinedPayment(): route and detour cards together for affordability/spending checks
Keep the existing ClaimRouteCommand(playerId, routeId, CardPayment) constructor as a no-detour convenience overload.
Add a new ClaimRouteCommand(playerId, routeId, ClaimRoutePayment) path for Rush Hour-aware claims.
During route claim validation:
Validate normal route/Ferry payment against routePayment.
If route is affected during PEAK, require exactly one detour card in rushHourDetourPayment.
The detour card may be any Transportation card colour, including Bus, but must be separate from
normal route/Ferry payment and cannot satisfy both requirements.
Ensure the player can afford the combined cards.
Discard both normal and detour cards on success.
Award normal route points plus active Rush Hour bonus points.
Rush Hour bonus points are added immediately to the player's score and tracked in the bonus ledger
for display/final-score breakdown only. Final scoring must not add them a second time.
Add Rush Hour state to GameSnapshot: phase, turns remaining, forecast event, active event, affected route ids, and per-player Rush Hour points.
Add a RushHourPanel in Swing and visually highlight affected routes from snapshots only. Swing must not decide whether a route is affected.
Event Deck
Start with 8 deterministic, data-driven events in a RushHourEventFactory:

Central Gridlock: Piccadilly Circus, Covent Garden, Trafalgar Square routes
Royal Procession: Buckingham Palace, Big Ben, Hyde Park routes
Museum Crowds: British Museum, Covent Garden, The Charterhouse routes
East End Market: Brick Lane, St Paul’s, Tower of London routes
South Bank Surge: Waterloo, Globe Theatre, Elephant & Castle routes
Northern Commuters: Regent’s Park, Baker Street, King’s Cross routes
Thames Tide: Thames-side routes plus Sprint 3 Ferry route ids
Colour Bottleneck: all grey routes
Use injected ShuffleStrategy for event deck recycling so tests stay deterministic.
Factory/data-source boundaries must create Rush Hour events. Do not hard-code event data in Swing or
GameApplicationService.

Documentation Amendments
Update sprint_3_specifications.md section 1.3 with Rush Hour Events as the chosen self-defined extension.
Update game_rules.md, architecture_guide.md, design_rationale.md, class_diagram.md, and docs/progress_tracker.md.
Remove active “district bonus scoring” guidance/checklist entries. Keep district numbers only as visual board data if still needed for rendering.
Add an ADR for Rush Hour Events, including alternatives rejected: Landmark Contracts, Passenger Delivery, and Borough Influence.
Test Plan
Rush Hour clock: forecast lasts one full round, then peak lasts one full round.
Forecast phase: affected routes do not require detour payment.
Peak phase: affected route without detour card fails and leaves state unchanged.
Peak phase: affected route with valid detour succeeds, discards all cards, claims route, advances turn, and awards +2.
Unaffected route during peak behaves normally.
The same card cannot be counted in both routePayment and rushHourDetourPayment.
Event deck recycles deterministically when exhausted.
Snapshot exposes phase, active/forecast event, affected route ids, and bonus points.
Undo integration restores Rush Hour phase, countdown, event deck, discard pile, bonus ledger, player score, route ownership, and cards.
Architecture test confirms no Swing/AWT imports outside ui.
Full Maven verification after implementation.
Assumptions
Rush Hour Events are always enabled for Sprint 3.
Rush Hour bonus points are immediate score points and must also appear in final scoring breakdown.
Rush Hour detour cards are separate from normal route/Ferry payment and cannot satisfy normal route requirements.
The mechanic should be tactical, not punitive: affected routes cost more during peak but reward players who commit to them.
