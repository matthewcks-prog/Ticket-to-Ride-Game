# ADR-009: Factory Pattern for Game Setup

## Status
Accepted

## Context
London setup requires a large amount of authoritative data: 17 locations, 40 printed routes, 44
Transportation cards, 20 Destination Tickets, initial player hands, the face-up display, and turn
order. Scattering that setup across UI or tests would duplicate data and risk rule drift from
`london_board_layout.md` and `destination_cards.md`.

The architecture guide also requires data-driven setup and constructor dependency injection. Future
Sprint 3 work may need alternate maps, controlled decks, or deterministic setup for automated tests.

## Decision
Centralise setup in infrastructure factories:

- `BoardFactory` creates the London `Board`, including route requirement strategies and double-route
  group identifiers.
- `DeckFactory` creates the London Transportation and Destination Ticket decks from the official
  card composition and ticket data.
- `GameFactory` coordinates full setup: players, shuffled decks, initial Transportation cards,
  face-up display with Bus flush enforcement, initial Destination Tickets, and `TurnManager`.
- `TestGameFactory` lives in test support and creates small deterministic games for focused tests.

The factories depend on domain constructors and injected domain `ShuffleStrategy` instances rather than
hidden global state. The default game setup keeps both initially drawn Destination Tickets, which is
a legal keep-at-least-one choice until a later setup UI can collect player choices.

## Alternatives Considered
1. Put setup data directly in `Game` -- rejected because `Game` would become a large aggregate plus
   data loader, weakening cohesion.
2. Load Markdown files at runtime -- rejected for this phase because the authoritative files are
   stable and the current domain tests should not depend on file I/O.
3. Build the board inside Swing rendering classes -- rejected because board data is game state, not
   rendering behaviour, and the UI must remain replaceable.

## Consequences
Positive: London data has one production source and can be tested independently from UI.
Positive: Tests can inject `FixedOrderShuffleStrategy` for deterministic setup.
Positive: Future alternate board or deck sources can be introduced behind factory or data-source
boundaries without changing the domain model.

Negative: Factory code must be kept in sync when authoritative Markdown data changes.
Negative: Initial ticket choice is temporarily conservative; a later UI/application setup flow should
allow players to return one ticket during setup.
