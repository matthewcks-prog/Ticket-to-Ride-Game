package ttrlondon.application.commands;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;

/**
 * Command that performs one transportation-card draw from either the deck or face-up display.
 */
public final class DrawTransportCardCommand implements GameCommand {
  private final String playerId;
  private final DrawSource source;
  private final int faceUpIndex;

  private DrawTransportCardCommand(String playerId, DrawSource source, int faceUpIndex) {
    this.playerId = Text.requireNonBlank(playerId, "playerId");
    this.source = Objects.requireNonNull(source, "source");
    this.faceUpIndex = faceUpIndex;
  }

  /**
   * Creates a blind-draw command.
   *
   * @param playerId drawing player identifier
   * @return blind-draw command
   */
  public static DrawTransportCardCommand blind(String playerId) {
    return new DrawTransportCardCommand(playerId, DrawSource.BLIND, -1);
  }

  /**
   * Creates a face-up draw command.
   *
   * @param playerId drawing player identifier
   * @param faceUpIndex face-up slot index
   * @return face-up draw command
   */
  public static DrawTransportCardCommand faceUp(String playerId, int faceUpIndex) {
    return new DrawTransportCardCommand(playerId, DrawSource.FACE_UP, faceUpIndex);
  }

  /** Returns the drawing player identifier. */
  public String playerId() {
    return playerId;
  }

  /** Returns the selected draw source. */
  public DrawSource source() {
    return source;
  }

  /** Returns the face-up index, or -1 for blind draws. */
  public int faceUpIndex() {
    return faceUpIndex;
  }

  @Override
  public CommandResult execute(Game game) {
    return executeDraw(game).commandResult();
  }

  /**
   * Executes a single draw and returns draw details needed by the application service.
   *
   * @param game target game
   * @return detailed draw result
   */
  public DrawTransportCardResult executeDraw(Game game) {
    Objects.requireNonNull(game, "game");
    CommandResult validation = validate(game);
    if (validation.isFailure()) {
      return DrawTransportCardResult.failure(validation.message());
    }

    Player player = findPlayer(game, playerId);
    if (source == DrawSource.BLIND) {
      Optional<CardColor> drawnCard = game.transportCardDeck().draw();
      if (drawnCard.isEmpty()) {
        return DrawTransportCardResult.failure("No transportation cards are available to draw.");
      }
      player.addCards(List.of(drawnCard.get()));
      return DrawTransportCardResult.success(drawnCard.get(), null, source, faceUpIndex);
    }

    CardColor drawnCard = game.faceUpDisplay().take(faceUpIndex);
    Optional<CardColor> replacement =
        game.faceUpDisplay().refillSlot(faceUpIndex, game.transportCardDeck());
    player.addCards(List.of(drawnCard));
    return DrawTransportCardResult.success(
        drawnCard, replacement.orElse(null), source, faceUpIndex);
  }

  private CommandResult validate(Game game) {
    if (!game.acceptsPlayerActions()) {
      return CommandResult.failure("Game is not accepting player actions.");
    }
    if (!game.turnManager().isCurrentPlayer(playerId)) {
      return CommandResult.failure("It is not this player's turn.");
    }
    if (game.findPlayer(playerId).isEmpty()) {
      return CommandResult.failure("Player does not exist.");
    }
    if (source == DrawSource.BLIND && !game.transportCardDeck().canDraw()) {
      return CommandResult.failure("No transportation cards are available for a blind draw.");
    }
    if (source == DrawSource.FACE_UP
        && (faceUpIndex < 0 || faceUpIndex >= game.faceUpDisplay().visibleCards().size())) {
      return CommandResult.failure("Face-up card slot does not exist.");
    }
    return CommandResult.success("Transportation card draw is valid.");
  }

  private static Player findPlayer(Game game, String playerId) {
    return game.findPlayer(playerId)
        .orElseThrow(() -> new IllegalArgumentException("player does not exist"));
  }
}
