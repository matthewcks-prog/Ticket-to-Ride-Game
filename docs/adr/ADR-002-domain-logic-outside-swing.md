# ADR-002: Domain Logic Outside Swing

## Status
Accepted

## Context
Route claiming, card supply behaviour, destination ticket drawing, route scoring, and final-round
sequencing are core game rules. The assignment requires these rules to be unit tested without
opening a Swing window, and future phases will add Swing as a presentation layer.

Putting validation in panels or event listeners would make the UI responsible for deciding legal
moves, removing cards, decrementing buses, and advancing final-round state. That would couple rules
to rendering and make later UI changes risky.

## Decision
Keep all rule enforcement in the domain layer:

- `Route`, `RouteRequirement`, and `Player` enforce route claim legality, payment, card spending,
  buses, and claim state.
- `TransportCardDeck` and `FaceUpDisplay` enforce deck exhaustion, discard reshuffling, slot refill,
  and the three-Bus flush rule.
- `DestinationTicketDeck` enforces ticket draw and bottom-return semantics.
- `TurnManager` owns clockwise turn order and final-round countdown.
- `RouteScoreTable` owns the London route length scoring table.

Swing classes may later ask the application layer to perform actions, but they must not duplicate or
override these rules.

## Alternatives Considered
1. Validate rules in Swing event handlers - rejected because it violates the architecture guide and
   makes rule tests depend on GUI setup.
2. Put all rules in a single controller or manager - rejected because route validation, card supply,
   tickets, scoring, and turns have different responsibilities and would form a God Class.
3. Store only data in domain objects and validate externally - rejected because it creates an
   anaemic model where external code inspects and mutates `Player`, `Route`, and deck internals.

## Consequences
Positive: Core rules can be tested with JUnit without Swing or file I/O.
Positive: UI phases can remain thin and can display `CommandResult` messages instead of deciding
move legality.
Positive: Responsibilities remain cohesive and easier to compare against the architecture guide.

Negative: The domain exposes more behavioural methods, so command/application code must delegate
carefully instead of manipulating state directly.
