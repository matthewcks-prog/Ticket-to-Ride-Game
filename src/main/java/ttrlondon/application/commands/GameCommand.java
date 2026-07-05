package ttrlondon.application.commands;

import ttrlondon.application.dto.CommandResult;
import ttrlondon.domain.game.Game;

/**
 * Application command representing one player intention.
 */
public interface GameCommand {
  /**
   * Validates and executes the command against a game aggregate.
   *
   * @param game target game
   * @return command result
   */
  CommandResult execute(Game game);
}
