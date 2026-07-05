package ttrlondon.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.rushhour.RushHourPhase;

/**
 * Immutable read model of the game state exposed to application clients and UI.
 */
public final class GameSnapshot {
  private final GamePhase phase;
  private final String currentPlayerId;
  private final List<String> playerOrder;
  private final List<PlayerSnapshot> players;
  private final List<LocationSnapshot> locations;
  private final List<RouteSnapshot> routes;
  private final List<CardColor> faceUpCards;
  private final int transportDrawPileSize;
  private final int transportDiscardPileSize;
  private final int destinationTicketDeckSize;
  private final boolean finalRoundActive;
  private final String triggeringPlayerId;
  private final int finalTurnsRemaining;
  private final TransportDrawProgress drawProgress;
  private final boolean canUndo;
  private final RushHourPhase rushHourPhase;
  private final int rushHourTurnsRemaining;
  private final RushHourEventSnapshot forecastRushHourEvent;
  private final RushHourEventSnapshot activeRushHourEvent;
  private final List<String> rushHourAffectedRouteIds;
  private final Map<String, Integer> rushHourPointsByPlayerId;

  /**
   * Creates a game snapshot.
   *
   * @param phase current game phase
   * @param currentPlayerId active player identifier
   * @param playerOrder clockwise player order
   * @param players player snapshots
   * @param locations location snapshots
   * @param routes route snapshots
   * @param faceUpCards visible transportation cards
   * @param transportDrawPileSize number of face-down transportation cards
   * @param transportDiscardPileSize number of discarded transportation cards
   * @param destinationTicketDeckSize number of destination tickets left in deck
   * @param finalRoundActive whether final round is active
   * @param triggeringPlayerId player that triggered final round, or null
   * @param finalTurnsRemaining final turns remaining
   * @param drawProgress in-flight transportation-card draw progress
   * @param canUndo whether undo history is available
   * @param rushHourPhase current Rush Hour phase
   * @param rushHourTurnsRemaining turns remaining in the current Rush Hour phase
   * @param forecastRushHourEvent forecast event, or null
   * @param activeRushHourEvent active event, or null
   * @param rushHourAffectedRouteIds currently visible affected route ids
   * @param rushHourPointsByPlayerId Rush Hour bonus points by player id
   */
  public GameSnapshot(
      GamePhase phase,
      String currentPlayerId,
      List<String> playerOrder,
      List<PlayerSnapshot> players,
      List<LocationSnapshot> locations,
      List<RouteSnapshot> routes,
      List<CardColor> faceUpCards,
      int transportDrawPileSize,
      int transportDiscardPileSize,
      int destinationTicketDeckSize,
      boolean finalRoundActive,
      String triggeringPlayerId,
      int finalTurnsRemaining,
      TransportDrawProgress drawProgress,
      boolean canUndo,
      RushHourPhase rushHourPhase,
      int rushHourTurnsRemaining,
      RushHourEventSnapshot forecastRushHourEvent,
      RushHourEventSnapshot activeRushHourEvent,
      List<String> rushHourAffectedRouteIds,
      Map<String, Integer> rushHourPointsByPlayerId) {
    this.phase = Objects.requireNonNull(phase, "phase");
    this.currentPlayerId = Text.requireNonBlank(currentPlayerId, "currentPlayerId");
    this.playerOrder = List.copyOf(playerOrder);
    this.players = List.copyOf(players);
    this.locations = List.copyOf(locations);
    this.routes = List.copyOf(routes);
    this.faceUpCards = List.copyOf(faceUpCards);
    this.transportDrawPileSize = transportDrawPileSize;
    this.transportDiscardPileSize = transportDiscardPileSize;
    this.destinationTicketDeckSize = destinationTicketDeckSize;
    this.finalRoundActive = finalRoundActive;
    this.triggeringPlayerId = Text.normalizeOptional(triggeringPlayerId);
    this.finalTurnsRemaining = finalTurnsRemaining;
    this.drawProgress = Objects.requireNonNull(drawProgress, "drawProgress");
    this.canUndo = canUndo;
    this.rushHourPhase = Objects.requireNonNull(rushHourPhase, "rushHourPhase");
    this.rushHourTurnsRemaining = rushHourTurnsRemaining;
    this.forecastRushHourEvent = forecastRushHourEvent;
    this.activeRushHourEvent = activeRushHourEvent;
    this.rushHourAffectedRouteIds = List.copyOf(rushHourAffectedRouteIds);
    this.rushHourPointsByPlayerId = Map.copyOf(rushHourPointsByPlayerId);
  }

