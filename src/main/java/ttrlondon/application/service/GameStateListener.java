package ttrlondon.application.service;

import ttrlondon.application.dto.GameSnapshot;

/**
 * Observer notified when application commands successfully change game state.
 */
public interface GameStateListener {
  /**
   * Receives the latest immutable game snapshot.
   *
   * @param snapshot updated game state
   */
  void onGameStateChanged(GameSnapshot snapshot);
}
