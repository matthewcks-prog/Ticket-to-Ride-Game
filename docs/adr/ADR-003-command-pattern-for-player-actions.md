# ADR-003: Command Pattern for Player Actions

## Status
Accepted

## Context
The UI phases need a way to express player intentions without validating game rules in Swing event
handlers. Route claims, transportation-card draws, and destination-ticket draws each carry different
input data and validation concerns, but they should all be executed through the same application
boundary.

The architecture guide requires commands so actions can be logged, tested, and reused by Swing,
tests, or future automated players. The application layer also needs to return meaningful validation
messages instead of letting invalid actions crash or silently fail.

## Decision
Represent player actions with the `GameCommand` interface and concrete command objects:

- `ClaimRouteCommand` stores player id, route id, and `CardPayment`.
- `DrawTransportCardCommand` stores player id and the selected draw source.
- `DrawDestinationTicketsCommand` stores player id and the selected tickets to keep.

`GameApplicationService` accepts commands, delegates rule decisions to the domain model, and
publishes snapshots after successful execution. The transport-card command exposes draw details to
the service so the service can coordinate the temporary two-card draw sequence without storing that
ephemeral state in the domain.

## Alternatives Considered
1. Put action methods directly on Swing controllers - rejected because UI code would become tightly
   coupled to rule execution and validation.
2. Use one large application method with many optional parameters - rejected because each action has
   different required data and optional parameters would hide invalid command shapes.
3. Put per-turn transportation draw state in `Game` - rejected because the state exists only while a
   UI/application use case is being resolved and does not belong to long-lived domain state.

## Consequences
Positive: UI clients can express actions as explicit, testable objects.
Positive: Validation failures return `CommandResult` messages that the UI can display directly.
Positive: Command tests can exercise use cases without Swing.

Negative: There are more small classes in the application layer.
Negative: The transportation-card command and service need a small detailed result object so the
service can apply second-draw Bus restrictions correctly.
