package ttrlondon.domain.board;

import java.util.Objects;
import ttrlondon.domain.common.Text;

/**
 * Immutable London board location used as route and ticket endpoint.
 */
public final class Location {
  private final String id;
  private final String displayName;
  private final int district;

  /**
   * Creates a location.
   *
   * @param id stable machine-readable location identifier
   * @param displayName player-facing location name
   * @param district board district number
   */
  public Location(String id, String displayName, int district) {
    this.id = Text.requireNonBlank(id, "id");
    this.displayName = Text.requireNonBlank(displayName, "displayName");
    this.district = district;
  }

  /** Returns the stable location identifier. */
  public String id() {
    return id;
  }

  /** Returns the player-facing location name. */
  public String displayName() {
    return displayName;
  }

  /** Returns the board district number. */
  public int district() {
    return district;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Location location)) {
      return false;
    }
    return id.equals(location.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
