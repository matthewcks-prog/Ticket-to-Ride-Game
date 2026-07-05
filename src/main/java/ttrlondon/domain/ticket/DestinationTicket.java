package ttrlondon.domain.ticket;

import java.util.Objects;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.common.Text;

/**
 * Immutable destination ticket connecting two board locations for a point value.
 */
public final class DestinationTicket {
  private final String id;
  private final Location locationA;
  private final Location locationB;
  private final int points;

  /**
   * Creates a destination ticket.
   *
   * @param id stable ticket identifier
   * @param locationA first endpoint
   * @param locationB second endpoint
   * @param points ticket value
   */
  public DestinationTicket(String id, Location locationA, Location locationB, int points) {
    this.id = Text.requireNonBlank(id, "id");
    this.locationA = Objects.requireNonNull(locationA, "locationA");
    this.locationB = Objects.requireNonNull(locationB, "locationB");
    if (locationA.equals(locationB)) {
      throw new IllegalArgumentException("ticket endpoints must be different");
    }
    if (points <= 0) {
      throw new IllegalArgumentException("points must be positive");
    }
    this.points = points;
  }

  /** Returns the stable ticket identifier. */
  public String id() {
    return id;
  }

  /** Returns the first endpoint. */
  public Location locationA() {
    return locationA;
  }

  /** Returns the second endpoint. */
  public Location locationB() {
    return locationB;
  }

  /** Returns the ticket point value. */
  public int points() {
    return points;
  }
}