  /** Creates a snapshot from a domain game aggregate. */
  public static GameSnapshot from(Game game) {
    Objects.requireNonNull(game, "game");
    return new GameSnapshot(
        game.phase(),
        game.turnManager().currentPlayerId(),
        game.turnManager().playerOrder(),
        game.players().stream().map(PlayerSnapshot::from).toList(),
        game.board().locations().stream().map(LocationSnapshot::from).toList(),
        game.board().routes().stream().map(RouteSnapshot::from).toList(),
        game.faceUpDisplay().visibleCards(),
        game.transportCardDeck().drawPileSize(),
        game.transportCardDeck().discardPileSize(),
        game.destinationTicketDeck().size(),
        game.turnManager().isFinalRoundActive(),
        game.turnManager().triggeringPlayerId(),
        game.turnManager().finalTurnsRemaining(),
        TransportDrawProgress.inactive(),
        false,
        game.rushHourManager().phase(),
        game.rushHourManager().turnsRemaining(),
        game.rushHourManager().forecastEvent().map(RushHourEventSnapshot::from).orElse(null),
        game.rushHourManager().activeEvent().map(RushHourEventSnapshot::from).orElse(null),
        game.rushHourManager().affectedRouteIds(game.board()),
        game.rushHourManager().bonusPointsByPlayerId());
  }

  /**
   * Creates a snapshot from a domain game aggregate and application-layer draw progress.
   *
   * @param game source game aggregate
   * @param drawProgress in-flight transportation-card draw progress
   * @return immutable game snapshot
   */
  public static GameSnapshot from(Game game, TransportDrawProgress drawProgress) {
    return from(game, drawProgress, false);
  }

  /**
   * Creates a snapshot from a domain game aggregate, draw progress, and undo availability.
   *
   * @param game source game aggregate
   * @param drawProgress in-flight transportation-card draw progress
   * @param canUndo whether undo history is available
   * @return immutable game snapshot
   */
  public static GameSnapshot from(Game game, TransportDrawProgress drawProgress, boolean canUndo) {
    Objects.requireNonNull(game, "game");
    Objects.requireNonNull(drawProgress, "drawProgress");
    return new GameSnapshot(
        game.phase(),
        game.turnManager().currentPlayerId(),
        game.turnManager().playerOrder(),
        game.players().stream().map(PlayerSnapshot::from).toList(),
        game.board().locations().stream().map(LocationSnapshot::from).toList(),
        game.board().routes().stream().map(RouteSnapshot::from).toList(),
        game.faceUpDisplay().visibleCards(),
        game.transportCardDeck().drawPileSize(),
        game.transportCardDeck().discardPileSize(),
        game.destinationTicketDeck().size(),
        game.turnManager().isFinalRoundActive(),
        game.turnManager().triggeringPlayerId(),
        game.turnManager().finalTurnsRemaining(),
        drawProgress,
        canUndo,
        game.rushHourManager().phase(),
        game.rushHourManager().turnsRemaining(),
        game.rushHourManager().forecastEvent().map(RushHourEventSnapshot::from).orElse(null),
        game.rushHourManager().activeEvent().map(RushHourEventSnapshot::from).orElse(null),
        game.rushHourManager().affectedRouteIds(game.board()),
        game.rushHourManager().bonusPointsByPlayerId());
  }

