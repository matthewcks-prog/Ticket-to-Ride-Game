package ttrlondon.testsupport;

import java.util.List;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.ColouredRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.random.ShuffleStrategy;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/**
 * Test-only factory for small deterministic game scenarios.
 */
public final class TestGameFactory {
  private TestGameFactory() {}

  /**
   * Creates a two-player game with a minimal board and injected card supplies.
   *
   * @param transportCards draw pile cards in top-to-bottom order
   * @param visibleCards initial face-up display cards
   * @param tickets destination ticket draw pile
   * @param shuffleStrategy shuffle strategy for transportation reshuffles
   * @return deterministic game for domain and application tests
   */
  public static Game createSmallGame(
      List<CardColor> transportCards,
      List<CardColor> visibleCards,
      List<DestinationTicket> tickets,
      ShuffleStrategy shuffleStrategy) {
    Board board = createSmallBoard();
    Player first = new Player("P1", "Red", PlayerColor.RED);
    Player second = new Player("P2", "Blue", PlayerColor.BLUE);
    return new Game(
        board,
        List.of(first, second),
        new TransportCardDeck(transportCards, shuffleStrategy),
        new FaceUpDisplay(visibleCards),
        new DestinationTicketDeck(tickets),
        new TurnManager(List.of(first.id(), second.id()), first.id()),
        GamePhase.RUNNING);
  }

  /**
   * Creates a two-player game with deterministic fixed-order shuffling.
   *
   * @return deterministic game with non-empty transport and ticket supplies
   */
  public static Game createSmallGame() {
    Board board = createSmallBoard();
    DestinationTicket ticket =
        new DestinationTicket(
            "T1",
            board.findLocation("A").orElseThrow(),
            board.findLocation("C").orElseThrow(),
            5);
    return createSmallGame(
        List.of(CardColor.BLUE, CardColor.GREEN, CardColor.BLACK),
        List.of(CardColor.PINK, CardColor.YELLOW, CardColor.ORANGE),
        List.of(ticket),
        new FixedOrderShuffleStrategy());
  }

  /** Creates a three-location board suitable for focused tests. */
  public static Board createSmallBoard() {
    Location first = new Location("A", "A", 1);
    Location second = new Location("B", "B", 1);
    Location third = new Location("C", "C", 1);
    return new Board(
        List.of(first, second, third),
        List.of(
            new Route(
                "R1",
                first,
                second,
                1,
                RouteColor.BLUE,
                null,
                new ColouredRouteRequirement(RouteColor.BLUE, 1)),
            new Route(
                "R2",
                second,
                third,
                2,
                RouteColor.GREEN,
                null,
                new ColouredRouteRequirement(RouteColor.GREEN, 2))));
  }
}
