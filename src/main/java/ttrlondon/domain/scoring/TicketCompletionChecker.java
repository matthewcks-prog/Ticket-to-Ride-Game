package ttrlondon.domain.scoring;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Checks whether claimed routes connect destination ticket endpoints.
 */
public final class TicketCompletionChecker {
  /**
   * Returns whether a ticket is completed by a set of claimed routes.
   *
   * @param ticket destination ticket to evaluate
   * @param claimedRoutes routes claimed by one player
   * @return true when the endpoints are connected
   */
  public boolean isCompleted(DestinationTicket ticket, List<Route> claimedRoutes) {
    Objects.requireNonNull(ticket, "ticket");
    Objects.requireNonNull(claimedRoutes, "claimedRoutes");
    Map<Location, List<Location>> adjacency = buildAdjacency(claimedRoutes);
    return isReachable(ticket.locationA(), ticket.locationB(), adjacency);
  }

  private static Map<Location, List<Location>> buildAdjacency(List<Route> routes) {
    Map<Location, List<Location>> adjacency = new HashMap<>();
    for (Route route : routes) {
      adjacency
          .computeIfAbsent(route.locationA(), ignored -> new ArrayList<>())
          .add(route.locationB());
      adjacency
          .computeIfAbsent(route.locationB(), ignored -> new ArrayList<>())
          .add(route.locationA());
    }
    return adjacency;
  }

  private static boolean isReachable(
      Location start, Location target, Map<Location, List<Location>> adjacency) {
    if (!adjacency.containsKey(start) || !adjacency.containsKey(target)) {
      return false;
    }

    Queue<Location> frontier = new ArrayDeque<>();
    Set<Location> visited = new HashSet<>();
    frontier.add(start);
    visited.add(start);

    while (!frontier.isEmpty()) {
      Location current = frontier.remove();
      if (current.equals(target)) {
        return true;
      }
      for (Location next : adjacency.getOrDefault(current, List.of())) {
        if (visited.add(next)) {
          frontier.add(next);
        }
      }
    }
    return false;
  }
}