  /** Returns the game phase. */
  public GamePhase phase() {
    return phase;
  }

  /** Returns whether player turn actions may currently be submitted. */
  public boolean acceptsPlayerActions() {
    return phase == GamePhase.RUNNING || phase == GamePhase.FINAL_ROUND;
  }

  /** Returns the active player identifier. */
  public String currentPlayerId() {
    return currentPlayerId;
  }

  /** Returns player identifiers in clockwise order. */
  public List<String> playerOrder() {
    return Collections.unmodifiableList(playerOrder);
  }

  /** Returns player snapshots. */
  public List<PlayerSnapshot> players() {
    return Collections.unmodifiableList(players);
  }

  /** Returns board location snapshots. */
  public List<LocationSnapshot> locations() {
    return Collections.unmodifiableList(locations);
  }

  /** Returns board route snapshots. */
  public List<RouteSnapshot> routes() {
    return Collections.unmodifiableList(routes);
  }

  /** Returns currently visible transportation cards. */
  public List<CardColor> faceUpCards() {
    return Collections.unmodifiableList(faceUpCards);
  }

  /** Returns the number of cards in the transportation draw pile. */
  public int transportDrawPileSize() {
    return transportDrawPileSize;
  }

  /** Returns the number of cards in the transportation discard pile. */
  public int transportDiscardPileSize() {
    return transportDiscardPileSize;
  }

  /** Returns the number of destination tickets left in the deck. */
  public int destinationTicketDeckSize() {
    return destinationTicketDeckSize;
  }

  /** Returns whether final round is active. */
  public boolean finalRoundActive() {
    return finalRoundActive;
  }

  /** Returns the player that triggered final round when present. */
  public Optional<String> triggeringPlayerId() {
    return Optional.ofNullable(triggeringPlayerId);
  }

  /** Returns the number of final turns remaining. */
  public int finalTurnsRemaining() {
    return finalTurnsRemaining;
  }

  /** Returns the in-flight transportation-card draw progress. */
  public TransportDrawProgress drawProgress() {
    return drawProgress;
  }

  /** Returns whether an undo operation is currently available. */
  public boolean canUndo() {
    return canUndo;
  }

  /** Returns the Rush Hour phase. */
  public RushHourPhase rushHourPhase() {
    return rushHourPhase;
  }

  /** Returns turns remaining in the current Rush Hour phase. */
  public int rushHourTurnsRemaining() {
    return rushHourTurnsRemaining;
  }

  /** Returns the forecast Rush Hour event when present. */
  public Optional<RushHourEventSnapshot> forecastRushHourEvent() {
    return Optional.ofNullable(forecastRushHourEvent);
  }

  /** Returns the active Rush Hour event when present. */
  public Optional<RushHourEventSnapshot> activeRushHourEvent() {
    return Optional.ofNullable(activeRushHourEvent);
  }

  /** Returns currently visible Rush Hour affected route ids. */
  public List<String> rushHourAffectedRouteIds() {
    return Collections.unmodifiableList(rushHourAffectedRouteIds);
  }

  /** Returns immediate Rush Hour bonus points by player id. */
  public Map<String, Integer> rushHourPointsByPlayerId() {
    return Collections.unmodifiableMap(rushHourPointsByPlayerId);
  }

  /** Returns whether a transportation-card draw action is in progress. */
  public boolean transportDrawActionActive() {
    return drawProgress.isActive();
  }

  /** Returns the player currently resolving a transportation-card draw action when present. */
  public Optional<String> transportDrawActionPlayerId() {
    return drawProgress.playerId();
  }

  /** Returns how many transportation cards have been drawn in the active action. */
  public int transportDrawsTaken() {
    return drawProgress.drawsTaken();
  }

  /** Returns the locked face-up slot index, or -1 when no slot is locked. */
  public int lockedFaceUpIndex() {
    return drawProgress.lockedFaceUpIndex();
  }

}
