package ttrlondon.domain.scoring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.scoring.FinalScore.PlayerFinalScore;
import ttrlondon.domain.scoring.FinalScore.TicketResult;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Coordinates final score calculation by delegating to scoring collaborators.
 */
public final class ScoreCalculator {
  private final RouteScoreTable routeScoreTable;
  private final TicketCompletionChecker ticketCompletionChecker;
  private final LongestPathCalculator longestPathCalculator;

  /**
   * Creates a score calculator.
   *
   * @param routeScoreTable route scoring table
   * @param ticketCompletionChecker ticket completion checker
   * @param longestPathCalculator longest path calculator
   */
  public ScoreCalculator(
      RouteScoreTable routeScoreTable,
      TicketCompletionChecker ticketCompletionChecker,
      LongestPathCalculator longestPathCalculator) {
    this.routeScoreTable = Objects.requireNonNull(routeScoreTable, "routeScoreTable");
    this.ticketCompletionChecker =
        Objects.requireNonNull(ticketCompletionChecker, "ticketCompletionChecker");
    this.longestPathCalculator =
        Objects.requireNonNull(longestPathCalculator, "longestPathCalculator");
  }

  /** Calculates final scoring for the supplied game state. */
  public FinalScore calculateFinalScore(Board board, List<Player> players) {
    Objects.requireNonNull(board, "board");
    Objects.requireNonNull(players, "players");
    if (players.isEmpty()) {
      throw new IllegalArgumentException("players must not be empty");
    }

    Map<String, List<Route>> claimedRoutesByPlayerId = claimedRoutesByPlayerId(board, players);
    Map<String, Integer> longestPathLengths = longestPathLengths(claimedRoutesByPlayerId);
    int longestPath =
        longestPathLengths.values().stream().mapToInt(Integer::intValue).max().orElse(0);

    Map<String, PlayerFinalScore> playerScores = new LinkedHashMap<>();
    for (Player player : players) {
      List<Route> claimedRoutes = claimedRoutesByPlayerId.getOrDefault(player.id(), List.of());
      List<TicketResult> ticketResults = ticketResults(player, claimedRoutes);
      int routePoints = routePoints(claimedRoutes);
      int ticketPoints = ticketResults.stream().mapToInt(TicketResult::scoreContribution).sum();
      int playerLongestPath = longestPathLengths.get(player.id());
      int longestPathBonus = playerLongestPath == longestPath ? 10 : 0;
      playerScores.put(
          player.id(),
          new PlayerFinalScore(
              player.id(),
              routePoints,
              ticketPoints,
              playerLongestPath,
              longestPathBonus,
              ticketResults));
    }

    return FinalScore.fromPlayerScores(playerScores, determineWinners(playerScores));
  }

  /** Returns the route score table collaborator. */
  public RouteScoreTable routeScoreTable() {
    return routeScoreTable;
  }

  /** Returns the ticket completion checker collaborator. */
  public TicketCompletionChecker ticketCompletionChecker() {
    return ticketCompletionChecker;
  }

  /** Returns the longest path calculator collaborator. */
  public LongestPathCalculator longestPathCalculator() {
    return longestPathCalculator;
  }

  private static Map<String, List<Route>> claimedRoutesByPlayerId(
      Board board, List<Player> players) {
    Map<String, List<Route>> routesByPlayerId = new LinkedHashMap<>();
    for (Player player : players) {
      routesByPlayerId.put(player.id(), new ArrayList<>());
    }
    for (Route route : board.routes()) {
      route.claimedBy()
          .ifPresent(
              playerId -> {
                if (routesByPlayerId.containsKey(playerId)) {
                  routesByPlayerId.get(playerId).add(route);
                }
              });
    }
    return routesByPlayerId;
  }

  private Map<String, Integer> longestPathLengths(
      Map<String, List<Route>> claimedRoutesByPlayerId) {
    Map<String, Integer> lengthsByPlayerId = new LinkedHashMap<>();
    for (Map.Entry<String, List<Route>> entry : claimedRoutesByPlayerId.entrySet()) {
      lengthsByPlayerId.put(
          entry.getKey(), longestPathCalculator.longestPathLength(entry.getValue()));
    }
    return lengthsByPlayerId;
  }

  private List<TicketResult> ticketResults(Player player, List<Route> claimedRoutes) {
    List<TicketResult> results = new ArrayList<>();
    for (DestinationTicket ticket : player.tickets()) {
      boolean completed = ticketCompletionChecker.isCompleted(ticket, claimedRoutes);
      results.add(
          new TicketResult(
              ticket.id(),
              ticket.locationA().id(),
              ticket.locationB().id(),
              ticket.points(),
              completed));
    }
    return results;
  }

  private int routePoints(List<Route> routes) {
    return routes.stream()
        .mapToInt(route -> routeScoreTable.pointsForLength(route.length()))
        .sum();
  }

  private static List<String> determineWinners(Map<String, PlayerFinalScore> playerScores) {
    List<PlayerFinalScore> candidates = new ArrayList<>(playerScores.values());
    candidates = playersWithMaximum(candidates, PlayerFinalScore::totalScore);
    candidates = playersWithMaximum(candidates, PlayerFinalScore::completedTicketCount);
    candidates = playersWithMaximum(candidates, PlayerFinalScore::longestPathLength);
    return candidates.stream().map(PlayerFinalScore::playerId).toList();
  }

  private static List<PlayerFinalScore> playersWithMaximum(
      List<PlayerFinalScore> candidates, IntScoreSelector selector) {
    int best = candidates.stream().mapToInt(selector::valueFor).max().orElseThrow();
    return candidates.stream().filter(candidate -> selector.valueFor(candidate) == best).toList();
  }

  private interface IntScoreSelector {
    int valueFor(PlayerFinalScore score);
  }
}
