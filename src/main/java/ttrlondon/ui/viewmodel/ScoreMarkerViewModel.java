package ttrlondon.ui.viewmodel;

import java.util.Objects;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.player.PlayerColor;

/**
 * Immutable rendering model for a player's score marker on the board-edge score track.
 */
public final class ScoreMarkerViewModel {
  private final String playerId;
  private final PlayerColor playerColor;
  private final int score;
  private final NormalizedPoint position;

  /**
   * Creates a score marker rendering model.
   *
   * @param playerId player identifier
   * @param playerColor player's chosen bus colour
   * @param score current score
   * @param position normalized score-track position
   */
  public ScoreMarkerViewModel(
      String playerId, PlayerColor playerColor, int score, NormalizedPoint position) {
    this.playerId = Text.requireNonBlank(playerId, "playerId");
    this.playerColor = Objects.requireNonNull(playerColor, "playerColor");
    this.score = score;
    this.position = Objects.requireNonNull(position, "position");
  }

  /** Returns the player identifier. */
  public String playerId() {
    return playerId;
  }

  /** Returns the player's colour. */
  public PlayerColor playerColor() {
    return playerColor;
  }

  /** Returns the score displayed by this marker. */
  public int score() {
    return score;
  }

  /** Returns the marker's normalized board position. */
  public NormalizedPoint position() {
    return position;
  }
}
