package ttrlondon.infrastructure.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;

/**
 * Pre-game setup state after decks have been shuffled and initial cards have been dealt.
 */
public final class GameSetupDraft {
  private final Board board;
  private final List<Player> players;
  private final TransportCardDeck transportCardDeck;
  private final FaceUpDisplay faceUpDisplay;
  private final DestinationTicketDeck destinationTicketDeck;
  private final Map<String, List<DestinationTicket>> initialTicketOptionsByPlayerId;

  /**
   * Creates a setup draft.
   *
   * @param board London board
   * @param players players in clockwise order
   * @param transportCardDeck shuffled transportation deck after initial hand deal
   * @param faceUpDisplay initial face-up display
   * @param destinationTicketDeck destination ticket deck after initial ticket deal
   * @param initialTicketOptionsByPlayerId initial ticket options by player identifier
   */
  public GameSetupDraft(
      Board board,
      List<Player> players,
      TransportCardDeck transportCardDeck,
      FaceUpDisplay faceUpDisplay,
      DestinationTicketDeck destinationTicketDeck,
      Map<String, List<DestinationTicket>> initialTicketOptionsByPlayerId) {
    this.board = Objects.requireNonNull(board, "board");
    this.players = List.copyOf(players);
    this.transportCardDeck = Objects.requireNonNull(transportCardDeck, "transportCardDeck");
    this.faceUpDisplay = Objects.requireNonNull(faceUpDisplay, "faceUpDisplay");
    this.destinationTicketDeck =
        Objects.requireNonNull(destinationTicketDeck, "destinationTicketDeck");
    this.initialTicketOptionsByPlayerId = copyTicketOptions(initialTicketOptionsByPlayerId);
  }

  /** Returns the board. */
  public Board board() {
    return board;
  }

  /** Returns players in clockwise order. */
  public List<Player> players() {
    return Collections.unmodifiableList(players);
  }

  /** Returns the transportation card deck. */
  public TransportCardDeck transportCardDeck() {
    return transportCardDeck;
  }

  /** Returns the face-up display. */
  public FaceUpDisplay faceUpDisplay() {
    return faceUpDisplay;
  }

  /** Returns the destination ticket deck. */
  public DestinationTicketDeck destinationTicketDeck() {
    return destinationTicketDeck;
  }

  /** Returns initial destination ticket options by player identifier. */
  public Map<String, List<DestinationTicket>> initialTicketOptionsByPlayerId() {
    return initialTicketOptionsByPlayerId;
  }

  private static Map<String, List<DestinationTicket>> copyTicketOptions(
      Map<String, List<DestinationTicket>> ticketOptions) {
    Objects.requireNonNull(ticketOptions, "ticketOptions");
    Map<String, List<DestinationTicket>> copied = new LinkedHashMap<>();
    for (Map.Entry<String, List<DestinationTicket>> entry : ticketOptions.entrySet()) {
      copied.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Collections.unmodifiableMap(copied);
  }
}
