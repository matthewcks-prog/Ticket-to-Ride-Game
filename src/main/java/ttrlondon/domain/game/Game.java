package ttrlondon.domain.game;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.rushhour.RushHourManager;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;

/**
 * Aggregate root for a Ticket to Ride: London game session.
 */
public final class Game {
  private final Board board;
  private final List<Player> players;
  private final TransportCardDeck transportCardDeck;
  private final FaceUpDisplay faceUpDisplay;
  private final DestinationTicketDeck destinationTicketDeck;
  private final TurnManager turnManager;
  private final RushHourManager rushHourManager;
  private GamePhase phase;

  /**
   * Creates a game aggregate.
   *
   * @param board game board
   * @param players players in the game
   * @param transportCardDeck shared transportation card deck
   * @param faceUpDisplay shared face-up display
   * @param destinationTicketDeck shared destination ticket deck
   * @param turnManager turn manager
   * @param phase initial game phase
   */
  public Game(
      Board board,
      List<Player> players,
      TransportCardDeck transportCardDeck,
      FaceUpDisplay faceUpDisplay,
      DestinationTicketDeck destinationTicketDeck,
      TurnManager turnManager,
      GamePhase phase) {
    this(
        board,
        players,
        transportCardDeck,
        faceUpDisplay,
        destinationTicketDeck,
        turnManager,
        new RushHourManager(List.of(), List.of(), List::copyOf),
        phase);
  }

  /**
   * Creates a game aggregate.
   *
   * @param board game board
   * @param players players in the game
   * @param transportCardDeck shared transportation card deck
   * @param faceUpDisplay shared face-up display
   * @param destinationTicketDeck shared destination ticket deck
   * @param turnManager turn manager
   * @param rushHourManager Rush Hour event manager
   * @param phase initial game phase
   */
  public Game(
      Board board,
      List<Player> players,
      TransportCardDeck transportCardDeck,
      FaceUpDisplay faceUpDisplay,
      DestinationTicketDeck destinationTicketDeck,
      TurnManager turnManager,
      RushHourManager rushHourManager,
      GamePhase phase) {
    this.board = Objects.requireNonNull(board, "board");
    this.players = List.copyOf(players);
    this.transportCardDeck = Objects.requireNonNull(transportCardDeck, "transportCardDeck");
    this.faceUpDisplay = Objects.requireNonNull(faceUpDisplay, "faceUpDisplay");
    this.destinationTicketDeck =
        Objects.requireNonNull(destinationTicketDeck, "destinationTicketDeck");
    this.turnManager = Objects.requireNonNull(turnManager, "turnManager");
    this.rushHourManager = Objects.requireNonNull(rushHourManager, "rushHourManager");
    this.phase = Objects.requireNonNull(phase, "phase");
    this.rushHourManager.start(this.players.size());
  }

  /** Returns the board. */
  public Board board() {
    return board;
  }

  /** Returns players in turn order. */
  public List<Player> players() {
    return Collections.unmodifiableList(players);
  }

  /** Returns the shared transportation card deck. */
  public TransportCardDeck transportCardDeck() {
    return transportCardDeck;
  }

  /** Returns the shared face-up display. */
  public FaceUpDisplay faceUpDisplay() {
    return faceUpDisplay;
  }

  /** Returns the shared destination ticket deck. */
  public DestinationTicketDeck destinationTicketDeck() {
    return destinationTicketDeck;
  }

  /** Returns the turn manager. */
  public TurnManager turnManager() {
    return turnManager;
  }

  /** Returns the Rush Hour event manager. */
  public RushHourManager rushHourManager() {
    return rushHourManager;
  }

  /** Returns the current game phase. */
  public GamePhase phase() {
    return phase;
  }

  /** Returns whether the game is currently accepting player turn actions. */
  public boolean acceptsPlayerActions() {
    return phase == GamePhase.RUNNING || phase == GamePhase.FINAL_ROUND;
  }

  /** Advances the aggregate to the supplied phase. */
  public void transitionTo(GamePhase nextPhase) {
    phase = Objects.requireNonNull(nextPhase, "nextPhase");
  }

  /** Ends the current turn and advances the game phase when final-round rules require it. */
  public void endCurrentTurn() {
    Player currentPlayer = currentPlayer();
    turnManager.endTurn(currentPlayer.busesRemaining());
    rushHourManager.advanceAfterCompletedTurn(players.size());
    if (turnManager.isFinalRoundComplete()) {
      transitionTo(GamePhase.SCORING);
    } else if (turnManager.isFinalRoundActive() && phase == GamePhase.RUNNING) {
      transitionTo(GamePhase.FINAL_ROUND);
    }
  }

  /** Returns the player whose turn is currently active. */
  public Player currentPlayer() {
    String currentPlayerId = turnManager.currentPlayerId();
    for (Player player : players) {
      if (player.id().equals(currentPlayerId)) {
        return player;
      }
    }
    throw new IllegalStateException("current player is not part of the game");
  }

  /**
   * Finds a player in this game by stable identifier.
   *
   * @param playerId player identifier
   * @return matching player when present
   */
  public Optional<Player> findPlayer(String playerId) {
    Objects.requireNonNull(playerId, "playerId");
    for (Player player : players) {
      if (player.id().equals(playerId)) {
        return Optional.of(player);
      }
    }
    return Optional.empty();
  }
}
