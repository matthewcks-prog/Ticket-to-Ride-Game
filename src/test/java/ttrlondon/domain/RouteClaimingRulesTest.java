package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.board.ColouredRouteRequirement;
import ttrlondon.domain.board.FerryRouteRequirement;
import ttrlondon.domain.board.GreyRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.scoring.RouteScoreTable;

/** Tests route payment, player affordability, buses, score, and double-route rules. */
final class RouteClaimingRulesTest {
  private static final Location A = new Location("A", "A", 1);
  private static final Location B = new Location("B", "B", 1);

  @Test
  void colouredRouteAcceptsMatchingCardsBusesAndAllBusPayments() {
    Route route = colouredRoute("r1", 3, RouteColor.BLUE, null);

    assertTrue(
        route.requirement().isSatisfiedBy(payment(CardColor.BLUE, CardColor.BLUE, CardColor.BLUE)));
    assertTrue(
        route.requirement().isSatisfiedBy(payment(CardColor.BLUE, CardColor.BUS, CardColor.BUS)));
    assertTrue(
        route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.BUS, CardColor.BUS)));
    assertFalse(
        route.requirement().isSatisfiedBy(payment(CardColor.BLUE, CardColor.GREEN, CardColor.BUS)));
    assertFalse(
        route.requirement()
            .isSatisfiedBy(payment(CardColor.GREEN, CardColor.GREEN, CardColor.BUS)));
    assertFalse(route.requirement().isSatisfiedBy(payment(CardColor.BLUE, CardColor.BLUE)));
  }

  @Test
  void greyRouteAcceptsAnySingleColourSetAndAllBusPayments() {
    Route route = greyRoute("r1", 2, null);

    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.ORANGE, CardColor.BUS)));
    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.PINK, CardColor.PINK)));
    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.BUS)));
    assertFalse(route.requirement().isSatisfiedBy(payment(CardColor.PINK, CardColor.BLACK)));
    assertFalse(route.requirement().isSatisfiedBy(payment(CardColor.PINK)));
  }

  @Test
  void ferryRouteAcceptsRequiredBusSymbolAndNormalRemainingSpaces() {
    Route route = ferryRoute("r1", 3, RouteColor.GREY, 1, null);

    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.PINK, CardColor.PINK)));
    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.BUS, CardColor.PINK)));
    assertFalse(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.PINK, CardColor.BLACK)));
  }

  @Test
  void ferryRouteAcceptsThreeCardSubstituteForMissingBusSymbol() {
    Route route = ferryRoute("r1", 3, RouteColor.GREY, 1, null);

    assertTrue(
        route.requirement()
            .isSatisfiedBy(
                payment(
                    CardColor.BLUE,
                    CardColor.GREEN,
                    CardColor.BLACK,
                    CardColor.PINK,
                    CardColor.PINK)));
    assertFalse(
        route.requirement()
            .isSatisfiedBy(payment(CardColor.BLUE, CardColor.GREEN, CardColor.PINK, CardColor.PINK)));
  }

  @Test
  void colouredFerryRouteStillRequiresPrintedColourForRemainingSpaces() {
    Route route = ferryRoute("r1", 3, RouteColor.BLUE, 1, null);

    assertTrue(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.BLUE, CardColor.BUS)));
    assertFalse(route.requirement().isSatisfiedBy(payment(CardColor.BUS, CardColor.GREEN, CardColor.GREEN)));
  }

  @Test
  void routeClaimRejectsClaimedRoutesBadPaymentAndInsufficientBuses() {
    Route route = colouredRoute("r1", 2, RouteColor.YELLOW, null);

    assertTrue(
        route.canBeClaimed("p1", 3, List.of(), payment(CardColor.YELLOW, CardColor.BUS), 2));
    assertFalse(
        route.canBeClaimed("p1", 3, List.of(), payment(CardColor.YELLOW, CardColor.BUS), 1));
    assertFalse(
        route.canBeClaimed("p1", 3, List.of(), payment(CardColor.YELLOW, CardColor.BLUE), 2));

    route.claim("p1");

    assertFalse(
        route.canBeClaimed("p2", 3, List.of(), payment(CardColor.YELLOW, CardColor.YELLOW), 2));
    assertThrows(IllegalStateException.class, () -> route.claim("p2"));
  }

  @Test
  void doubleRouteRulesRespectOwnerAndPlayerCountRestrictions() {
    Route first = colouredRoute("r1", 1, RouteColor.GREEN, "double-1");
    Route second = colouredRoute("r2", 1, RouteColor.YELLOW, "double-1");
    List<Route> group = List.of(first, second);

    first.claim("p1");

    assertFalse(second.canBeClaimed("p1", 4, group, payment(CardColor.YELLOW), 17));
    assertFalse(second.canBeClaimed("p2", 2, group, payment(CardColor.YELLOW), 17));
    assertTrue(second.canBeClaimed("p2", 3, group, payment(CardColor.YELLOW), 17));
  }

  @Test
  void spendingCardsUsingBusesAndScoringAreAppliedByDomainObjects() {
    Player player = new Player("p1", "Red", PlayerColor.RED);
    Route route = colouredRoute("r1", 4, RouteColor.BLACK, null);
    CardPayment payment = payment(CardColor.BLACK, CardColor.BLACK, CardColor.BUS, CardColor.BUS);
    RouteScoreTable scoreTable = new RouteScoreTable();
    player.addCards(
        List.of(CardColor.BLACK, CardColor.BLACK, CardColor.BUS, CardColor.BUS, CardColor.GREEN));

    assertTrue(player.canAfford(payment));
    player.spendCards(payment);
    player.useBuses(route.length());
    route.claim(player.id());
    player.addScore(scoreTable.pointsForLength(route.length()));

    assertEquals(List.of(CardColor.GREEN), player.hand());
    assertEquals(13, player.busesRemaining());
    assertEquals(7, player.score());
    assertEquals("p1", route.claimedBy().orElseThrow());
    assertThrows(IllegalArgumentException.class, () -> player.spendCards(payment(CardColor.PINK)));
  }

  @Test
  void routeScoreTableSupportsLondonLengthsOnly() {
    RouteScoreTable scoreTable = new RouteScoreTable();

    assertEquals(1, scoreTable.pointsForLength(1));
    assertEquals(2, scoreTable.pointsForLength(2));
    assertEquals(4, scoreTable.pointsForLength(3));
    assertEquals(7, scoreTable.pointsForLength(4));
    assertThrows(IllegalArgumentException.class, () -> scoreTable.pointsForLength(5));
  }

  private static Route colouredRoute(
      String id, int length, RouteColor color, String doubleGroupId) {
    return new Route(
        id, A, B, length, color, doubleGroupId, new ColouredRouteRequirement(color, length));
  }

  private static Route greyRoute(String id, int length, String doubleGroupId) {
    return new Route(
        id, A, B, length, RouteColor.GREY, doubleGroupId, new GreyRouteRequirement(length));
  }

  private static Route ferryRoute(
      String id, int length, RouteColor color, int requiredBusSymbols, String doubleGroupId) {
    return new Route(
        id,
        A,
        B,
        length,
        color,
        ttrlondon.domain.board.RouteKind.FERRY,
        requiredBusSymbols,
        doubleGroupId,
        new FerryRouteRequirement(color, length, requiredBusSymbols));
  }

  private static CardPayment payment(CardColor... cards) {
    return new CardPayment(List.of(cards));
  }
}
