package ttrlondon.application.commands;

import java.util.List;
import java.util.Objects;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.rushhour.RushHourClaimRule;
import ttrlondon.domain.scoring.RouteScoreTable;

/**
 * Command that claims a single route for the active player.
 */
public final class ClaimRouteCommand implements GameCommand {
  private final String playerId;
  private final String routeId;
  private final ClaimRoutePayment payment;
  private final RouteScoreTable routeScoreTable;

  /**
   * Creates a route-claim command.
   *
   * @param playerId claiming player identifier
   * @param routeId route identifier
   * @param payment transportation card payment
   */
  public ClaimRouteCommand(String playerId, String routeId, CardPayment payment) {
    this(playerId, routeId, ClaimRoutePayment.routeOnly(payment), new RouteScoreTable());
  }

  /**
   * Creates a route-claim command with split route and Rush Hour detour payment.
   *
   * @param playerId claiming player identifier
   * @param routeId route identifier
   * @param payment split route-claim payment
   */
  public ClaimRouteCommand(String playerId, String routeId, ClaimRoutePayment payment) {
    this(playerId, routeId, payment, new RouteScoreTable());
  }

  /**
   * Creates a route-claim command with an explicit route score table.
   *
   * @param playerId claiming player identifier
   * @param routeId route identifier
   * @param payment transportation card payment
   * @param routeScoreTable route scoring rule
   */
  public ClaimRouteCommand(
      String playerId, String routeId, CardPayment payment, RouteScoreTable routeScoreTable) {
    this(playerId, routeId, ClaimRoutePayment.routeOnly(payment), routeScoreTable);
  }

  /**
   * Creates a route-claim command with an explicit route score table.
   *
   * @param playerId claiming player identifier
   * @param routeId route identifier
   * @param payment split route-claim payment
   * @param routeScoreTable route scoring rule
   */
  public ClaimRouteCommand(
      String playerId, String routeId, ClaimRoutePayment payment, RouteScoreTable routeScoreTable) {
    this.playerId = Text.requireNonBlank(playerId, "playerId");
    this.routeId = Text.requireNonBlank(routeId, "routeId");
    this.payment = Objects.requireNonNull(payment, "payment");
    this.routeScoreTable = Objects.requireNonNull(routeScoreTable, "routeScoreTable");
  }

  /** Returns the claiming player identifier. */
  public String playerId() {
    return playerId;
  }

  /** Returns the route identifier. */
  public String routeId() {
    return routeId;
  }

  /** Returns the offered card payment. */
  public ClaimRoutePayment payment() {
    return payment;
  }

  @Override
  public CommandResult execute(Game game) {
    Objects.requireNonNull(game, "game");
    CommandResult validation = validate(game);
    if (validation.isFailure()) {
      return validation;
    }

    Player player = findPlayer(game, playerId);
    Route route = game.board().findRoute(routeId).orElseThrow();
    RushHourClaimRule rushHourClaimRule = new RushHourClaimRule(game.rushHourManager());
    int rushHourBonus = rushHourClaimRule.bonusPoints(route);
    CardPayment combinedPayment = payment.combinedPayment();
    player.spendCards(combinedPayment);
    game.transportCardDeck().discard(combinedPayment.copyCards());
    player.useBuses(route.length());
    route.claim(player.id());
    player.addScore(routeScoreTable.pointsForLength(route.length()) + rushHourBonus);
    game.rushHourManager().awardBonus(player.id(), rushHourBonus);
    game.endCurrentTurn();
    return CommandResult.success("Route claimed.");
  }

