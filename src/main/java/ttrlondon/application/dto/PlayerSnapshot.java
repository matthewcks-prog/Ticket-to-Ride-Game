package ttrlondon.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;

/**
 * Immutable read model of a player's visible state.
 */
public final class PlayerSnapshot {
  private final String id;
  private final String name;
  private final PlayerColor color;
  private final int busesRemaining;
  private final int score;
  private final List<CardColor> handCards;
  private final List<DestinationTicketSnapshot> destinationTickets;

  /**
   * Creates a player snapshot.
   *
   * @param id player identifier
   * @param name display name
   * @param color player colour
   * @param busesRemaining buses remaining
   * @param score current score
   * @param handCards transportation cards in hand
   * @param destinationTickets destination tickets held by the player
   */
  public PlayerSnapshot(
      String id,
      String name,
      PlayerColor color,
      int busesRemaining,
      int score,
      List<CardColor> handCards,
      List<DestinationTicketSnapshot> destinationTickets) {
    this.id = Text.requireNonBlank(id, "id");
    this.name = Text.requireNonBlank(name, "name");
    this.color = Objects.requireNonNull(color, "color");
    this.busesRemaining = busesRemaining;
    this.score = score;
    this.handCards = List.copyOf(handCards);
    this.destinationTickets = List.copyOf(destinationTickets);
  }

  /** Creates a snapshot from a domain player. */
  public static PlayerSnapshot from(Player player) {
    Objects.requireNonNull(player, "player");
    return new PlayerSnapshot(
        player.id(),
        player.name(),
        player.color(),
        player.busesRemaining(),
        player.score(),
        player.hand(),
        player.tickets().stream().map(DestinationTicketSnapshot::from).toList());
  }

  /** Returns the player identifier. */
  public String id() {
    return id;
  }

  /** Returns the display name. */
  public String name() {
    return name;
  }

  /** Returns the player colour. */
  public PlayerColor color() {
    return color;
  }

  /** Returns buses remaining. */
  public int busesRemaining() {
    return busesRemaining;
  }

  /** Returns current score. */
  public int score() {
    return score;
  }

  /** Returns the player's transportation cards. */
  public List<CardColor> handCards() {
    return Collections.unmodifiableList(handCards);
  }

  /** Returns the player's destination tickets. */
  public List<DestinationTicketSnapshot> destinationTickets() {
    return Collections.unmodifiableList(destinationTickets);
  }
}
