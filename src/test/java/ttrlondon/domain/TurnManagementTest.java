package ttrlondon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.turn.TurnManager;

/** Tests turn order and final-round countdown sequencing. */
final class TurnManagementTest {
  @Test
  void turnAdvancesClockwiseAndValidatesCurrentPlayer() {
    TurnManager turnManager = new TurnManager(List.of("p1", "p2", "p3"), "p2");

    assertEquals("p2", turnManager.currentPlayerId());
    assertTrue(turnManager.isCurrentPlayer("p2"));
    assertFalse(turnManager.isCurrentPlayer("p1"));
    assertThrows(IllegalArgumentException.class, () -> turnManager.requireCurrentPlayer("p1"));

    turnManager.endTurn(3);
    assertEquals("p3", turnManager.currentPlayerId());
    turnManager.endTurn(17);
    assertEquals("p1", turnManager.currentPlayerId());
  }

  @Test
  void finalRoundTriggersAtZeroOneOrTwoBusesButNotThree() {
    assertFinalRoundTrigger(0);
    assertFinalRoundTrigger(1);
    assertFinalRoundTrigger(2);

    TurnManager turnManager = new TurnManager(List.of("p1", "p2"), "p1");
    turnManager.endTurn(3);

    assertFalse(turnManager.isFinalRoundActive());
  }

  @Test
  void finalRoundGivesEachPlayerOneAdditionalTurnIncludingTriggeringPlayer() {
    TurnManager turnManager = new TurnManager(List.of("p1", "p2"), "p2");

    turnManager.endTurn(2);

    assertTrue(turnManager.isFinalRoundActive());
    assertEquals("p2", turnManager.triggeringPlayerId());
    assertEquals(2, turnManager.finalTurnsRemaining());
    assertEquals("p1", turnManager.currentPlayerId());

    turnManager.endTurn(17);

    assertEquals(1, turnManager.finalTurnsRemaining());
    assertEquals("p2", turnManager.currentPlayerId());
    assertFalse(turnManager.isFinalRoundComplete());

    turnManager.endTurn(17);

    assertEquals(0, turnManager.finalTurnsRemaining());
    assertEquals("p2", turnManager.currentPlayerId());
    assertTrue(turnManager.isFinalRoundComplete());
  }

  private static void assertFinalRoundTrigger(int busesRemaining) {
    TurnManager turnManager = new TurnManager(List.of("p1", "p2"), "p1");

    turnManager.endTurn(busesRemaining);

    assertTrue(turnManager.isFinalRoundActive());
    assertEquals("p1", turnManager.triggeringPlayerId());
    assertEquals(2, turnManager.finalTurnsRemaining());
  }
}
