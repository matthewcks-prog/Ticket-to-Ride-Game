package ttrlondon.domain.rushhour;

import ttrlondon.domain.board.Route;

/**
 * Strategy for deciding whether a Rush Hour event affects a route.
 */
public interface RouteSelector {
  /**
   * Returns whether the supplied route is affected by this selector.
   *
   * @param route candidate route
   * @return true when the route is affected
   */
  boolean matches(Route route);
}
