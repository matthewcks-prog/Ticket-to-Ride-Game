package ttrlondon.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;

/** Tests player setup validation and roster creation. */
final class PlayerSetupConfigurationTest {
  @Test
  void createPlayersAcceptsValidTwoPlayerRoster() {
    List<Player> players =
        PlayerSetupConfiguration.createPlayers(
            List.of(
                new PlayerSetupEntry("Ada", PlayerColor.RED),
                new PlayerSetupEntry("Grace", PlayerColor.WHITE)));

    assertEquals(2, players.size());
    assertEquals("Ada", players.get(0).name());
    assertEquals(PlayerColor.RED, players.get(0).color());
    assertEquals("Grace", players.get(1).name());
    assertEquals(PlayerColor.WHITE, players.get(1).color());
  }

  @Test
  void createPlayersRejectsBlankNames() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                PlayerSetupConfiguration.createPlayers(
                    List.of(
                        new PlayerSetupEntry("   ", PlayerColor.RED),
                        new PlayerSetupEntry("Grace", PlayerColor.WHITE))));

    assertEquals("Player names must not be blank.", exception.getMessage());
  }

  @Test
  void createPlayersRejectsDuplicateColours() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                PlayerSetupConfiguration.createPlayers(
                    List.of(
                        new PlayerSetupEntry("Ada", PlayerColor.RED),
                        new PlayerSetupEntry("Grace", PlayerColor.RED))));

    assertEquals("Player colours must be unique.", exception.getMessage());
  }

  @Test
  void createPlayersRejectsTooFewPlayers() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                PlayerSetupConfiguration.createPlayers(
                    List.of(new PlayerSetupEntry("Ada", PlayerColor.RED))));

    assertEquals("London supports two to four players", exception.getMessage());
  }

  @Test
  void createPlayersRejectsTooManyPlayers() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                PlayerSetupConfiguration.createPlayers(
                    List.of(
                        new PlayerSetupEntry("P1", PlayerColor.RED),
                        new PlayerSetupEntry("P2", PlayerColor.WHITE),
                        new PlayerSetupEntry("P3", PlayerColor.BLUE),
                        new PlayerSetupEntry("P4", PlayerColor.YELLOW),
                        new PlayerSetupEntry("P5", PlayerColor.RED))));

    assertEquals("London supports two to four players", exception.getMessage());
  }
}
