package ttrlondon.domain.turn;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Owns player order, current turn, and final-round state.
 */
public final class TurnManager {
  private final List<String> playerOrder;
  private int currentIndex;
  private boolean finalRoundActive;
  private String triggeringPlayerId;
  private int finalTurnsRemaining;

  /**
   * Creates a turn manager.
   *
   * @param playerOrder player identifiers in clockwise order
   * @param startingPlayerId player identifier that takes the first turn
   */
  public TurnManager(List<String> playerOrder, String startingPlayerId) {
    Objects.requireNonNull(playerOrder, "playerOrder");
    if (playerOrder.isEmpty()) {
      throw new IllegalArgumentException("player order must not be empty");
    }
    this.playerOrder = List.copyOf(playerOrder);
    this.currentIndex = this.playerOrder.indexOf(startingPlayerId);
    if (currentIndex < 0) {
      throw new IllegalArgumentException("starting player must be in player order");
    }
  }

  /** Returns the current player identifier. */
  public String currentPlayerId() {
    return playerOrder.get(currentIndex);
  }

  /** Returns the clockwise player order. */
  public List<String> playerOrder() {
    return Collections.unmodifiableList(playerOrder);
  }

  /** Returns whether the supplied player is currently active. */
  public boolean isCurrentPlayer(String playerId) {
    return currentPlayerId().equals(playerId);
  }

  /** Rejects when the supplied player is not currently active. */
  public void requireCurrentPlayer(String playerId) {
    if (!isCurrentPlayer(playerId)) {
      throw new IllegalArgumentException("not the current player's turn");
    }
  }

  /** Advances to the next player without applying Phase 2 final-round rules. */
  public void advanceTurn() {
    currentIndex = (currentIndex + 1) % playerOrder.size();
  }

  /**
   * Ends the current player's turn and applies endgame trigger sequencing.
   *
   * @param currentPlayerBusesRemaining bus count after the completed action
   */
  public void endTurn(int currentPlayerBusesRemaining) {
    if (currentPlayerBusesRemaining < 0) {
      throw new IllegalArgumentException("bus count must not be negative");
    }
    if (finalRoundActive) {
      finalTurnsRemaining--;
      if (finalTurnsRemaining > 0) {
        advanceTurn();
      }
      return;
    }
    if (currentPlayerBusesRemaining <= 2) {
      finalRoundActive = true;
      triggeringPlayerId = currentPlayerId();
      finalTurnsRemaining = playerOrder.size();
    }
    advanceTurn();
  }

  /** Returns whether the final round has been triggered. */
  public boolean isFinalRoundActive() {
    return finalRoundActive;
  }

  /** Returns the player that triggered the final round, or null when inactive. */
  public String triggeringPlayerId() {
    return triggeringPlayerId;
  }

  /** Returns final turns remaining once final round is active. */
  public int finalTurnsRemaining() {
    return finalTurnsRemaining;
  }

  /** Returns whether all final turns have been completed. */
  public boolean isFinalRoundComplete() {
    return finalRoundActive && finalTurnsRemaining == 0;
  }

  /**
   * Restores turn sequencing from a trusted game memento.
   *
   * @param restoredCurrentPlayerId player whose turn should be active
   * @param restoredFinalRoundActive whether final round is active
   * @param restoredTriggeringPlayerId triggering player, or null when inactive
   * @param restoredFinalTurnsRemaining final turns remaining
   */
  public void restoreState(
      String restoredCurrentPlayerId,
      boolean restoredFinalRoundActive,
      String restoredTriggeringPlayerId,
      int restoredFinalTurnsRemaining) {
    int restoredCurrentIndex = playerOrder.indexOf(restoredCurrentPlayerId);
    if (restoredCurrentIndex < 0) {
      throw new IllegalArgumentException("restored current player must be in player order");
    }
    if (restoredFinalTurnsRemaining < 0) {
      throw new IllegalArgumentException("final turns remaining must not be negative");
    }
    if (!restoredFinalRoundActive
        && (restoredTriggeringPlayerId != null || restoredFinalTurnsRemaining != 0)) {
      throw new IllegalArgumentException("inactive final round cannot have final-round state");
    }
    if (restoredFinalRoundActive && !playerOrder.contains(restoredTriggeringPlayerId)) {
      throw new IllegalArgumentException("triggering player must be in player order");
    }
    currentIndex = restoredCurrentIndex;
    finalRoundActive = restoredFinalRoundActive;
    triggeringPlayerId = restoredTriggeringPlayerId;
    finalTurnsRemaining = restoredFinalTurnsRemaining;
  }
}
