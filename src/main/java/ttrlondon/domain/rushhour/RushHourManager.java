package ttrlondon.domain.rushhour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.random.ShuffleStrategy;

/**
 * Owns Rush Hour event sequencing, event recycling, and bonus accounting.
 */
public final class RushHourManager {
  private final ShuffleStrategy shuffleStrategy;
  private final Map<String, RushHourEvent> eventsById;
  private final Map<String, Integer> bonusPointsByPlayerId;
  private List<String> eventDeck;
  private List<String> eventDiscard;
  private RushHourPhase phase;
  private String forecastEventId;
  private String activeEventId;
  private int turnsRemaining;

  /**
   * Creates a Rush Hour manager.
   *
   * @param events event definitions
   * @param shuffledEventIds initial event deck order
   * @param shuffleStrategy recycling shuffle strategy
   */
  public RushHourManager(
      List<RushHourEvent> events, List<String> shuffledEventIds, ShuffleStrategy shuffleStrategy) {
    Objects.requireNonNull(events, "events");
    this.shuffleStrategy = Objects.requireNonNull(shuffleStrategy, "shuffleStrategy");
    this.eventsById = indexEvents(events);
    this.eventDeck = new ArrayList<>(shuffledEventIds);
    if (!eventsById.keySet().containsAll(eventDeck)) {
      throw new IllegalArgumentException("event deck contains unknown event id");
    }
    this.eventDiscard = new ArrayList<>();
    this.bonusPointsByPlayerId = new LinkedHashMap<>();
    this.phase = RushHourPhase.INACTIVE;
    this.turnsRemaining = 0;
  }

  /** Starts the first forecast event for the supplied player count. */
  public void start(int playerCount) {
    if (phase != RushHourPhase.INACTIVE) {
      return;
    }
    forecastEventId = drawEventId().orElse(null);
    if (forecastEventId != null) {
      phase = RushHourPhase.FORECAST;
      turnsRemaining = playerCount;
    }
  }

  /** Advances the Rush Hour clock after one complete player turn. */
  public void advanceAfterCompletedTurn(int playerCount) {
    if (phase == RushHourPhase.INACTIVE) {
      start(playerCount);
      return;
    }
    if (turnsRemaining > 0) {
      turnsRemaining--;
    }
    if (turnsRemaining > 0) {
      return;
    }
    if (phase == RushHourPhase.FORECAST) {
      activeEventId = forecastEventId;
      forecastEventId = null;
      phase = RushHourPhase.PEAK;
      turnsRemaining = playerCount;
      return;
    }
    if (phase == RushHourPhase.PEAK) {
      if (activeEventId != null) {
        eventDiscard.add(activeEventId);
      }
      activeEventId = null;
      forecastEventId = drawEventId().orElse(null);
      if (forecastEventId == null) {
        phase = RushHourPhase.INACTIVE;
        turnsRemaining = 0;
      } else {
        phase = RushHourPhase.FORECAST;
        turnsRemaining = playerCount;
      }
    }
  }

  /** Returns the current Rush Hour phase. */
  public RushHourPhase phase() {
    return phase;
  }

  /** Returns turns remaining in the current Rush Hour phase. */
  public int turnsRemaining() {
    return turnsRemaining;
  }

  /** Returns the forecast event when present. */
  public Optional<RushHourEvent> forecastEvent() {
    return Optional.ofNullable(forecastEventId).map(eventsById::get);
  }

  /** Returns the forecast event id when present. */
  public Optional<String> forecastEventId() {
    return Optional.ofNullable(forecastEventId);
  }

  /** Returns the active event when present. */
  public Optional<RushHourEvent> activeEvent() {
    return Optional.ofNullable(activeEventId).map(eventsById::get);
  }

  /** Returns the active event id when present. */
  public Optional<String> activeEventId() {
    return Optional.ofNullable(activeEventId);
  }

