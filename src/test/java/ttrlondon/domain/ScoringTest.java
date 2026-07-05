package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.ColouredRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.scoring.FinalScore;
import ttrlondon.domain.scoring.FinalScore.PlayerFinalScore;
import ttrlondon.domain.scoring.LongestPathCalculator;
import ttrlondon.domain.scoring.RouteScoreTable;
import ttrlondon.domain.scoring.ScoreCalculator;
import ttrlondon.domain.scoring.TicketCompletionChecker;
import ttrlondon.domain.ticket.DestinationTicket;

/** Tests final scoring, ticket completion, and longest path rules. */
final class ScoringTest {
  private final TicketCompletionChecker ticketCompletionChecker = new TicketCompletionChecker();
  private final LongestPathCalculator longestPathCalculator = new LongestPathCalculator();
  private final ScoreCalculator scoreCalculator =
      new ScoreCalculator(new RouteScoreTable(), ticketCompletionChecker, longestPathCalculator);

  @Test
  void ticketCompletionRequiresClaimedRouteConnectivity() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Route first = route("R1", a, b, 1);
    Route second = route("R2", b, c, 1);
    first.claim("P1");
    second.claim("P1");
    DestinationTicket ticket = ticket("T1", a, c, 5);

    assertTrue(ticketCompletionChecker.isCompleted(ticket, List.of(first, second)));
  }

  @Test
  void ticketFailsWhenEndpointsAreDisconnected() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Route first = route("R1", a, b, 1);
    Route second = route("R2", c, d, 1);
    first.claim("P1");
    second.claim("P1");
    DestinationTicket ticket = ticket("T1", a, d, 5);

    assertFalse(ticketCompletionChecker.isCompleted(ticket, List.of(first, second)));
  }

  @Test
  void ticketCompletionUsesAnyAvailablePath() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Route direct = route("R1", a, d, 1);
    Route first = route("R2", a, b, 1);
    Route second = route("R3", b, c, 1);
    Route third = route("R4", c, d, 1);
    first.claim("P1");
    second.claim("P1");
    third.claim("P1");
    DestinationTicket ticket = ticket("T1", a, d, 5);

    assertTrue(ticketCompletionChecker.isCompleted(ticket, List.of(direct, first, second, third)));
  }

  @Test
  void longestPathCountsSimpleLinearChain() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    List<Route> routes = List.of(route("R1", a, b, 1), route("R2", b, c, 2), route("R3", c, d, 3));

    assertEquals(6, longestPathCalculator.longestPathLength(routes));
  }

  @Test
  void longestPathAllowsVertexRevisitWithoutReusingEdges() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    List<Route> routes =
        List.of(
            route("R1", a, b, 1),
            route("R2", b, c, 1),
            route("R3", c, a, 1),
            route("R4", a, d, 1));

    assertEquals(4, longestPathCalculator.longestPathLength(routes));
  }

  @Test
  void longestPathUsesFullRouteWeights() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    List<Route> routes = List.of(route("R1", a, b, 4), route("R2", b, c, 1), route("R3", c, d, 3));

    assertEquals(8, longestPathCalculator.longestPathLength(routes));
  }

  @Test
  void finalScoreIncludesRoutesTicketsAndLongestPathBonus() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Route first = route("R1", a, b, 2);
    Route second = route("R2", b, c, 3);
    first.claim("P1");
    second.claim("P1");
    Player firstPlayer = player("P1", PlayerColor.RED);
    Player secondPlayer = player("P2", PlayerColor.BLUE);
    firstPlayer.addTickets(List.of(ticket("T1", a, c, 5), ticket("T2", a, d, 4)));

    FinalScore finalScore =
        scoreCalculator.calculateFinalScore(board(first, second), List.of(firstPlayer, secondPlayer));
    PlayerFinalScore firstScore = finalScore.scoresByPlayerId().get("P1");

    assertEquals(6, firstScore.routePoints());
    assertEquals(1, firstScore.ticketPoints());
    assertEquals(5, firstScore.longestPathLength());
    assertEquals(10, firstScore.longestPathBonus());
    assertEquals(17, firstScore.totalScore());
    assertEquals(List.of("P1"), finalScore.winnerIds());
  }

  @Test
  void longestPathTieAwardsBonusToAllTiedPlayers() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Route first = route("R1", a, b, 2);
    Route second = route("R2", c, d, 2);
    first.claim("P1");
    second.claim("P2");

    FinalScore finalScore =
        scoreCalculator.calculateFinalScore(
            board(first, second),
            List.of(player("P1", PlayerColor.RED), player("P2", PlayerColor.BLUE)));

    assertEquals(10, finalScore.scoresByPlayerId().get("P1").longestPathBonus());
    assertEquals(10, finalScore.scoresByPlayerId().get("P2").longestPathBonus());
    assertEquals(List.of("P1", "P2"), finalScore.winnerIds());
  }

  @Test
  void pointsTieBreaksByCompletedTicketCount() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Location e = location("E");
    Location f = location("F");
    Location g = location("G");
    Route p1First = route("R1", a, b, 1);
    Route p1Second = route("R2", b, c, 1);
    Route p2First = route("R3", d, e, 2);
    Route p2Second = route("R4", f, g, 2);
    p1First.claim("P1");
    p1Second.claim("P1");
    p2First.claim("P2");
    p2Second.claim("P2");
    Player firstPlayer = player("P1", PlayerColor.RED);
    Player secondPlayer = player("P2", PlayerColor.BLUE);
    firstPlayer.addTickets(List.of(ticket("T1", a, c, 2)));

    FinalScore finalScore =
        scoreCalculator.calculateFinalScore(
            board(p1First, p1Second, p2First, p2Second), List.of(firstPlayer, secondPlayer));

    assertEquals(Map.of("P1", 14, "P2", 14), finalScore.totalsByPlayerId());
    assertEquals(List.of("P1"), finalScore.winnerIds());
  }

  @Test
  void remainingTieBreaksByLongestPathLength() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Location e = location("E");
    Location f = location("F");
    Location g = location("G");
    Route p1Route = route("R1", a, b, 3);
    Route p2First = route("R2", d, e, 2);
    Route p2Second = route("R3", f, g, 2);
    p1Route.claim("P1");
    p2First.claim("P2");
    p2Second.claim("P2");
    Player firstPlayer = player("P1", PlayerColor.RED);
    Player secondPlayer = player("P2", PlayerColor.BLUE);
    firstPlayer.addTickets(List.of(ticket("T1", a, b, 1), ticket("T2", c, d, 5)));
    secondPlayer.addTickets(List.of(ticket("T3", d, e, 6)));

    FinalScore finalScore =
        scoreCalculator.calculateFinalScore(
            board(p1Route, p2First, p2Second), List.of(firstPlayer, secondPlayer));

    assertEquals(Map.of("P1", 10, "P2", 10), finalScore.totalsByPlayerId());
    assertEquals(List.of("P1"), finalScore.winnerIds());
  }

  @Test
  void sharedWinRemainsWhenAllTieBreakersAreExhausted() {
    Location a = location("A");
    Location b = location("B");
    Location c = location("C");
    Location d = location("D");
    Route first = route("R1", a, b, 2);
    Route second = route("R2", c, d, 2);
    first.claim("P1");
    second.claim("P2");
    Player firstPlayer = player("P1", PlayerColor.RED);
    Player secondPlayer = player("P2", PlayerColor.BLUE);
    firstPlayer.addTickets(List.of(ticket("T1", a, b, 1)));
    secondPlayer.addTickets(List.of(ticket("T2", c, d, 1)));

    FinalScore finalScore =
        scoreCalculator.calculateFinalScore(
            board(first, second), List.of(firstPlayer, secondPlayer));

    assertEquals(Map.of("P1", 13, "P2", 13), finalScore.totalsByPlayerId());
    assertEquals(List.of("P1", "P2"), finalScore.winnerIds());
  }

  private static Board board(Route... routes) {
    return new Board(locationsFrom(routes), List.of(routes));
  }

  private static List<Location> locationsFrom(Route... routes) {
    Map<String, Location> locations = new LinkedHashMap<>();
    for (Route route : routes) {
      locations.put(route.locationA().id(), route.locationA());
      locations.put(route.locationB().id(), route.locationB());
    }
    return new ArrayList<>(locations.values());
  }

  private static Location location(String id) {
    return new Location(id, id, 1);
  }

  private static Route route(String id, Location locationA, Location locationB, int length) {
    return new Route(
        id,
        locationA,
        locationB,
        length,
        RouteColor.BLUE,
        null,
        new ColouredRouteRequirement(RouteColor.BLUE, length));
  }

  private static DestinationTicket ticket(
      String id, Location locationA, Location locationB, int points) {
    return new DestinationTicket(id, locationA, locationB, points);
  }

  private static Player player(String id, PlayerColor color) {
    return new Player(id, id, color);
  }
}
