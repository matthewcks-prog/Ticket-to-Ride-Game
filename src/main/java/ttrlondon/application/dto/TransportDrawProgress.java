package ttrlondon.application.dto;

import java.util.Optional;

/**
 * Immutable progress of an in-flight transportation-card draw action.
 *
 * <p>Groups the four values that previously travelled together as separate parameters across the
 * snapshot boundary: whether a draw action is active, which player owns it, how many cards have been
 * drawn, and which face-up slot (if any) is locked against the second draw.
 */
public final class TransportDrawProgress {
  /** Sentinel value meaning no face-up slot is locked. */
  public static final int NO_LOCKED_SLOT = -1;

  private final boolean active;
  private final String playerId;
  private final int drawsTaken;
  private final int lockedFaceUpIndex;

  /**
   * Creates a transportation draw progress value.
   *
   * @param active whether a draw action is currently in progress
   * @param playerId player resolving the draw action, or {@code null} when inactive
   * @param drawsTaken number of cards drawn so far in this action
   * @param lockedFaceUpIndex face-up slot unavailable as the second draw, or {@link #NO_LOCKED_SLOT}
   */
  public TransportDrawProgress(
      boolean active, String playerId, int drawsTaken, int lockedFaceUpIndex) {
    this.active = active;
    this.playerId = playerId;
    this.drawsTaken = drawsTaken;
    this.lockedFaceUpIndex = lockedFaceUpIndex;
  }

  /** Returns a progress value representing no active draw action. */
  public static TransportDrawProgress inactive() {
    return new TransportDrawProgress(false, null, 0, NO_LOCKED_SLOT);
  }

  /** Returns whether a transportation-card draw action is in progress. */
  public boolean isActive() {
    return active;
  }

  /** Returns the player resolving the draw action when present. */
  public Optional<String> playerId() {
    return Optional.ofNullable(playerId);
  }

  /** Returns how many transportation cards have been drawn in this action. */
  public int drawsTaken() {
    return drawsTaken;
  }

  /** Returns the locked face-up slot index, or {@link #NO_LOCKED_SLOT} when none is locked. */
  public int lockedFaceUpIndex() {
    return lockedFaceUpIndex;
  }
}
