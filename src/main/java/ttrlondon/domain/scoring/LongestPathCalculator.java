package ttrlondon.domain.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;

/**
 * Calculates a player's longest continuous path from claimed routes.
 */
public final class LongestPathCalculator {
  /**
   * Calculates the maximum trail length from one player's claimed routes.
   *
   * @param claimedRoutes routes claimed by one player
   * @return longest continuous path length
   */
  public int longestPathLength(List<Route> claimedRoutes) {
    Objects.requireNonNull(claimedRoutes, "claimedRoutes");
    Map<Location, List<Edge>> adjacency = buildAdjacency(claimedRoutes);
    int longest = 0;
    for (Location location : adjacency.keySet()) {
      longest = Math.max(longest, search(location, adjacency, new HashSet<>()));
    }
    return longest;
  }

  private static Map<Location, List<Edge>> buildAdjacency(List<Route> routes) {
    Map<Location, List<Edge>> adjacency = new HashMap<>();
    for (Route route : routes) {
      Edge edge = new Edge(route);
      adjacency.computeIfAbsent(route.locationA(), ignored -> new ArrayList<>()).add(edge);
      adjacency.computeIfAbsent(route.locationB(), ignored -> new ArrayList<>()).add(edge);
    }
    return adjacency;
  }

  private static int search(
      Location current, Map<Location, List<Edge>> adjacency, Set<String> usedRouteIds) {
    int longest = 0;
    for (Edge edge : adjacency.getOrDefault(current, List.of())) {
      if (!usedRouteIds.add(edge.id())) {
        continue;
      }
      int candidate =
          edge.length() + search(edge.otherEndpoint(current), adjacency, usedRouteIds);
      longest = Math.max(longest, candidate);
      usedRouteIds.remove(edge.id());
    }
    return longest;
  }

  private record Edge(Route route) {
    private String id() {
      return route.id();
    }

    private int length() {
      return route.length();
    }

    private Location otherEndpoint(Location endpoint) {
      if (route.locationA().equals(endpoint)) {
        return route.locationB();
      }
      if (route.locationB().equals(endpoint)) {
        return route.locationA();
      }
      throw new IllegalArgumentException("location is not an endpoint of this route");
    }
  }
}
