package ttrlondon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.commands.ClaimRoutePayment;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.ColouredRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.rushhour.RouteSelectors;
import ttrlondon.domain.rushhour.RushHourEvent;
import ttrlondon.domain.rushhour.RushHourManager;
import ttrlondon.domain.rushhour.RushHourPhase;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/** Tests Rush Hour event clock, claim modifiers, snapshots, and undo integration. */
final class RushHourEventsTest {
  @Test
  void forecastAffectedRouteDoesNotRequireDetourPayment() {
    Game game = createRushHourGame();
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))));

    assertTrue(result.isSuccess());
    assertEquals(1, game.players().get(0).score());
    assertEquals(0, game.rushHourManager().bonusPointsByPlayerId().getOrDefault("P1", 0));
  }

  @Test
  void peakAffectedRouteRequiresSeparateDetourPayment() {
    Game game = createRushHourGameInPeak();
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))));

    assertTrue(result.isFailure());
    assertTrue(result.message().contains("Rush Hour"));
    assertFalse(game.board().findRoute("R1").orElseThrow().isClaimed());
    assertEquals(List.of(CardColor.BLUE), game.players().get(0).hand());
  }

  @Test
  void peakAffectedRouteWithDetourAwardsBonusAndDiscardsCombinedPayment() {
    Game game = createRushHourGameInPeak();
    game.players().get(0).addCards(List.of(CardColor.BLUE, CardColor.GREEN));
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand(
                "P1",
                "R1",
                new ClaimRoutePayment(
                    new CardPayment(List.of(CardColor.BLUE)),
                    new CardPayment(List.of(CardColor.GREEN)))));

    assertTrue(result.isSuccess());
    assertEquals(3, game.players().get(0).score());
    assertEquals(2, game.rushHourManager().bonusPointsByPlayerId().get("P1"));
    assertEquals(2, game.transportCardDeck().discardPileSize());
  }

  @Test
  void snapshotExposesRushHourEventStateAndAffectedRoutes() {
    Game game = createRushHourGame();
    GameSnapshot snapshot = new GameApplicationService(game).getSnapshot();

    assertEquals(RushHourPhase.FORECAST, snapshot.rushHourPhase());
    assertEquals(2, snapshot.rushHourTurnsRemaining());
    assertEquals("RH_TEST", snapshot.forecastRushHourEvent().orElseThrow().id());
    assertEquals(List.of("R1"), snapshot.rushHourAffectedRouteIds());
  }

  @Test
  void undoRestoresRushHourBonusLedgerAndPhase() {
    Game game = createRushHourGameInPeak();
    game.players().get(0).addCards(List.of(CardColor.BLUE, CardColor.GREEN));
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand(
                    "P1",
                    "R1",
                    new ClaimRoutePayment(
                        new CardPayment(List.of(CardColor.BLUE)),
                        new CardPayment(List.of(CardColor.GREEN)))))
            .isSuccess());

    assertTrue(service.undoLastTurn().isSuccess());

    assertEquals(RushHourPhase.PEAK, game.rushHourManager().phase());
    assertTrue(game.rushHourManager().bonusPointsByPlayerId().isEmpty());
    assertEquals(0, game.players().get(0).score());
    assertFalse(game.board().findRoute("R1").orElseThrow().isClaimed());
  }

  @Test
  void rushHourClockStaysInactiveWhenNoEventsExist() {
    RushHourManager manager =
        new RushHourManager(List.of(), List.of(), new FixedOrderShuffleStrategy());

    manager.start(2);
    manager.advanceAfterCompletedTurn(2);

    assertEquals(RushHourPhase.INACTIVE, manager.phase());
    assertEquals(0, manager.turnsRemaining());
    assertTrue(manager.forecastEvent().isEmpty());
    assertTrue(manager.activeEvent().isEmpty());
  }

  @Test
  void rushHourClockRecyclesDiscardedEventsAfterPeakRound() {
    RushHourManager manager =
        new RushHourManager(
            List.of(rushHourEvent("RH_TEST", "R1")),
            List.of("RH_TEST"),
            new FixedOrderShuffleStrategy());

    manager.start(2);
    manager.advanceAfterCompletedTurn(2);
    manager.advanceAfterCompletedTurn(2);
    assertEquals(RushHourPhase.PEAK, manager.phase());
    assertEquals("RH_TEST", manager.activeEventId().orElseThrow());

    manager.advanceAfterCompletedTurn(2);
    manager.advanceAfterCompletedTurn(2);

    assertEquals(RushHourPhase.FORECAST, manager.phase());
    assertEquals("RH_TEST", manager.forecastEventId().orElseThrow());
    assertTrue(manager.activeEventId().isEmpty());
    assertTrue(manager.eventDeckSnapshot().isEmpty());
    assertTrue(manager.eventDiscardSnapshot().isEmpty());
    assertEquals(2, manager.turnsRemaining());
  }

  private static Game createRushHourGameInPeak() {
    Game game = createRushHourGame();
    game.rushHourManager().advanceAfterCompletedTurn(2);
    game.rushHourManager().advanceAfterCompletedTurn(2);
    return game;
  }

  private static Game createRushHourGame() {
    Location first = new Location("A", "A", 1);
    Location second = new Location("B", "B", 1);
    Board board =
        new Board(
            List.of(first, second),
            List.of(
                new Route(
                    "R1",
                    first,
                    second,
                    1,
                    RouteColor.BLUE,
                    null,
                    new ColouredRouteRequirement(RouteColor.BLUE, 1))));
    Player firstPlayer = new Player("P1", "Red", PlayerColor.RED);
    Player secondPlayer = new Player("P2", "Blue", PlayerColor.BLUE);
    RushHourManager rushHourManager =
        new RushHourManager(
            List.of(
                new RushHourEvent(
                    "RH_TEST",
                    "Test Gridlock",
                    "Test event",
                    RouteSelectors.byRouteIds("R1"),
                    1,
                    2)),
            List.of("RH_TEST"),
            new FixedOrderShuffleStrategy());
    return new Game(
        board,
        List.of(firstPlayer, secondPlayer),
        new TransportCardDeck(List.of(), new FixedOrderShuffleStrategy()),
        new FaceUpDisplay(List.of()),
        new DestinationTicketDeck(List.of()),
        new TurnManager(List.of("P1", "P2"), "P1"),
        rushHourManager,
        GamePhase.RUNNING);
  }

  private static RushHourEvent rushHourEvent(String id, String routeId) {
    return new RushHourEvent(
        id,
        "Test Gridlock",
        "Test event",
        RouteSelectors.byRouteIds(routeId),
        1,
        2);
  }
}
