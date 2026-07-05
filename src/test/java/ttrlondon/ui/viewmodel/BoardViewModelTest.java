package ttrlondon.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.domain.game.Game;
import ttrlondon.infrastructure.config.GameFactory;

final class BoardViewModelTest {
  @Test
  void createsRenderableBoardGeometryForCompleteLondonSnapshot() {
    Game game = GameFactory.createNewGame(List.of("Player 1", "Player 2"));
    BoardViewModel boardViewModel = BoardViewModel.from(GameSnapshot.from(game));

    assertEquals(17, boardViewModel.locations().size());
    assertEquals(43, boardViewModel.routes().size());
    assertEquals(2, boardViewModel.scoreMarkers().size());
    assertTrue(
        boardViewModel.locations().stream()
            .allMatch(location -> location.position().x() >= 0.0 && location.position().x() <= 1.0));
    assertTrue(
        boardViewModel.locations().stream()
            .allMatch(location -> location.position().y() >= 0.0 && location.position().y() <= 1.0));
  }

  @Test
  void appliesParallelLaneOffsetsForDoubleRoutes() {
    Game game = GameFactory.createNewGame(List.of("Player 1", "Player 2"));
    BoardViewModel boardViewModel = BoardViewModel.from(GameSnapshot.from(game));

    BoardRouteViewModel greenParallel = route(boardViewModel, "R12");
    BoardRouteViewModel yellowParallel = route(boardViewModel, "R13");
    BoardRouteViewModel stPaulsTowerPink = route(boardViewModel, "R33");
    BoardRouteViewModel stPaulsTowerYellow = route(boardViewModel, "R34");
    BoardRouteViewModel hydeParkPiccadillyUpper = route(boardViewModel, "R16");
    BoardRouteViewModel hydeParkPiccadillyLower = route(boardViewModel, "R41");

    assertEquals(-1, greenParallel.laneOffset());
    assertEquals(1, yellowParallel.laneOffset());
    assertEquals(1, stPaulsTowerPink.laneOffset());
    assertEquals(-1, stPaulsTowerYellow.laneOffset());
    assertEquals(-1, hydeParkPiccadillyUpper.laneOffset());
    assertEquals(1, hydeParkPiccadillyLower.laneOffset());
  }

  @Test
  void exposesFerryRouteMetadataForRendering() {
    Game game = GameFactory.createNewGame(List.of("Player 1", "Player 2"));
    BoardViewModel boardViewModel = BoardViewModel.from(GameSnapshot.from(game));

    BoardRouteViewModel ferry = route(boardViewModel, "R39");
    BoardRouteViewModel standard = route(boardViewModel, "R27");

    assertTrue(ferry.isFerry());
    assertEquals(1, ferry.requiredBusSymbols());
    assertEquals(0, standard.requiredBusSymbols());
  }

  private BoardRouteViewModel route(BoardViewModel boardViewModel, String routeId) {
    return boardViewModel.routes().stream()
        .filter(route -> route.id().equals(routeId))
        .findFirst()
        .orElseThrow();
  }
}
