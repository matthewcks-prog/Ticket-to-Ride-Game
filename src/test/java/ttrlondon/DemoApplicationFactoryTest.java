package ttrlondon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.commands.ClaimRoutePayment;
import ttrlondon.application.commands.DrawTransportCardCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.rushhour.RushHourPhase;

/** Verifies the deterministic Sprint 3 demo setup. */
final class DemoApplicationFactoryTest {
  @Test
  void demoStateSupportsFerryRushHourAndTwoTurnUndo() {
    GameApplicationService service = DemoApplicationFactory.createDemoApplicationService();
    GameSnapshot initial = service.getSnapshot();

    assertEquals("P1", initial.currentPlayerId());
    assertEquals(RushHourPhase.PEAK, initial.rushHourPhase());
    assertEquals("RH07", initial.activeRushHourEvent().orElseThrow().id());
    assertTrue(initial.rushHourAffectedRouteIds().contains("R28"));
    assertEquals(CardColor.BUS, initial.faceUpCards().get(0));
    assertFalse(initial.canUndo());

    CommandResult ferryClaim =
        service.executeCommand(
            new ClaimRouteCommand(
                "P1",
                "R28",
                new ClaimRoutePayment(
                    new CardPayment(List.of(CardColor.BUS)),
                    new CardPayment(List.of(CardColor.BLUE)))));
    assertTrue(ferryClaim.isSuccess(), ferryClaim.message());

    GameSnapshot afterClaim = service.getSnapshot();
    assertEquals("P2", afterClaim.currentPlayerId());
    assertTrue(afterClaim.canUndo());
    assertEquals(3, scoreFor(afterClaim, "P1"));
    assertEquals(2, afterClaim.rushHourPointsByPlayerId().get("P1"));
    assertTrue(routeClaimedBy(afterClaim, "R28", "P1"));

    CommandResult busDraw = service.executeCommand(DrawTransportCardCommand.faceUp("P2", 0));
    assertTrue(busDraw.isSuccess(), busDraw.message());
    assertEquals("P1", service.getSnapshot().currentPlayerId());

    assertTrue(service.undoLastTurn().isSuccess());
    assertEquals("P2", service.getSnapshot().currentPlayerId());
    assertTrue(routeClaimedBy(service.getSnapshot(), "R28", "P1"));

    assertTrue(service.undoLastTurn().isSuccess());
    GameSnapshot afterSecondUndo = service.getSnapshot();
    assertEquals("P1", afterSecondUndo.currentPlayerId());
    assertFalse(routeClaimedBy(afterSecondUndo, "R28", "P1"));
    assertEquals(0, scoreFor(afterSecondUndo, "P1"));
  }

  private static int scoreFor(GameSnapshot snapshot, String playerId) {
    return snapshot.players().stream()
        .filter(player -> player.id().equals(playerId))
        .findFirst()
        .orElseThrow()
        .score();
  }

  private static boolean routeClaimedBy(GameSnapshot snapshot, String routeId, String playerId) {
    return snapshot.routes().stream()
        .filter(route -> route.id().equals(routeId))
        .findFirst()
        .orElseThrow()
        .claimedBy()
        .filter(playerId::equals)
        .isPresent();
  }
}
