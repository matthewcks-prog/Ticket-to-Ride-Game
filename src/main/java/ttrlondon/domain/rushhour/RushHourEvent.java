package ttrlondon.domain.rushhour;

import java.util.Objects;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.common.Text;

/**
 * Immutable Rush Hour event definition.
 */
public final class RushHourEvent {
  private final String id;
  private final String title;
  private final String description;
  private final RouteSelector routeSelector;
  private final int extraCardCost;
  private final int bonusPoints;

  /**
   * Creates a Rush Hour event.
   *
   * @param id stable event identifier
   * @param title display title
   * @param description short event description
   * @param routeSelector affected-route selector
   * @param extraCardCost extra detour cards required during peak
   * @param bonusPoints immediate bonus awarded for affected peak claims
   */
  public RushHourEvent(
      String id,
      String title,
      String description,
      RouteSelector routeSelector,
      int extraCardCost,
      int bonusPoints) {
    this.id = Text.requireNonBlank(id, "id");
    this.title = Text.requireNonBlank(title, "title");
    this.description = Text.requireNonBlank(description, "description");
    this.routeSelector = Objects.requireNonNull(routeSelector, "routeSelector");
    if (extraCardCost < 0) {
      throw new IllegalArgumentException("extraCardCost must not be negative");
    }
    if (bonusPoints < 0) {
      throw new IllegalArgumentException("bonusPoints must not be negative");
    }
    this.extraCardCost = extraCardCost;
    this.bonusPoints = bonusPoints;
  }

  /** Returns the event identifier. */
  public String id() {
    return id;
  }

  /** Returns the display title. */
  public String title() {
    return title;
  }

  /** Returns the event description. */
  public String description() {
    return description;
  }

  /** Returns the extra detour card cost. */
  public int extraCardCost() {
    return extraCardCost;
  }

  /** Returns the successful-claim bonus points. */
  public int bonusPoints() {
    return bonusPoints;
  }

  /** Returns whether this event affects the supplied route. */
  public boolean affects(Route route) {
    return routeSelector.matches(Objects.requireNonNull(route, "route"));
  }
}
