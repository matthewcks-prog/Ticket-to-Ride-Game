package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/** Tests aggregate phase transitions driven by final-round turn sequencing. */
final class GamePhaseTransitionTest {
  @Test
  void gameTransitionsToScoringAfterAllFinalTurnsComplete() {
    Player first = new Player("p1", "Red", PlayerColor.RED);
    Player second = new Player("p2", "Blue", PlayerColor.BLUE);
    first.useBuses(15);
    Game game =
        new Game(
            new Board(List.of(), List.of()),
            List.of(first, second),
            new TransportCardDeck(List.of(), new FixedOrderShuffleStrategy()),
            new FaceUpDisplay(List.of()),
            new DestinationTicketDeck(List.of()),
            new TurnManager(List.of("p1", "p2"), "p1"),
            GamePhase.RUNNING);

    game.endCurrentTurn();

    assertEquals(GamePhase.FINAL_ROUND, game.phase());
    assertEquals("p2", game.turnManager().currentPlayerId());

    game.endCurrentTurn();
    game.endCurrentTurn();

    assertEquals(GamePhase.SCORING, game.phase());
  }
}
