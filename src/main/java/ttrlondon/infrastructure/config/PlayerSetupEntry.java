package ttrlondon.infrastructure.config;

import java.util.Objects;
import ttrlondon.domain.player.PlayerColor;

/**
 * One player's pre-game name and colour choice collected before a match starts.
 */
public record PlayerSetupEntry(String name, PlayerColor color) {
  /**
   * Creates a player setup entry.
   *
   * @param name display name
   * @param color player bus colour
   */
  public PlayerSetupEntry {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(color, "color");
  }
}
