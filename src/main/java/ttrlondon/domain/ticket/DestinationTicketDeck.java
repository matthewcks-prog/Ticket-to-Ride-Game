package ttrlondon.domain.ticket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Destination ticket draw pile with bottom-return semantics.
 */
public final class DestinationTicketDeck {
  private final Deque<DestinationTicket> drawPile;

  /**
   * Creates a destination ticket deck.
   *
   * @param drawPile initial ticket order, with the first element as the top ticket
   */
  public DestinationTicketDeck(List<DestinationTicket> drawPile) {
    Objects.requireNonNull(drawPile, "drawPile");
    this.drawPile = new ArrayDeque<>(drawPile);
  }

  /** Draws up to the requested number of tickets from the top of the deck. */
  public List<DestinationTicket> draw(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("ticket draw count must not be negative");
    }
    List<DestinationTicket> drawn = new ArrayList<>();
    for (int ticketCount = 0; ticketCount < count && !drawPile.isEmpty(); ticketCount++) {
      drawn.add(drawPile.removeFirst());
    }
    return Collections.unmodifiableList(drawn);
  }

  /** Draws the destination tickets available for a player ticket action. */
  public List<DestinationTicket> drawForTurn() {
    if (drawPile.isEmpty()) {
      throw new IllegalStateException("destination ticket deck is empty");
    }
    return draw(2);
  }

  /**
   * Returns unkept tickets from a draw action to the bottom of the deck.
   *
   * @param drawnTickets tickets drawn for the action
   * @param keptTickets tickets the player chose to keep
   */
  public void returnUnkeptToBottom(
      List<DestinationTicket> drawnTickets, List<DestinationTicket> keptTickets) {
    Objects.requireNonNull(drawnTickets, "drawnTickets");
    Objects.requireNonNull(keptTickets, "keptTickets");
    List<DestinationTicket> drawn = List.copyOf(drawnTickets);
    List<DestinationTicket> kept = List.copyOf(keptTickets);
    if (kept.isEmpty()) {
      throw new IllegalArgumentException("at least one destination ticket must be kept");
    }
    if (!drawn.containsAll(kept)) {
      throw new IllegalArgumentException("kept tickets must come from the drawn tickets");
    }
    List<DestinationTicket> returned = new ArrayList<>();
    for (DestinationTicket ticket : drawn) {
      if (!kept.contains(ticket)) {
        returned.add(ticket);
      }
    }
    returnToBottom(returned);
  }

  /** Returns tickets to the bottom of the deck in the supplied order. */
  public void returnToBottom(List<DestinationTicket> tickets) {
    for (DestinationTicket ticket : List.copyOf(tickets)) {
      drawPile.addLast(ticket);
    }
  }

  /** Returns the number of tickets remaining. */
  public int size() {
    return drawPile.size();
  }

  /** Returns a read-only snapshot of remaining tickets. */
  public List<DestinationTicket> ticketsSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(drawPile));
  }

  /**
   * Restores the destination ticket pile from a trusted game memento.
   *
   * @param restoredTickets ticket draw pile in top-to-bottom order
   */
  public void restoreState(List<DestinationTicket> restoredTickets) {
    Objects.requireNonNull(restoredTickets, "restoredTickets");
    drawPile.clear();
    drawPile.addAll(List.copyOf(restoredTickets));
  }
}
