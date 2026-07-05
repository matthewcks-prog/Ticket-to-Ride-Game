package ttrlondon.application.dto;

import ttrlondon.domain.common.Text;
import ttrlondon.domain.rushhour.RushHourEvent;

/**
 * Immutable read model for a Rush Hour event.
 */
public record RushHourEventSnapshot(
    String id, String title, String description, int extraCardCost, int bonusPoints) {
  /** Creates a Rush Hour event snapshot. */
  public RushHourEventSnapshot {
    id = Text.requireNonBlank(id, "id");
    title = Text.requireNonBlank(title, "title");
    description = Text.requireNonBlank(description, "description");
    if (extraCardCost < 0 || bonusPoints < 0) {
      throw new IllegalArgumentException("Rush Hour values must not be negative");
    }
  }

  /** Creates a snapshot from a domain event. */
  public static RushHourEventSnapshot from(RushHourEvent event) {
    return new RushHourEventSnapshot(
        event.id(), event.title(), event.description(), event.extraCardCost(), event.bonusPoints());
  }
}
