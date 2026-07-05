package ttrlondon.infrastructure.config;

import java.util.ArrayList;
import java.util.List;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.random.ShuffleStrategy;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;

/**
 * Creates London card decks from authoritative card composition data.
 */
public final class DeckFactory {
  private DeckFactory() {}

  /**
   * Creates the 44-card London transportation deck.
   *
   * @param shuffleStrategy strategy used to shuffle the deck order
   * @return shuffled transport card deck
   */
  public static TransportCardDeck createTransportDeck(ShuffleStrategy shuffleStrategy) {
    List<CardColor> cards = new ArrayList<>();
    addCopies(cards, CardColor.BLUE, 6);
    addCopies(cards, CardColor.GREEN, 6);
    addCopies(cards, CardColor.BLACK, 6);
    addCopies(cards, CardColor.PINK, 6);
    addCopies(cards, CardColor.YELLOW, 6);
    addCopies(cards, CardColor.ORANGE, 6);
    addCopies(cards, CardColor.BUS, 8);
    return new TransportCardDeck(shuffleStrategy.shuffle(cards), shuffleStrategy);
  }

  /**
   * Creates the 20-card London destination ticket deck.
   *
   * @param board board whose locations are used as ticket endpoints
   * @param shuffleStrategy strategy used to shuffle the deck order
   * @return shuffled destination ticket deck
   */
  public static DestinationTicketDeck createDestinationTicketDeck(
      Board board, ShuffleStrategy shuffleStrategy) {
    List<DestinationTicket> tickets = createDestinationTickets(board);
    return new DestinationTicketDeck(shuffleStrategy.shuffle(tickets));
  }

  /** Creates the unshuffled London destination tickets in source-data order. */
  public static List<DestinationTicket> createDestinationTickets(Board board) {
    return List.of(
        ticket(board, "T01", BoardFactory.BAKER_STREET, BoardFactory.BIG_BEN, 7),
        ticket(board, "T02", BoardFactory.KINGS_CROSS, BoardFactory.TRAFALGAR_SQUARE, 5),
        ticket(board, "T03", BoardFactory.THE_CHARTERHOUSE, BoardFactory.PICCADILLY_CIRCUS, 6),
        ticket(board, "T04", BoardFactory.KINGS_CROSS, BoardFactory.BIG_BEN, 7),
        ticket(board, "T05", BoardFactory.KINGS_CROSS, BoardFactory.GLOBE_THEATRE, 6),
        ticket(board, "T06", BoardFactory.BRICK_LANE, BoardFactory.ELEPHANT_CASTLE, 7),
        ticket(board, "T07", BoardFactory.ST_PAULS, BoardFactory.BIG_BEN, 5),
        ticket(board, "T08", BoardFactory.BUCKINGHAM_PALACE, BoardFactory.BRICK_LANE, 11),
        ticket(board, "T09", BoardFactory.BRICK_LANE, BoardFactory.COVENT_GARDEN, 6),
        ticket(board, "T10", BoardFactory.COVENT_GARDEN, BoardFactory.TOWER_OF_LONDON, 6),
        ticket(board, "T11", BoardFactory.TOWER_OF_LONDON, BoardFactory.BIG_BEN, 7),
        ticket(board, "T12", BoardFactory.BUCKINGHAM_PALACE, BoardFactory.TOWER_OF_LONDON, 10),
        ticket(board, "T13", BoardFactory.BAKER_STREET, BoardFactory.ST_PAULS, 9),
        ticket(board, "T14", BoardFactory.GLOBE_THEATRE, BoardFactory.BUCKINGHAM_PALACE, 6),
        ticket(board, "T15", BoardFactory.HYDE_PARK, BoardFactory.TOWER_OF_LONDON, 11),
        ticket(board, "T16", BoardFactory.REGENTS_PARK, BoardFactory.BIG_BEN, 8),
        ticket(board, "T17", BoardFactory.BRITISH_MUSEUM, BoardFactory.BUCKINGHAM_PALACE, 4),
        ticket(board, "T18", BoardFactory.WATERLOO, BoardFactory.HYDE_PARK, 5),
        ticket(board, "T19", BoardFactory.KINGS_CROSS, BoardFactory.WATERLOO, 8),
        ticket(board, "T20", BoardFactory.BRICK_LANE, BoardFactory.TRAFALGAR_SQUARE, 8));
  }

  private static void addCopies(List<CardColor> cards, CardColor color, int count) {
    for (int index = 0; index < count; index++) {
      cards.add(color);
    }
  }

  private static DestinationTicket ticket(
      Board board, String id, String locationAId, String locationBId, int points) {
    Location locationA =
        board.findLocation(locationAId)
            .orElseThrow(() -> new IllegalArgumentException("unknown location id: " + locationAId));
    Location locationB =
        board.findLocation(locationBId)
            .orElseThrow(() -> new IllegalArgumentException("unknown location id: " + locationBId));
    return new DestinationTicket(id, locationA, locationB, points);
  }
}
