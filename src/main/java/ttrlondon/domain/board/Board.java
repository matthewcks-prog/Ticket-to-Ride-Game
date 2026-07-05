package ttrlondon.domain.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain representation of the board map and printed routes.
 */
public final class Board {
  private final Map<String, Location> locationsById;
  private final Map<String, Route> routesById;

  /**
   * Creates a board from predefined locations and routes.
   *
   * @param locations board locations
   * @param routes printed board routes
   */
  public Board(List<Location> locations, List<Route> routes) {
    this.locationsById = indexLocations(locations);
    this.routesById = indexRoutes(routes);
  }

  /** Returns all locations in insertion order. */
  public List<Location> locations() {
    return Collections.unmodifiableList(new ArrayList<>(locationsById.values()));
  }

  /** Returns all routes in insertion order. */
  public List<Route> routes() {
    return Collections.unmodifiableList(new ArrayList<>(routesById.values()));
  }

  /** Finds a location by identifier. */
  public Optional<Location> findLocation(String locationId) {
    return Optional.ofNullable(locationsById.get(locationId));
  }

  /** Finds a route by identifier. */
  public Optional<Route> findRoute(String routeId) {
    return Optional.ofNullable(routesById.get(routeId));
  }

  /** Returns routes belonging to a double-route group. */
  public List<Route> routesInDoubleGroup(String doubleGroupId) {
    List<Route> matches = new ArrayList<>();
    for (Route route : routesById.values()) {
      if (route.doubleGroupId().filter(doubleGroupId::equals).isPresent()) {
        matches.add(route);
      }
    }
    return Collections.unmodifiableList(matches);
  }

  private static Map<String, Location> indexLocations(List<Location> locations) {
    Objects.requireNonNull(locations, "locations");
    Map<String, Location> indexed = new LinkedHashMap<>();
    for (Location location : locations) {
      if (indexed.put(location.id(), location) != null) {
        throw new IllegalArgumentException("duplicate location id: " + location.id());
      }
    }
    return indexed;
  }

  private static Map<String, Route> indexRoutes(List<Route> routes) {
    Objects.requireNonNull(routes, "routes");
    Map<String, Route> indexed = new LinkedHashMap<>();
    for (Route route : routes) {
      if (indexed.put(route.id(), route) != null) {
        throw new IllegalArgumentException("duplicate route id: " + route.id());
      }
    }
    return indexed;
  }
}
