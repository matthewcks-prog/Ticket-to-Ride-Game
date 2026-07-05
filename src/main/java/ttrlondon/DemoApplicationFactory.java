package ttrlondon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.rushhour.RushHourManager;
import ttrlondon.domain.rushhour.RushHourPhase;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.config.BoardFactory;
import ttrlondon.infrastructure.config.DeckFactory;
import ttrlondon.infrastructure.config.RushHourEventFactory;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/**
 * Creates a deterministic mid-game state for the Sprint 3 video demonstration.
 */
public final class DemoApplicationFactory {
  private static final String DEMO_RUSH_HOUR_EVENT_ID = "RH07";

  private DemoApplicationFactory() {}

  /**
   * Creates an application service around the deterministic demonstration game.
   *
   * @return application service ready to render in the Swing client
   */
  public static GameApplicationService createDemoApplicationService() {
    return new GameApplicationService(createDemoGame());
  }

  /**
   * Creates a deterministic game state focused on Sprint 3 extension demonstration.
   *
   * @return game with a Rush Hour affected ferry ready for P1 to claim
   */
  public static Game createDemoGame() {
    FixedOrderShuffleStrategy shuffleStrategy = new FixedOrderShuffleStrategy();
    Board board = BoardFactory.createLondonBoard();
    List<Player> players = createDemoPlayers(board);
    TransportCardDeck transportDeck =
        new TransportCardDeck(demoDrawPile(), shuffleStrategy);
    FaceUpDisplay faceUpDisplay =
        new FaceUpDisplay(
            List.of(
                CardColor.BUS,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW));
    DestinationTicketDeck ticketDeck = createDemoTicketDeck(board, players);
    RushHourManager rushHourManager =
        RushHourEventFactory.createLondonRushHourManager(shuffleStrategy);
    Game game =
        new Game(
            board,
            players,
            transportDeck,
            faceUpDisplay,
            ticketDeck,
            new TurnManager(players.stream().map(Player::id).toList(), "P1"),
            rushHourManager,
            GamePhase.RUNNING);
    game.rushHourManager()
        .restoreState(
            RushHourPhase.PEAK,
            null,
            DEMO_RUSH_HOUR_EVENT_ID,
            players.size(),
            remainingRushHourEventIds(),
            List.of(),
            Map.of());
    return game;
  }

  private static List<Player> createDemoPlayers(Board board) {
    Player playerOne = new Player("P1", "Matthew", PlayerColor.RED);
    playerOne.addCards(
        List.of(
            CardColor.BUS,
            CardColor.BLUE,
            CardColor.ORANGE,
            CardColor.GREEN,
            CardColor.BLACK));

    Player playerTwo = new Player("P2", "Assessor", PlayerColor.BLUE);
    playerTwo.addCards(List.of(CardColor.PINK, CardColor.YELLOW, CardColor.ORANGE));

    List<DestinationTicket> tickets = DeckFactory.createDestinationTickets(board);
    playerOne.addTickets(List.of(tickets.get(0)));
    playerTwo.addTickets(List.of(tickets.get(1)));
    return List.of(playerOne, playerTwo);
  }

  private static DestinationTicketDeck createDemoTicketDeck(Board board, List<Player> players) {
    List<DestinationTicket> remainingTickets =
        new ArrayList<>(DeckFactory.createDestinationTickets(board));
    remainingTickets.removeAll(players.get(0).tickets());
    remainingTickets.removeAll(players.get(1).tickets());
    return new DestinationTicketDeck(remainingTickets);
  }

  private static List<CardColor> demoDrawPile() {
    return List.of(
        CardColor.ORANGE,
        CardColor.BLUE,
        CardColor.GREEN,
        CardColor.BLACK,
        CardColor.PINK,
        CardColor.YELLOW,
        CardColor.ORANGE,
        CardColor.BLUE,
        CardColor.GREEN,
        CardColor.BLACK,
        CardColor.PINK,
        CardColor.YELLOW);
  }

  private static List<String> remainingRushHourEventIds() {
    return List.of("RH01", "RH02", "RH03", "RH04", "RH05", "RH06", "RH08");
  }
}