  /** Returns whether the route is affected during the active peak phase. */
  public boolean affectsDuringPeak(Route route) {
    return phase == RushHourPhase.PEAK && activeEvent().filter(event -> event.affects(route)).isPresent();
  }

  /** Returns affected route ids for the current visible event. */
  public List<String> affectedRouteIds(Board board) {
    Objects.requireNonNull(board, "board");
    Optional<RushHourEvent> event =
        phase == RushHourPhase.PEAK ? activeEvent() : forecastEvent();
    if (event.isEmpty()) {
      return List.of();
    }
    return board.routes().stream().filter(event.get()::affects).map(Route::id).toList();
  }

  /** Records Rush Hour bonus points for a player. */
  public void awardBonus(String playerId, int points) {
    Text.requireNonBlank(playerId, "playerId");
    if (points < 0) {
      throw new IllegalArgumentException("points must not be negative");
    }
    if (points > 0) {
      bonusPointsByPlayerId.merge(playerId, points, Integer::sum);
    }
  }

  /** Returns Rush Hour bonus points by player id. */
  public Map<String, Integer> bonusPointsByPlayerId() {
    return Collections.unmodifiableMap(bonusPointsByPlayerId);
  }

  /** Returns a snapshot of remaining event deck ids. */
  public List<String> eventDeckSnapshot() {
    return Collections.unmodifiableList(eventDeck);
  }

  /** Returns a snapshot of discarded event ids. */
  public List<String> eventDiscardSnapshot() {
    return Collections.unmodifiableList(eventDiscard);
  }

  /** Restores Rush Hour mutable state from a trusted memento. */
  public void restoreState(
      RushHourPhase restoredPhase,
      String restoredForecastEventId,
      String restoredActiveEventId,
      int restoredTurnsRemaining,
      List<String> restoredEventDeck,
      List<String> restoredEventDiscard,
      Map<String, Integer> restoredBonusPointsByPlayerId) {
    phase = Objects.requireNonNull(restoredPhase, "restoredPhase");
    forecastEventId = normalizeKnownEvent(restoredForecastEventId);
    activeEventId = normalizeKnownEvent(restoredActiveEventId);
    if (restoredTurnsRemaining < 0) {
      throw new IllegalArgumentException("restoredTurnsRemaining must not be negative");
    }
    turnsRemaining = restoredTurnsRemaining;
    eventDeck = new ArrayList<>(List.copyOf(restoredEventDeck));
    eventDiscard = new ArrayList<>(List.copyOf(restoredEventDiscard));
    if (!eventsById.keySet().containsAll(eventDeck) || !eventsById.keySet().containsAll(eventDiscard)) {
      throw new IllegalArgumentException("restored Rush Hour state references unknown events");
    }
    bonusPointsByPlayerId.clear();
    bonusPointsByPlayerId.putAll(Map.copyOf(restoredBonusPointsByPlayerId));
  }

  private Optional<String> drawEventId() {
    if (eventDeck.isEmpty() && !eventDiscard.isEmpty()) {
      eventDeck = new ArrayList<>(shuffleStrategy.shuffle(eventDiscard));
      eventDiscard = new ArrayList<>();
    }
    if (eventDeck.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(eventDeck.remove(0));
  }

  private String normalizeKnownEvent(String eventId) {
    String normalized = Text.normalizeOptional(eventId);
    if (normalized != null && !eventsById.containsKey(normalized)) {
      throw new IllegalArgumentException("unknown Rush Hour event id: " + normalized);
    }
    return normalized;
  }

  private static Map<String, RushHourEvent> indexEvents(List<RushHourEvent> events) {
    Map<String, RushHourEvent> indexed = new LinkedHashMap<>();
    for (RushHourEvent event : events) {
      if (indexed.put(event.id(), event) != null) {
        throw new IllegalArgumentException("duplicate Rush Hour event id: " + event.id());
      }
    }
    return Collections.unmodifiableMap(indexed);
  }
}
