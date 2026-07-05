# ADR-005: Strategy Pattern for Route Requirement

## Status
Accepted

## Context
Route payments differ by printed route colour. Coloured routes require a specific card colour plus
optional Bus wild cards. Grey routes allow any one non-Bus colour set plus optional Bus wild cards.

This variation must stay outside the UI and should not be implemented as repeated colour checks in
controllers or panels. The architecture guide also requires route validation to use polymorphism
instead of branching on route type.

## Decision
Represent payment validation with the `RouteRequirement` strategy interface:

- `ColouredRouteRequirement` validates fixed-colour routes.
- `GreyRouteRequirement` validates neutral routes.
- `Route` composes a `RouteRequirement` and calls `isSatisfiedBy(payment)` when checking claim
  legality.

`CardPayment` provides small query methods such as `hasSingleNonBusColor()` and
`nonBusCardsMatch(...)`, allowing each strategy to express its own rule without exposing mutable
player hand state.

## Alternatives Considered
1. Put `if (route.color() == GREY)` inside `Route` - rejected because every new requirement variant
   would make `Route` larger and less cohesive.
2. Create `GreyRoute` and `ColouredRoute` subclasses - rejected because the variation is payment
   policy, not route identity or lifecycle.
3. Validate route colour in the application layer - rejected because rule logic must remain in the
   domain and be testable without commands or UI.

## Consequences
Positive: Route validation is open to additional requirement types without rewriting `Route`.
Positive: The implementation directly supports deterministic unit tests of each route requirement.
Positive: `Route` remains responsible for claim state and double-route availability while delegating
payment policy to the composed strategy.

Negative: Factory code in Phase 3 must construct the correct requirement strategy for each route.
