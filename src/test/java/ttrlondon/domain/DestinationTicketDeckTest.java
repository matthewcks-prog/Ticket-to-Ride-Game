package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;

/** Tests destination ticket draw and bottom-return rules. */
final class DestinationTicketDeckTest {
  private static final Location A = new Location("A", "A", 1);
  private static final Location B = new Location("B", "B", 1);

  @Test
  void drawForTurnDrawsUpToTwoTicketsAndCanKeepBoth() {
    DestinationTicket first = ticket("t1");
    DestinationTicket second = ticket("t2");
    DestinationTicketDeck deck = new DestinationTicketDeck(List.of(first, second));

    List<DestinationTicket> drawn = deck.drawForTurn();
    deck.returnUnkeptToBottom(drawn, drawn);

    assertEquals(List.of(first, second), drawn);
    assertEquals(0, deck.size());
  }

  @Test
  void unkeptTicketsReturnToBottomInOriginalDrawnOrder() {
    DestinationTicket first = ticket("t1");
    DestinationTicket second = ticket("t2");
    DestinationTicket third = ticket("t3");
    DestinationTicketDeck deck = new DestinationTicketDeck(List.of(first, second, third));

    List<DestinationTicket> drawn = deck.drawForTurn();
    deck.returnUnkeptToBottom(drawn, List.of(second));

    assertEquals(List.of(third, first), deck.ticketsSnapshot());
  }

  @Test
  void drawForTurnDrawsSingleRemainingTicketAndRejectsEmptyDeck() {
    DestinationTicket first = ticket("t1");
    DestinationTicketDeck deck = new DestinationTicketDeck(List.of(first));

    assertEquals(List.of(first), deck.drawForTurn());
    assertThrows(IllegalStateException.class, deck::drawForTurn);
  }

  @Test
  void keepingZeroTicketsIsRejected() {
    DestinationTicket first = ticket("t1");
    DestinationTicket second = ticket("t2");
    DestinationTicketDeck deck = new DestinationTicketDeck(List.of(first, second));
    List<DestinationTicket> drawn = deck.drawForTurn();

    assertThrows(IllegalArgumentException.class, () -> deck.returnUnkeptToBottom(drawn, List.of()));
  }

  private static DestinationTicket ticket(String id) {
    return new DestinationTicket(id, A, B, 5);
  }
}
