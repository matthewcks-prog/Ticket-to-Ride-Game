package ttrlondon.domain.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Player state and behaviours that naturally belong to a participant.
 */
public final class Player {
  public static final int STARTING_BUSES = 17;

  private final String id;
  private final String name;
  private final PlayerColor color;
  private final List<CardColor> hand;
  private final List<DestinationTicket> tickets;
  private int busesRemaining;
  private int score;

  /**
   * Creates a player with the London starting bus and score values.
   *
   * @param id stable player identifier
   * @param name display name
   * @param color player bus colour
   */
  public Player(String id, String name, PlayerColor color) {
    this.id = Text.requireNonBlank(id, "id");
    this.name = Text.requireNonBlank(name, "name");
    this.color = Objects.requireNonNull(color, "color");
    this.hand = new ArrayList<>();
    this.tickets = new ArrayList<>();
    this.busesRemaining = STARTING_BUSES;
    this.score = 0;
  }

  /** Returns the player identifier. */
  public String id() {
    return id;
  }

  /** Returns the player display name. */
  public String name() {
    return name;
  }

  /** Returns the player's bus colour. */
  public PlayerColor color() {
    return color;
  }

  /** Returns an immutable view of the transportation cards in hand. */
  public List<CardColor> hand() {
    return Collections.unmodifiableList(hand);
  }

  /** Returns an immutable view of destination tickets held by the player. */
  public List<DestinationTicket> tickets() {
    return Collections.unmodifiableList(tickets);
  }

  /** Returns the number of buses remaining. */
  public int busesRemaining() {
    return busesRemaining;
  }

  /** Returns the player's current score. */
  public int score() {
    return score;
  }

  /** Adds transportation cards to the player's hand. */
  public void addCards(List<CardColor> cards) {
    hand.addAll(List.copyOf(cards));
  }

  /** Adds destination tickets to the player. */
  public void addTickets(List<DestinationTicket> newTickets) {
    tickets.addAll(List.copyOf(newTickets));
  }

  /** Returns whether this player can afford the supplied payment. */
  public boolean canAfford(CardPayment payment) {
    Objects.requireNonNull(payment, "payment");
    List<CardColor> remaining = new ArrayList<>(hand);
    for (CardColor card : payment.cards()) {
      if (!remaining.remove(card)) {
        return false;
      }
    }
    return true;
  }

  /** Spends the supplied payment from this player's hand. */
  public void spendCards(CardPayment payment) {
    Objects.requireNonNull(payment, "payment");
    if (!canAfford(payment)) {
      throw new IllegalArgumentException("player cannot afford payment");
    }
    for (CardColor card : payment.cards()) {
      hand.remove(card);
    }
  }

  /** Uses buses to claim a route. */
  public void useBuses(int count) {
    if (count <= 0) {
      throw new IllegalArgumentException("bus count must be positive");
    }
    if (count > busesRemaining) {
      throw new IllegalArgumentException("not enough buses remaining");
    }
    busesRemaining -= count;
  }

  /** Adds points to the player's score. */
  public void addScore(int points) {
    score += points;
  }

  /**
   * Restores this player's mutable state from a trusted game memento.
   *
   * @param restoredHand transportation cards to hold after restore
   * @param restoredTickets destination tickets to hold after restore
   * @param restoredBusesRemaining buses remaining after restore
   * @param restoredScore score after restore
   */
  public void restoreState(
      List<CardColor> restoredHand,
      List<DestinationTicket> restoredTickets,
      int restoredBusesRemaining,
      int restoredScore) {
    Objects.requireNonNull(restoredHand, "restoredHand");
    Objects.requireNonNull(restoredTickets, "restoredTickets");
    if (restoredBusesRemaining < 0 || restoredBusesRemaining > STARTING_BUSES) {
      throw new IllegalArgumentException("restored buses remaining is out of range");
    }
    hand.clear();
    hand.addAll(List.copyOf(restoredHand));
    tickets.clear();
    tickets.addAll(List.copyOf(restoredTickets));
    busesRemaining = restoredBusesRemaining;
    score = restoredScore;
  }
}
