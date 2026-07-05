package ttrlondon.application.undo;

import java.util.List;
import ttrlondon.application.dto.TransportDrawProgress;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.rushhour.RushHourPhase;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Immutable snapshot of all mutable game and application state needed for undo.
 */
public record GameMemento(
    GamePhase phase,
    TurnState turnState,
    List<PlayerState> players,
    List<RouteState> routes,
    List<CardColor> transportDrawPile,
    List<CardColor> transportDiscardPile,
    List<CardColor> faceUpCards,
    List<DestinationTicket> destinationTickets,
    RushHourState rushHourState,
    TransportDrawProgress drawProgress) {

  /** Creates an immutable game memento. */
  public GameMemento {
    players = List.copyOf(players);
    routes = List.copyOf(routes);
    transportDrawPile = List.copyOf(transportDrawPile);
    transportDiscardPile = List.copyOf(transportDiscardPile);
    faceUpCards = List.copyOf(faceUpCards);
    destinationTickets = List.copyOf(destinationTickets);
  }

  /** Snapshot of one player's mutable state. */
  public record PlayerState(
      String id,
      List<CardColor> hand,
      List<DestinationTicket> tickets,
      int busesRemaining,
      int score) {
    /** Creates an immutable player-state memento. */
    public PlayerState {
      hand = List.copyOf(hand);
      tickets = List.copyOf(tickets);
    }
  }

  /** Snapshot of one route's mutable claim state. */
  public record RouteState(String id, String claimedBy) {}

  /** Snapshot of turn sequencing and final-round state. */
  public record TurnState(
      String currentPlayerId,
      boolean finalRoundActive,
      String triggeringPlayerId,
      int finalTurnsRemaining) {}

  /** Snapshot of Rush Hour mutable state. */
  public record RushHourState(
      RushHourPhase phase,
      String forecastEventId,
      String activeEventId,
      int turnsRemaining,
      List<String> eventDeck,
      List<String> eventDiscard,
      java.util.Map<String, Integer> bonusPointsByPlayerId) {
    /** Creates an immutable Rush Hour state memento. */
    public RushHourState {
      eventDeck = List.copyOf(eventDeck);
      eventDiscard = List.copyOf(eventDiscard);
      bonusPointsByPlayerId = java.util.Map.copyOf(bonusPointsByPlayerId);
    }
  }
}
