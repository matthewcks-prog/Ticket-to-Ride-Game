package ttrlondon.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.commands.DrawDestinationTicketsCommand;
import ttrlondon.application.commands.DrawTransportCardCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.DestinationTicketDrawPreview;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.FerryRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;
import ttrlondon.testsupport.TestGameFactory;

/** Tests application commands, snapshots, observers, and draw orchestration. */
final class GameApplicationServiceTest {
  @Test
  void claimRouteCommandExecutesDomainBehaviourAndPublishesSnapshot() {
    Game game = TestGameFactory.createSmallGame();
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    GameApplicationService service = new GameApplicationService(game);
    List<GameSnapshot> snapshots = new ArrayList<>();
    service.addListener(snapshots::add);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))));

    assertTrue(result.isSuccess());
    assertEquals("P2", game.turnManager().currentPlayerId());
    assertEquals("P1", game.board().findRoute("R1").orElseThrow().claimedBy().orElseThrow());
    assertEquals(16, game.players().get(0).busesRemaining());
    assertEquals(1, game.players().get(0).score());
    assertEquals(1, game.transportCardDeck().discardPileSize());
    assertEquals(1, snapshots.size());
    assertEquals("P2", snapshots.get(0).currentPlayerId());
  }

  @Test
  void ferryClaimDiscardsFullPaymentButUsesPrintedRouteLengthForBusesAndScore() {
    Game game = createFerryGame();
    game.players()
        .get(0)
        .addCards(
            List.of(
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.PINK));
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand(
                "P1",
                "F1",
                new CardPayment(
                    List.of(
                        CardColor.BLUE,
                        CardColor.GREEN,
                        CardColor.BLACK,
                        CardColor.PINK,
                        CardColor.PINK))));

    assertTrue(result.isSuccess());
    assertEquals("P1", game.board().findRoute("F1").orElseThrow().claimedBy().orElseThrow());
    assertEquals(14, game.players().get(0).busesRemaining());
    assertEquals(4, game.players().get(0).score());
    assertEquals(5, game.transportCardDeck().discardPileSize());
    assertEquals("P2", game.turnManager().currentPlayerId());
  }

  @Test
  void failedFerryClaimDoesNotMutateGameState() {
    Game game = createFerryGame();
    game.players().get(0).addCards(List.of(CardColor.PINK, CardColor.PINK, CardColor.GREEN));
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand(
                "P1",
                "F1",
                new CardPayment(List.of(CardColor.PINK, CardColor.PINK, CardColor.GREEN))));

    assertTrue(result.isFailure());
    assertFalse(game.board().findRoute("F1").orElseThrow().isClaimed());
    assertEquals(List.of(CardColor.PINK, CardColor.PINK, CardColor.GREEN), game.players().get(0).hand());
    assertEquals(17, game.players().get(0).busesRemaining());
    assertEquals(0, game.players().get(0).score());
    assertEquals(0, game.transportCardDeck().discardPileSize());
    assertEquals("P1", game.turnManager().currentPlayerId());
  }

  @Test
  void invalidCommandReturnsFailureAndDoesNotPublishSnapshot() {
    Game game = TestGameFactory.createSmallGame();
    GameApplicationService service = new GameApplicationService(game);
    List<GameSnapshot> snapshots = new ArrayList<>();
    service.addListener(snapshots::add);

    CommandResult result =
        service.executeCommand(
            new ClaimRouteCommand("P2", "R1", new CardPayment(List.of(CardColor.BLUE))));

    assertTrue(result.isFailure());
    assertTrue(result.message().contains("turn"));
    assertFalse(game.board().findRoute("R1").orElseThrow().isClaimed());
    assertTrue(snapshots.isEmpty());
  }

  @Test
  void twoBlindTransportationCardDrawsCompleteTurn() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE, CardColor.GREEN),
            List.of(),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);
    List<GameSnapshot> snapshots = new ArrayList<>();
    service.addListener(snapshots::add);

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertTrue(service.getSnapshot().transportDrawActionActive());
    assertEquals(1, service.getSnapshot().transportDrawsTaken());

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());

    assertEquals(List.of(CardColor.BLUE, CardColor.GREEN), game.players().get(0).hand());
    assertEquals("P2", game.turnManager().currentPlayerId());
    assertEquals(2, snapshots.size());
  }

  @Test
  void faceUpNonBusThenBlindTransportationCardDrawIsAllowed() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.ORANGE, CardColor.BLUE),
            List.of(CardColor.PINK),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0)).isSuccess());
    assertEquals(List.of(CardColor.ORANGE), game.faceUpDisplay().visibleCards());

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());

    assertEquals(List.of(CardColor.PINK, CardColor.BLUE), game.players().get(0).hand());
    assertEquals("P2", game.turnManager().currentPlayerId());
  }

  @Test
  void faceUpBusAsFirstTransportationCardDrawEndsActionImmediately() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE),
            List.of(CardColor.BUS),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result = service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0));

    assertTrue(result.isSuccess());
    assertEquals(List.of(CardColor.BUS), game.players().get(0).hand());
    assertEquals("P2", game.turnManager().currentPlayerId());
  }

  @Test
  void faceUpBusCannotBeSecondTransportationCardDraw() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.ORANGE),
            List.of(CardColor.PINK, CardColor.BUS),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0)).isSuccess());
    CommandResult result = service.executeCommand(DrawTransportCardCommand.faceUp("P1", 1));

    assertTrue(result.isFailure());
    assertTrue(result.message().contains("Bus"));
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertEquals(List.of(CardColor.PINK), game.players().get(0).hand());
  }

  @Test
  void replacementBusSlotIsLockedFromSecondTransportationCardDraw() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BUS, CardColor.BLUE),
            List.of(
                CardColor.PINK,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.YELLOW,
                CardColor.ORANGE),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0)).isSuccess());
    CommandResult result = service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0));

    assertTrue(result.isFailure());
    assertTrue(result.message().contains("replacement Bus"));
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertEquals(List.of(CardColor.PINK), game.players().get(0).hand());
  }

  @Test
  void blindDrawnBusDoesNotRestrictSecondTransportationCardDraw() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BUS, CardColor.BLUE),
            List.of(),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());
    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());

    assertEquals(List.of(CardColor.BUS, CardColor.BLUE), game.players().get(0).hand());
    assertEquals("P2", game.turnManager().currentPlayerId());
  }

  @Test
  void endingTransportationDrawAfterOneCardAdvancesTurnAndPublishesSnapshot() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE, CardColor.GREEN),
            List.of(),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);
    List<GameSnapshot> snapshots = new ArrayList<>();
    service.addListener(snapshots::add);

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());
    CommandResult result = service.endTransportCardDrawAction();

    assertTrue(result.isSuccess());
    assertEquals("P2", game.turnManager().currentPlayerId());
    assertFalse(service.getSnapshot().transportDrawActionActive());
    assertEquals(2, snapshots.size());
    assertEquals("P2", snapshots.get(1).currentPlayerId());
  }

  @Test
  void drawDestinationTicketsKeepsChosenTicketsAndReturnsOthersToBottom() {
    Board board = TestGameFactory.createSmallBoard();
    DestinationTicket first = ticket(board, "T1");
    DestinationTicket second = ticket(board, "T2");
    DestinationTicket third = ticket(board, "T3");
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE),
            List.of(),
            List.of(first, second, third),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(new DrawDestinationTicketsCommand("P1", List.of("T2")));

    assertTrue(result.isSuccess());
    assertEquals(List.of(second), game.players().get(0).tickets());
    assertEquals(List.of(third, first), game.destinationTicketDeck().ticketsSnapshot());
    assertEquals("P2", game.turnManager().currentPlayerId());
  }

  @Test
  void destinationTicketPreviewExposesNextTicketsWithoutMutatingDeck() {
    Board board = TestGameFactory.createSmallBoard();
    DestinationTicket first = ticket(board, "T1");
    DestinationTicket second = ticket(board, "T2");
    DestinationTicket third = ticket(board, "T3");
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE),
            List.of(),
            List.of(first, second, third),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    DestinationTicketDrawPreview preview = service.previewDestinationTickets("P1");

    assertTrue(preview.commandResult().isSuccess());
    assertEquals(List.of("T1", "T2"), preview.tickets().stream().map(ticket -> ticket.id()).toList());
    assertEquals(List.of(first, second, third), game.destinationTicketDeck().ticketsSnapshot());
  }

  @Test
  void drawDestinationTicketsRejectsInvalidKeepSelectionWithoutMutatingDeck() {
    Board board = TestGameFactory.createSmallBoard();
    DestinationTicket first = ticket(board, "T1");
    DestinationTicket second = ticket(board, "T2");
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE),
            List.of(),
            List.of(first, second),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    CommandResult result =
        service.executeCommand(new DrawDestinationTicketsCommand("P1", List.of("NOT_DRAWN")));

    assertTrue(result.isFailure());
    assertEquals(List.of(first, second), game.destinationTicketDeck().ticketsSnapshot());
    assertEquals("P1", game.turnManager().currentPlayerId());
  }

  @Test
  void undoAfterRouteClaimRestoresPreTurnState() {
    Game game = TestGameFactory.createSmallGame();
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    GameApplicationService service = new GameApplicationService(game);
    List<GameSnapshot> snapshots = new ArrayList<>();
    service.addListener(snapshots::add);

    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))))
            .isSuccess());
    assertTrue(service.canUndo());

    CommandResult result = service.undoLastTurn();

    assertTrue(result.isSuccess());
    assertFalse(service.canUndo());
    assertFalse(game.board().findRoute("R1").orElseThrow().isClaimed());
    assertEquals(List.of(CardColor.BLUE), game.players().get(0).hand());
    assertEquals(17, game.players().get(0).busesRemaining());
    assertEquals(0, game.players().get(0).score());
    assertEquals(0, game.transportCardDeck().discardPileSize());
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertEquals(GamePhase.RUNNING, game.phase());
    assertEquals(2, snapshots.size());
    assertFalse(snapshots.get(1).canUndo());
  }

  @Test
  void undoAfterDestinationTicketDrawRestoresTicketOwnershipAndDeckOrder() {
    Board board = TestGameFactory.createSmallBoard();
    DestinationTicket first = ticket(board, "T1");
    DestinationTicket second = ticket(board, "T2");
    DestinationTicket third = ticket(board, "T3");
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE),
            List.of(),
            List.of(first, second, third),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(
        service.executeCommand(new DrawDestinationTicketsCommand("P1", List.of("T2"))).isSuccess());

    assertTrue(service.undoLastTurn().isSuccess());

    assertEquals(List.of(), game.players().get(0).tickets());
    assertEquals(List.of(first, second, third), game.destinationTicketDeck().ticketsSnapshot());
    assertEquals("P1", game.turnManager().currentPlayerId());
  }

  @Test
  void undoAfterTwoCardTransportationDrawRestoresDecksAndFaceUpCards() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE, CardColor.GREEN, CardColor.BLACK),
            List.of(CardColor.PINK, CardColor.YELLOW),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.faceUp("P1", 0)).isSuccess());
    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());

    assertTrue(service.undoLastTurn().isSuccess());

    assertEquals(List.of(), game.players().get(0).hand());
    assertEquals(
        List.of(CardColor.BLUE, CardColor.GREEN, CardColor.BLACK),
        game.transportCardDeck().drawPileSnapshot());
    assertEquals(List.of(), game.transportCardDeck().discardPileSnapshot());
    assertEquals(List.of(CardColor.PINK, CardColor.YELLOW), game.faceUpDisplay().visibleCards());
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertFalse(service.getSnapshot().transportDrawActionActive());
  }

  @Test
  void undoAfterExplicitOneCardTransportationDrawEndRestoresPreTurnState() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE, CardColor.GREEN),
            List.of(),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());
    assertTrue(service.endTransportCardDrawAction().isSuccess());

    assertTrue(service.undoLastTurn().isSuccess());

    assertEquals(List.of(), game.players().get(0).hand());
    assertEquals(List.of(CardColor.BLUE, CardColor.GREEN), game.transportCardDeck().drawPileSnapshot());
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertFalse(service.getSnapshot().transportDrawActionActive());
  }

  @Test
  void undoHistoryRestoresLastTwoTurnsOnly() {
    Game game = TestGameFactory.createSmallGame();
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    game.players().get(1).addCards(List.of(CardColor.GREEN, CardColor.GREEN));
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))))
            .isSuccess());
    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand(
                    "P2", "R2", new CardPayment(List.of(CardColor.GREEN, CardColor.GREEN))))
            .isSuccess());

    assertTrue(service.undoLastTurn().isSuccess());
    assertFalse(game.board().findRoute("R2").orElseThrow().isClaimed());
    assertTrue(game.board().findRoute("R1").orElseThrow().isClaimed());
    assertEquals("P2", game.turnManager().currentPlayerId());

    assertTrue(service.undoLastTurn().isSuccess());
    assertFalse(game.board().findRoute("R1").orElseThrow().isClaimed());
    assertEquals("P1", game.turnManager().currentPlayerId());

    CommandResult thirdUndo = service.undoLastTurn();
    assertTrue(thirdUndo.isFailure());
    assertTrue(thirdUndo.message().contains("No completed turn"));
  }

  @Test
  void failedCommandDoesNotCreateUndoCheckpoint() {
    Game game = TestGameFactory.createSmallGame();
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand("P2", "R1", new CardPayment(List.of(CardColor.BLUE))))
            .isFailure());

    assertFalse(service.canUndo());
    assertTrue(service.undoLastTurn().isFailure());
  }

  @Test
  void undoIsRejectedDuringPartialTransportationDraw() {
    Game game =
        TestGameFactory.createSmallGame(
            List.of(CardColor.BLUE, CardColor.GREEN),
            List.of(),
            List.of(ticket(TestGameFactory.createSmallBoard(), "T1")),
            new FixedOrderShuffleStrategy());
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(service.executeCommand(DrawTransportCardCommand.blind("P1")).isSuccess());

    CommandResult result = service.undoLastTurn();

    assertTrue(result.isFailure());
    assertTrue(result.message().contains("Complete the transportation card draw"));
    assertEquals(List.of(CardColor.BLUE), game.players().get(0).hand());
    assertEquals("P1", game.turnManager().currentPlayerId());
  }

  @Test
  void undoFromFinalRoundRestoresPhaseAndFinalRoundCounters() {
    Game game = TestGameFactory.createSmallGame();
    game.players().get(0).useBuses(15);
    game.players().get(0).addCards(List.of(CardColor.BLUE));
    GameApplicationService service = new GameApplicationService(game);

    assertTrue(
        service
            .executeCommand(
                new ClaimRouteCommand("P1", "R1", new CardPayment(List.of(CardColor.BLUE))))
            .isSuccess());
    assertEquals(GamePhase.FINAL_ROUND, game.phase());
    assertTrue(game.turnManager().isFinalRoundActive());

    assertTrue(service.undoLastTurn().isSuccess());

    assertEquals(GamePhase.RUNNING, game.phase());
    assertFalse(game.turnManager().isFinalRoundActive());
    assertEquals(0, game.turnManager().finalTurnsRemaining());
    assertEquals(2, game.players().get(0).busesRemaining());
    assertEquals("P1", game.turnManager().currentPlayerId());
  }

  private static DestinationTicket ticket(Board board, String id) {
    return new DestinationTicket(
        id, board.findLocation("A").orElseThrow(), board.findLocation("C").orElseThrow(), 5);
  }

  private static Game createFerryGame() {
    Location first = new Location("A", "A", 1);
    Location second = new Location("B", "B", 1);
    Board board =
        new Board(
            List.of(first, second),
            List.of(
                new Route(
                    "F1",
                    first,
                    second,
                    3,
                    RouteColor.GREY,
                    RouteKind.FERRY,
                    1,
                    null,
                    new FerryRouteRequirement(RouteColor.GREY, 3, 1))));
    Player firstPlayer = new Player("P1", "Red", PlayerColor.RED);
    Player secondPlayer = new Player("P2", "Blue", PlayerColor.BLUE);
    return new Game(
        board,
        List.of(firstPlayer, secondPlayer),
        new TransportCardDeck(List.of(), new FixedOrderShuffleStrategy()),
        new FaceUpDisplay(List.of()),
        new DestinationTicketDeck(List.of()),
        new TurnManager(List.of("P1", "P2"), "P1"),
        GamePhase.RUNNING);
  }
}
