# ADR-013: Ferry Route Requirement Strategy

## Status
Accepted

## Context
Sprint 3 requires Ferry routes. Ferry routes have special payment rules: each printed Bus symbol
requires either one Bus card or any three cards as a substitute bundle, while remaining route
spaces still use the normal coloured or grey route payment rules. The existing architecture already
uses `RouteRequirement` as a Strategy for route payment validation.

## Decision
Model ferries as a new `FerryRouteRequirement` implementation of `RouteRequirement`. Store route
metadata on `Route` with a route kind and required Bus-symbol count, and expose that metadata
through snapshots for rendering. Keep route claim orchestration in `ClaimRouteCommand` and keep
Swing limited to collecting selected cards and rendering ferry indicators.

## Alternatives Considered
1. Route subclass such as `FerryRoute` - rejected because the existing `Route` entity already
   composes behaviour through `RouteRequirement`, and subclassing would duplicate claim state.
2. Conditional ferry logic in `ClaimRouteCommand` - rejected because it would centralise payment
   variants in one command and weaken the Strategy pattern.
3. Swing-side ferry validation - rejected because it violates the rule that game logic must remain
   outside Swing.

## Consequences
Positive: ferry rules are domain-owned, testable without UI, and open to future special route
requirements.

Negative: route payment validation now allows variable card counts, so command error messages and
tests must no longer assume every valid claim uses exactly `route.length()` cards.
