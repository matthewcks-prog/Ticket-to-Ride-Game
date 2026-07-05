package ttrlondon.application.service;

import java.util.Objects;
import ttrlondon.application.commands.DrawSource;
import ttrlondon.application.commands.DrawTransportCardCommand;
import ttrlondon.application.commands.DrawTransportCardResult;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.TransportDrawProgress;
import ttrlondon.application.undo.GameMemento;
import ttrlondon.application.undo.GameMementoFactory;
import ttrlondon.application.undo.UndoHistory;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.game.Game;

/**
 * Coordinates the multi-step transportation-card draw action for the application layer.
 */
final class TransportDrawCoordinator {
  private final Game game;
  private final GameMementoFactory mementoFactory;
  private final UndoHistory undoHistory;
  private DrawActionState drawActionState;
  private GameMemento pendingDrawActionMemento;

  TransportDrawCoordinator(
      Game game, GameMementoFactory mementoFactory, UndoHistory undoHistory) {
    this.game = Objects.requireNonNull(game, "game");
    this.mementoFactory = Objects.requireNonNull(mementoFactory, "mementoFactory");
    this.undoHistory = Objects.requireNonNull(undoHistory, "undoHistory");
    this.drawActionState = DrawActionState.inactive();
  }

  CommandResult execute(DrawTransportCardCommand command) {
    CommandResult stateValidation = validateDrawState(command);
    if (stateValidation.isFailure()) {
      return stateValidation;
    }

    GameMemento beforeTurn =
        drawActionState.isActive() ? pendingDrawActionMemento : captureMemento();
    DrawTransportCardResult drawResult = command.executeDraw(game);
    if (!drawResult.isSuccess()) {
      return drawResult.commandResult();
    }

    if (!drawActionState.isActive()) {
      drawActionState = DrawActionState.started(command.playerId());
      pendingDrawActionMemento = beforeTurn;
    }
    drawActionState = drawActionState.recordDraw();

    if (shouldCompleteAfter(drawResult)) {
      completeDrawAction();
      pushPendingDrawMemento();
    } else {
      drawActionState = drawActionState.withLockedFaceUpIndex(lockedSlotFrom(drawResult));
      if (!canDrawAnotherTransportationCard()) {
        completeDrawAction();
        pushPendingDrawMemento();
      }
    }

    return drawResult.commandResult();
  }

  CommandResult endAction() {
    if (!drawActionState.isActive()) {
      return CommandResult.failure("No transportation card draw action is in progress.");
    }
    GameMemento beforeTurn = pendingDrawActionMemento;
    completeDrawAction();
    if (beforeTurn != null) {
      undoHistory.push(beforeTurn);
    }
    pendingDrawActionMemento = null;
    return CommandResult.success("Transportation card draw action ended.");
  }

  boolean isActive() {
    return drawActionState.isActive();
  }

  TransportDrawProgress progress() {
    return drawActionState.toProgress();
  }

  void restoreProgress(TransportDrawProgress progress) {
    drawActionState = DrawActionState.from(progress);
    pendingDrawActionMemento = null;
  }

  private GameMemento captureMemento() {
    return mementoFactory.capture(game, progress());
  }

  private void pushPendingDrawMemento() {
    if (pendingDrawActionMemento != null) {
      undoHistory.push(pendingDrawActionMemento);
      pendingDrawActionMemento = null;
    }
  }

  private CommandResult validateDrawState(DrawTransportCardCommand command) {
    if (!drawActionState.isActive()) {
      return CommandResult.success("Transportation draw state is valid.");
    }
    if (!drawActionState.playerId().equals(command.playerId())) {
      return CommandResult.failure("Complete the active player's transportation card draw first.");
    }
    if (drawActionState.drawsTaken() >= 2) {
      return CommandResult.failure("Transportation card draw action is already complete.");
    }
    if (command.source() == DrawSource.FACE_UP) {
      if (drawActionState.lockedFaceUpIndex() == command.faceUpIndex()) {
        return CommandResult.failure(
            "That replacement Bus card cannot be taken as the second draw.");
      }
      if (game.faceUpDisplay().isBusAt(command.faceUpIndex())) {
        return CommandResult.failure("A face-up Bus card cannot be taken as the second draw.");
      }
    }
    return CommandResult.success("Transportation draw state is valid.");
  }

  private boolean shouldCompleteAfter(DrawTransportCardResult drawResult) {
    if (drawActionState.drawsTaken() >= 2) {
      return true;
    }
    return drawResult.source().filter(source -> source == DrawSource.FACE_UP).isPresent()
        && drawResult.drawnCard().filter(card -> card == CardColor.BUS).isPresent();
  }

  private int lockedSlotFrom(DrawTransportCardResult drawResult) {
    if (drawActionState.drawsTaken() != 1) {
      return DrawActionState.NO_LOCKED_SLOT;
    }
    if (drawResult.source().filter(source -> source == DrawSource.FACE_UP).isEmpty()) {
      return DrawActionState.NO_LOCKED_SLOT;
    }
    if (drawResult.replacementCard().filter(card -> card == CardColor.BUS).isEmpty()) {
      return DrawActionState.NO_LOCKED_SLOT;
    }
    if (!game.faceUpDisplay().isBusAt(drawResult.faceUpIndex())) {
      return DrawActionState.NO_LOCKED_SLOT;
    }
    return drawResult.faceUpIndex();
  }

  private boolean canDrawAnotherTransportationCard() {
    if (!drawActionState.isActive() || drawActionState.drawsTaken() >= 2) {
      return false;
    }
    return game.transportCardDeck().canDraw()
        || game.faceUpDisplay().hasNonBusCardOutsideSlot(drawActionState.lockedFaceUpIndex());
  }

  private void completeDrawAction() {
    game.endCurrentTurn();
    drawActionState = DrawActionState.inactive();
  }

  private static final class DrawActionState {
    private static final int NO_LOCKED_SLOT = TransportDrawProgress.NO_LOCKED_SLOT;

    private final String playerId;
    private final int drawsTaken;
    private final int lockedFaceUpIndex;

    private DrawActionState(String playerId, int drawsTaken, int lockedFaceUpIndex) {
      this.playerId = playerId;
      this.drawsTaken = drawsTaken;
      this.lockedFaceUpIndex = lockedFaceUpIndex;
    }

    private static DrawActionState inactive() {
      return new DrawActionState(null, 0, NO_LOCKED_SLOT);
    }

    private static DrawActionState started(String playerId) {
      return new DrawActionState(playerId, 0, NO_LOCKED_SLOT);
    }

    private boolean isActive() {
      return playerId != null;
    }

    private String playerId() {
      return playerId;
    }

    private int drawsTaken() {
      return drawsTaken;
    }

    private int lockedFaceUpIndex() {
      return lockedFaceUpIndex;
    }

    private DrawActionState recordDraw() {
      return new DrawActionState(playerId, drawsTaken + 1, lockedFaceUpIndex);
    }

    private DrawActionState withLockedFaceUpIndex(int lockedFaceUpIndex) {
      return new DrawActionState(playerId, drawsTaken, lockedFaceUpIndex);
    }

    private TransportDrawProgress toProgress() {
      return new TransportDrawProgress(isActive(), playerId, drawsTaken, lockedFaceUpIndex);
    }

    private static DrawActionState from(TransportDrawProgress progress) {
      Objects.requireNonNull(progress, "progress");
      return progress.isActive()
          ? new DrawActionState(
              progress.playerId().orElseThrow(),
              progress.drawsTaken(),
              progress.lockedFaceUpIndex())
          : inactive();
    }
  }
}
