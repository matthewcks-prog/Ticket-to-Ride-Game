package ttrlondon.application.dto;

import java.util.Objects;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.common.Text;

/**
 * Immutable read model of a board location.
 */
public final class LocationSnapshot {
  private final String id;
  private final String displayName;
  private final int district;

  /**
   * Creates a location snapshot.
   *
   * @param id location identifier
   * @param displayName location display name
   * @param district London district number
   */
  public LocationSnapshot(String id, String displayName, int district) {
    this.id = Text.requireNonBlank(id, "id");
    this.displayName = Text.requireNonBlank(displayName, "displayName");
    this.district = district;
  }

  /** Creates a snapshot from a domain location. */
  public static LocationSnapshot from(Location location) {
    Objects.requireNonNull(location, "location");
    return new LocationSnapshot(location.id(), location.displayName(), location.district());
  }

  /** Returns the location identifier. */
  public String id() {
    return id;
  }

  /** Returns the display name. */
  public String displayName() {
    return displayName;
  }

  /** Returns the district number. */
  public int district() {
    return district;
  }
}
