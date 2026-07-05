package ttrlondon.domain.scoring;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ttrlondon.domain.common.Text;

/**
 * Immutable final scoring result for all players.
 */
public final class FinalScore {
  private final Map<String, PlayerFinalScore> scoresByPlayerId;
  private final List<String> winnerIds;

  /**
   * Creates a final scoring result.
   *
   * @param totalsByPlayerId total score per player identifier
   * @param winnerIds winner player identifiers
   */
  public FinalScore(Map<String, Integer> totalsByPlayerId, List<String> winnerIds) {
    Objects.requireNonNull(totalsByPlayerId, "totalsByPlayerId");
    Objects.requireNonNull(winnerIds, "winnerIds");
    Map<String, PlayerFinalScore> scores = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : totalsByPlayerId.entrySet()) {
      scores.put(
          entry.getKey(), PlayerFinalScore.withTotalOnly(entry.getKey(), entry.getValue()));
    }
    this.scoresByPlayerId = Collections.unmodifiableMap(scores);
    this.winnerIds = List.copyOf(winnerIds);
  }

  private FinalScore(List<String> winnerIds, Map<String, PlayerFinalScore> scoresByPlayerId) {
    Objects.requireNonNull(scoresByPlayerId, "scoresByPlayerId");
    Objects.requireNonNull(winnerIds, "winnerIds");
    this.scoresByPlayerId =
        Collections.unmodifiableMap(new LinkedHashMap<>(scoresByPlayerId));
    this.winnerIds = List.copyOf(winnerIds);
  }

  /**
   * Creates a final scoring result from complete per-player breakdowns.
   *
   * @param scoresByPlayerId scoring breakdowns in player order
   * @param winnerIds winner player identifiers
   * @return immutable final scoring result
   */
  public static FinalScore fromPlayerScores(
      Map<String, PlayerFinalScore> scoresByPlayerId, List<String> winnerIds) {
    return new FinalScore(winnerIds, scoresByPlayerId);
  }

  /** Returns final totals by player identifier. */
  public Map<String, Integer> totalsByPlayerId() {
    Map<String, Integer> totals = new LinkedHashMap<>();
    for (PlayerFinalScore score : scoresByPlayerId.values()) {
      totals.put(score.playerId(), score.totalScore());
    }
    return Collections.unmodifiableMap(totals);
  }

  /** Returns complete score breakdowns by player identifier. */
  public Map<String, PlayerFinalScore> scoresByPlayerId() {
    return scoresByPlayerId;
  }

  /** Returns winner player identifiers. */
  public List<String> winnerIds() {
    return Collections.unmodifiableList(winnerIds);
  }

  /**
   * Immutable scoring breakdown for one player.
   */
  public static final class PlayerFinalScore {
    private final String playerId;
    private final int routePoints;
    private final int ticketPoints;
    private final int longestPathLength;
    private final int longestPathBonus;
    private final List<TicketResult> ticketResults;
    private final int totalScore;

    /**
     * Creates a scoring breakdown for one player.
     *
     * @param playerId player identifier
     * @param routePoints points from claimed routes
     * @param ticketPoints net destination ticket points
     * @param longestPathLength longest continuous path length
     * @param longestPathBonus awarded longest path bonus
     * @param ticketResults per-ticket completion results
     */
    public PlayerFinalScore(
        String playerId,
        int routePoints,
        int ticketPoints,
        int longestPathLength,
        int longestPathBonus,
        List<TicketResult> ticketResults) {
      this.playerId = Text.requireNonBlank(playerId, "playerId");
      this.routePoints = routePoints;
      this.ticketPoints = ticketPoints;
      this.longestPathLength = longestPathLength;
      this.longestPathBonus = longestPathBonus;
      this.ticketResults = List.copyOf(ticketResults);
      this.totalScore = routePoints + ticketPoints + longestPathBonus;
    }

    private static PlayerFinalScore withTotalOnly(String playerId, int totalScore) {
      return new PlayerFinalScore(playerId, totalScore, 0, 0, 0, List.of());
    }

    /** Returns the player identifier. */
    public String playerId() {
      return playerId;
    }

    /** Returns points from claimed routes. */
    public int routePoints() {
      return routePoints;
    }

    /** Returns net destination ticket points. */
    public int ticketPoints() {
      return ticketPoints;
    }

    /** Returns the longest continuous path length. */
    public int longestPathLength() {
      return longestPathLength;
    }

    /** Returns the longest path bonus awarded to this player. */
    public int longestPathBonus() {
      return longestPathBonus;
    }

    /** Returns per-ticket completion results. */
    public List<TicketResult> ticketResults() {
      return Collections.unmodifiableList(ticketResults);
    }

    /** Returns the number of completed destination tickets. */
    public int completedTicketCount() {
      int count = 0;
      for (TicketResult ticketResult : ticketResults) {
        if (ticketResult.completed()) {
          count++;
        }
      }
      return count;
    }

    /** Returns the total final score. */
    public int totalScore() {
      return totalScore;
    }
  }

  /**
   * Immutable completion result for a single destination ticket.
   */
  public static final class TicketResult {
    private final String ticketId;
    private final String locationAId;
    private final String locationBId;
    private final int points;
    private final boolean completed;

    /**
     * Creates a ticket scoring result.
     *
     * @param ticketId ticket identifier
     * @param locationAId first endpoint identifier
     * @param locationBId second endpoint identifier
     * @param points printed ticket value
     * @param completed whether the ticket was completed
     */
    public TicketResult(
        String ticketId,
        String locationAId,
        String locationBId,
        int points,
        boolean completed) {
      this.ticketId = Text.requireNonBlank(ticketId, "ticketId");
      this.locationAId = Text.requireNonBlank(locationAId, "locationAId");
      this.locationBId = Text.requireNonBlank(locationBId, "locationBId");
      this.points = points;
      this.completed = completed;
    }

    /** Returns the ticket identifier. */
    public String ticketId() {
      return ticketId;
    }

    /** Returns the first endpoint identifier. */
    public String locationAId() {
      return locationAId;
    }

    /** Returns the second endpoint identifier. */
    public String locationBId() {
      return locationBId;
    }

    /** Returns the printed ticket value. */
    public int points() {
      return points;
    }

    /** Returns whether the ticket was completed. */
    public boolean completed() {
      return completed;
    }

    /** Returns signed points contributed by this ticket. */
    public int scoreContribution() {
      return completed ? points : -points;
    }
  }
}
