package ttrlondon.application.dto;

import java.util.Objects;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Immutable read model of a destination ticket.
 */
public final class DestinationTicketSnapshot {
  private final String id;
  private final String locationAId;
  private final String locationADisplayName;
  private final String locationBId;
  private final String locationBDisplayName;
  private final int points;

  /**
   * Creates a destination ticket snapshot.
   *
   * @param id ticket identifier
   * @param locationAId first endpoint identifier
   * @param locationADisplayName first endpoint display name
   * @param locationBId second endpoint identifier
   * @param locationBDisplayName second endpoint display name
   * @param points ticket point value
   */
  public DestinationTicketSnapshot(
      String id,
      String locationAId,
      String locationADisplayName,
      String locationBId,
      String locationBDisplayName,
      int points) {
    this.id = Text.requireNonBlank(id, "id");
    this.locationAId = Text.requireNonBlank(locationAId, "locationAId");
    this.locationADisplayName = Text.requireNonBlank(locationADisplayName, "locationADisplayName");
    this.locationBId = Text.requireNonBlank(locationBId, "locationBId");
    this.locationBDisplayName = Text.requireNonBlank(locationBDisplayName, "locationBDisplayName");
    this.points = points;
  }

  /** Creates a snapshot from a domain destination ticket. */
  public static DestinationTicketSnapshot from(DestinationTicket ticket) {
    Objects.requireNonNull(ticket, "ticket");
    return new DestinationTicketSnapshot(
        ticket.id(),
        ticket.locationA().id(),
        ticket.locationA().displayName(),
        ticket.locationB().id(),
        ticket.locationB().displayName(),
        ticket.points());
  }

  /** Returns the ticket identifier. */
  public String id() {
    return id;
  }

  /** Returns the first endpoint identifier. */
  public String locationAId() {
    return locationAId;
  }

  /** Returns the first endpoint display name. */
  public String locationADisplayName() {
    return locationADisplayName;
  }

  /** Returns the second endpoint identifier. */
  public String locationBId() {
    return locationBId;
  }

  /** Returns the second endpoint display name. */
  public String locationBDisplayName() {
    return locationBDisplayName;
  }

  /** Returns the ticket point value. */
  public int points() {
    return points;
  }
}
