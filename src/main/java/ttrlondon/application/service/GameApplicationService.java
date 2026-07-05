package ttrlondon.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ttrlondon.application.commands.DrawTransportCardCommand;
import ttrlondon.application.commands.GameCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.DestinationTicketDrawPreview;
import ttrlondon.application.dto.DestinationTicketSnapshot;
import ttrlondon.application.dto.FinalScoreSnapshot;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.undo.GameMemento;
import ttrlondon.application.undo.GameMementoFactory;
import ttrlondon.application.undo.UndoHistory;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.scoring.LongestPathCalculator;
import ttrlondon.domain.scoring.RouteScoreTable;
import ttrlondon.domain.scoring.ScoreCalculator;
import ttrlondon.domain.scoring.TicketCompletionChecker;

/**
 * Application-layer entry point that executes commands and publishes immutable snapshots.
 */
public final class GameApplicationService {
  private final Game game;
  private final ScoreCalculator scoreCalculator;
  private final List<GameStateListener> listeners;
  private final UndoHistory undoHistory;
  private final GameMementoFactory mementoFactory;
  private final TransportDrawCoordinator transportDrawCoordinator;

  /**
   * Creates an application service around a game aggregate.
   *
   * @param game game aggregate
   */
  public GameApplicationService(Game game) {
    this(
        game,
        new ScoreCalculator(
            new RouteScoreTable(), new TicketCompletionChecker(), new LongestPathCalculator()));
  }

  /**
   * Creates an application service around a game aggregate with explicit scoring collaborators.
   *
   * @param game game aggregate
   * @param scoreCalculator final scoring calculator
   */
  public GameApplicationService(Game game, ScoreCalculator scoreCalculator) {
    this.game = Objects.requireNonNull(game, "game");
    this.scoreCalculator = Objects.requireNonNull(scoreCalculator, "scoreCalculator");
    this.listeners = new ArrayList<>();
    this.undoHistory = new UndoHistory();
    this.mementoFactory = new GameMementoFactory();
    this.transportDrawCoordinator =
        new TransportDrawCoordinator(this.game, mementoFactory, undoHistory);
  }

  /**
   * Executes an application command and publishes a snapshot after successful commands.
   *
   * @param command command to execute
   * @return command result
   */
  public CommandResult executeCommand(GameCommand command) {
    Objects.requireNonNull(command, "command");
    CommandResult result;
    if (command instanceof DrawTransportCardCommand drawCommand) {
      result = transportDrawCoordinator.execute(drawCommand);
    } else {
      if (transportDrawCoordinator.isActive()) {
        return CommandResult.failure("Complete the transportation card draw action first.");
      }
      GameMemento beforeTurn = captureMemento();
      result = command.execute(game);
      if (result.isSuccess()) {
        undoHistory.push(beforeTurn);
      }
    }
    if (result.isSuccess()) {
      publishSnapshot();
    }
    return result;
  }

  /**
   * Ends a partially completed transportation-card draw action and advances the turn.
   *
   * @return command result
   */
  public CommandResult endTransportCardDrawAction() {
    CommandResult result = transportDrawCoordinator.endAction();
    if (result.isSuccess()) {
      publishSnapshot();
    }
    return result;
  }

  /** Returns an immutable snapshot of current game state. */
  public GameSnapshot getSnapshot() {
    return GameSnapshot.from(game, transportDrawCoordinator.progress(), canUndo());
  }

  /**
   * Returns whether a completed turn can currently be undone.
   *
   * @return true when undo history exists and no partial draw is active
   */
  public boolean canUndo() {
    return undoHistory.canUndo() && !transportDrawCoordinator.isActive();
  }

  /**
   * Restores the game to the start of the most recent completed turn.
   *
   * @return command result describing the undo outcome
   */
  public CommandResult undoLastTurn() {
    if (transportDrawCoordinator.isActive()) {
      return CommandResult.failure("Complete the transportation card draw action before undoing.");
    }
    GameMemento memento = undoHistory.pop().orElse(null);
    if (memento == null) {
      return CommandResult.failure("No completed turn is available to undo.");
    }
    mementoFactory.restore(game, memento);
    transportDrawCoordinator.restoreProgress(memento.drawProgress());
    publishSnapshot();
    return CommandResult.success("Last turn undone.");
  }

  /**
   * Returns a read-only preview of destination tickets available to the active player.
   *
   * <p>This does not mutate the ticket deck. The final keep/return decision is still executed by
   * {@code DrawDestinationTicketsCommand}, which revalidates the chosen ticket identifiers.
   *
   * @param playerId active player requesting the ticket draw action
   * @return preview result and immutable ticket snapshots
   */
  public DestinationTicketDrawPreview previewDestinationTickets(String playerId) {
    CommandResult validation = validateDestinationTicketPreview(playerId);
    if (validation.isFailure()) {
      return DestinationTicketDrawPreview.failure(validation.message());
    }
    List<DestinationTicketSnapshot> tickets =
        game.destinationTicketDeck().ticketsSnapshot().stream()
            .limit(2)
            .map(DestinationTicketSnapshot::from)
            .toList();
    return DestinationTicketDrawPreview.success(tickets);
  }

  /** Returns final scoring details for the current game state. */
  public FinalScoreSnapshot finalScoreSnapshot() {
    return FinalScoreSnapshot.from(
        scoreCalculator.calculateFinalScore(game.board(), game.players()), game);
  }

  /**
   * Marks scoring as presented and moves the game to the terminal phase.
   *
   * @return command result for lifecycle completion
   */
  public CommandResult completeScoring() {
    if (game.phase() != GamePhase.SCORING) {
      return CommandResult.failure("Game is not in final scoring.");
    }
    game.transitionTo(GamePhase.GAME_OVER);
    publishSnapshot();
    return CommandResult.success("Game over.");
  }

  /**
   * Registers a listener for successful state changes.
   *
   * @param listener listener to add
   */
  public void addListener(GameStateListener listener) {
    listeners.add(Objects.requireNonNull(listener, "listener"));
  }

  /**
   * Removes a registered listener.
   *
   * @param listener listener to remove
   */
  public void removeListener(GameStateListener listener) {
    listeners.remove(listener);
  }

  private GameMemento captureMemento() {
    return mementoFactory.capture(game, transportDrawCoordinator.progress());
  }

  private void publishSnapshot() {
    GameSnapshot snapshot = getSnapshot();
    for (GameStateListener listener : List.copyOf(listeners)) {
      listener.onGameStateChanged(snapshot);
    }
  }

  private CommandResult validateDestinationTicketPreview(String playerId) {
    Objects.requireNonNull(playerId, "playerId");
    if (playerId.isBlank()) {
      return CommandResult.failure("Player does not exist.");
    }
    if (transportDrawCoordinator.isActive()) {
      return CommandResult.failure("Complete the transportation card draw action first.");
    }
    if (!game.acceptsPlayerActions()) {
      return CommandResult.failure("Game is not accepting player actions.");
    }
    if (!game.turnManager().isCurrentPlayer(playerId)) {
      return CommandResult.failure("It is not this player's turn.");
    }
    if (game.findPlayer(playerId).isEmpty()) {
      return CommandResult.failure("Player does not exist.");
    }
    if (game.destinationTicketDeck().size() == 0) {
      return CommandResult.failure("Destination ticket deck is empty.");
    }
    return CommandResult.success("Destination ticket draw is valid.");
  }

}