  private CommandResult validate(Game game) {
    if (!game.acceptsPlayerActions()) {
      return CommandResult.failure("Game is not accepting player actions.");
    }
    if (!game.turnManager().isCurrentPlayer(playerId)) {
      return CommandResult.failure("It is not this player's turn.");
    }
    Player player = game.findPlayer(playerId).orElse(null);
    if (player == null) {
      return CommandResult.failure("Player does not exist.");
    }
    Route route = game.board().findRoute(routeId).orElse(null);
    if (route == null) {
      return CommandResult.failure("Route does not exist.");
    }
    if (route.isClaimed()) {
      return CommandResult.failure(
          "Route " + route.id() + " is already claimed and cannot be claimed again.");
    }
    if (player.busesRemaining() < route.length()) {
      return CommandResult.failure(
          "Player needs "
              + route.length()
              + " buses to claim this route but only has "
              + player.busesRemaining()
              + " remaining.");
    }
    if (!route.requirement().isSatisfiedBy(payment.routePayment())) {
      return CommandResult.failure(paymentFailureMessage(route, payment.routePayment()));
    }
    RushHourClaimRule rushHourClaimRule = new RushHourClaimRule(game.rushHourManager());
    if (!rushHourClaimRule.isDetourSatisfied(route, payment.rushHourDetourPayment())) {
      return CommandResult.failure(rushHourFailureMessage(route, rushHourClaimRule));
    }
    if (!player.canAfford(payment.combinedPayment())) {
      return CommandResult.failure(
          "Player does not have all selected transportation cards in hand.");
    }
    List<Route> doubleGroupRoutes =
        route.doubleGroupId().map(game.board()::routesInDoubleGroup).orElse(List.of());
    if (!route.canBeClaimed(
        playerId,
        game.players().size(),
        doubleGroupRoutes,
        payment.routePayment(),
        player.busesRemaining())) {
      return CommandResult.failure(doubleRouteFailureMessage(game, route, playerId));
    }
    return CommandResult.success("Route claim is valid.");
  }

  private static String rushHourFailureMessage(Route route, RushHourClaimRule rushHourClaimRule) {
    int required = rushHourClaimRule.requiredDetourCards(route);
    if (required == 0) {
      return "Unaffected routes must not include Rush Hour detour cards.";
    }
    return "Rush Hour affects route "
        + route.id()
        + "; add exactly "
        + required
        + " extra detour card.";
  }

  private static String paymentFailureMessage(Route route, CardPayment payment) {
    if (route.isFerry()) {
      return "Ferry route "
          + route.id()
          + " requires "
          + route.requiredBusSymbols()
          + " Bus symbol(s). Use one Bus card per symbol, or any 3 cards in place of each "
          + "missing Bus symbol, plus the normal cards for remaining route spaces.";
    }
    if (payment.size() != route.length()) {
      return "Payment must contain exactly "
          + route.length()
          + " cards for this route, but "
          + payment.size()
          + " were selected.";
    }
    if (!payment.hasSingleNonBusColor()) {
      return "Payment must use one colour set; Bus cards may substitute, but mixed non-Bus "
          + "colours are not allowed.";
    }
    if (route.color() != RouteColor.GREY) {
      return "Payment must match the route colour "
          + route.color()
          + ", using Bus cards only as wild substitutions.";
    }
    return "Payment does not satisfy this route's card requirement.";
  }

  private static String doubleRouteFailureMessage(Game game, Route route, String playerId) {
    List<Route> doubleGroupRoutes =
        route.doubleGroupId().map(game.board()::routesInDoubleGroup).orElse(List.of());
    boolean playerAlreadyClaimedPair =
        doubleGroupRoutes.stream()
            .anyMatch(groupRoute -> groupRoute.claimedBy().filter(playerId::equals).isPresent());
    if (playerAlreadyClaimedPair) {
      return "A player cannot claim both routes in the same double-route pair.";
    }
    if (game.players().size() == 2 && doubleGroupRoutes.stream().anyMatch(Route::isClaimed)) {
      return "In a 2-player game, claiming one route in a double-route pair closes the other.";
    }
    return "Route cannot be claimed because of double-route restrictions.";
  }

  private static Player findPlayer(Game game, String playerId) {
    return game.findPlayer(playerId)
        .orElseThrow(() -> new IllegalArgumentException("player does not exist"));
  }
}
