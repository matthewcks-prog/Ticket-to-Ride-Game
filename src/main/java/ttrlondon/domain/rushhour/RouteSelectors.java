package ttrlondon.domain.rushhour;

import java.util.Arrays;
import java.util.Set;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;

/**
 * Factory methods for common Rush Hour route-selector strategies.
 */
public final class RouteSelectors {
  private RouteSelectors() {}

  /** Returns a selector matching any route id in the supplied set. */
  public static RouteSelector byRouteIds(String... routeIds) {
    Set<String> ids = Set.copyOf(Arrays.asList(routeIds));
    return route -> ids.contains(route.id());
  }

  /** Returns a selector matching routes touching any supplied location id. */
  public static RouteSelector touchingLocations(String... locationIds) {
    Set<String> ids = Set.copyOf(Arrays.asList(locationIds));
    return route -> ids.contains(route.locationA().id()) || ids.contains(route.locationB().id());
  }

  /** Returns a selector matching routes with the supplied printed route colour. */
  public static RouteSelector byColor(RouteColor color) {
    return route -> route.color() == color;
  }

  /** Returns a selector matching routes with the supplied route kind. */
  public static RouteSelector byKind(RouteKind kind) {
    return route -> route.kind() == kind;
  }

  /** Returns a selector matching routes accepted by any supplied selector. */
  public static RouteSelector anyOf(RouteSelector... selectors) {
    return route -> {
      for (RouteSelector selector : selectors) {
        if (selector.matches(route)) {
          return true;
        }
      }
      return false;
    };
  }
}
