# ADR-010: Setup Draft for Initial Ticket Selection

## Status
Accepted

## Context
Phase 9 requires player count, names, colours, first-player selection, and initial destination
ticket choices before play begins. The existing factory created a running game immediately and kept
both initial destination tickets for every player, which was legal but did not support the required
setup choice screen.

## Decision
Introduce `GameSetupDraft` in the infrastructure configuration layer. The draft represents the
pre-game state after decks are shuffled, starting Transportation cards are dealt, the face-up market
is prepared, and each player has been dealt two initial Destination Ticket options. The UI presents
those options, but `GameFactory` validates the keep-at-least-one rule, gives kept tickets to players,
returns unkept tickets to the bottom of the deck, and creates the running `Game`.

## Alternatives Considered
1. Put setup ticket selection directly in Swing by drawing from the deck there - rejected because it
   would move setup sequencing and ticket return rules into the UI.
2. Start the domain game in `SETUP` with pending ticket state - rejected for now because setup is a
   bounded pre-game use case and does not need to complicate the `Game` aggregate lifecycle.

## Consequences
Positive: Setup choices are now playable while the factory remains the single owner of game setup
sequencing and validation.
Negative: The setup draft is mutable through the contained domain objects, so it must remain a
short-lived pre-game object and should not be exposed after the running game is created.
