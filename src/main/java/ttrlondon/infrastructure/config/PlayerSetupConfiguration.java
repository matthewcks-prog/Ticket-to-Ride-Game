package ttrlondon.infrastructure.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;

/**
 * Validates and materialises player rosters for London setup flows.
 *
 * <p>Centralises player-count limits and uniqueness rules so Swing setup UI and factories share
 * one source of truth.
 */
public final class PlayerSetupConfiguration {
  /** Minimum supported player count for London. */
  public static final int MIN_PLAYERS = 2;

  /** Maximum supported player count for London. */
  public static final int MAX_PLAYERS = 4;

  private PlayerSetupConfiguration() {}

  /**
   * Creates configured players from setup entries.
   *
   * @param entries player names and colours in clockwise order
   * @return validated players with generated identifiers {@code P1}..{@code Pn}
   * @throws IllegalArgumentException when count, names, or colours are invalid
   */
  public static List<Player> createPlayers(List<PlayerSetupEntry> entries) {
    Objects.requireNonNull(entries, "entries");
    if (entries.size() < MIN_PLAYERS || entries.size() > MAX_PLAYERS) {
      throw new IllegalArgumentException("London supports two to four players");
    }
    EnumSet<PlayerColor> usedColors = EnumSet.noneOf(PlayerColor.class);
    List<Player> players = new ArrayList<>();
    for (int index = 0; index < entries.size(); index++) {
      PlayerSetupEntry entry = entries.get(index);
      String name = entry.name().trim();
      if (name.isBlank()) {
        throw new IllegalArgumentException("Player names must not be blank.");
      }
      if (!usedColors.add(entry.color())) {
        throw new IllegalArgumentException("Player colours must be unique.");
      }
      players.add(new Player("P" + (index + 1), name, entry.color()));
    }
    return List.copyOf(players);
  }
}
