package ttrlondon.application.commands;

import java.util.Optional;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.domain.card.CardColor;

/**
 * Result for a single transportation-card draw, including cards needed for draw orchestration.
 */
public final class DrawTransportCardResult {
  private final CommandResult commandResult;
  private final CardColor drawnCard;
  private final CardColor replacementCard;
  private final DrawSource source;
  private final int faceUpIndex;

  private DrawTransportCardResult(
      CommandResult commandResult,
      CardColor drawnCard,
      CardColor replacementCard,
      DrawSource source,
      int faceUpIndex) {
    this.commandResult = commandResult;
    this.drawnCard = drawnCard;
    this.replacementCard = replacementCard;
    this.source = source;
    this.faceUpIndex = faceUpIndex;
  }

  /**
   * Creates a successful draw result.
   *
   * @param drawnCard card drawn by the player
   * @param replacementCard card used to refill the face-up slot, or null
   * @param source draw source
   * @param faceUpIndex face-up slot index, or -1 for blind draw
   * @return successful draw result
   */
  public static DrawTransportCardResult success(
      CardColor drawnCard, CardColor replacementCard, DrawSource source, int faceUpIndex) {
    return new DrawTransportCardResult(
        CommandResult.success("Transportation card drawn."),
        drawnCard,
        replacementCard,
        source,
        faceUpIndex);
  }

  /**
   * Creates a failed draw result.
   *
   * @param message failure message
   * @return failed draw result
   */
  public static DrawTransportCardResult failure(String message) {
    return new DrawTransportCardResult(CommandResult.failure(message), null, null, null, -1);
  }

  /** Returns the public command result. */
  public CommandResult commandResult() {
    return commandResult;
  }

  /** Returns whether the draw succeeded. */
  public boolean isSuccess() {
    return commandResult.isSuccess();
  }

  /** Returns the card drawn by the player when successful. */
  public Optional<CardColor> drawnCard() {
    return Optional.ofNullable(drawnCard);
  }

  /** Returns the replacement face-up card when one was drawn. */
  public Optional<CardColor> replacementCard() {
    return Optional.ofNullable(replacementCard);
  }

  /** Returns the draw source when successful. */
  public Optional<DrawSource> source() {
    return Optional.ofNullable(source);
  }

  /** Returns the face-up slot index when the source was face-up. */
  public int faceUpIndex() {
    return faceUpIndex;
  }
}
