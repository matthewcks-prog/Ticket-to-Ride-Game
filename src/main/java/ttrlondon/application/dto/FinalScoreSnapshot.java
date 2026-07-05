package ttrlondon.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.scoring.FinalScore;

/**
 * Immutable read model for final scoring presentation.
 */
public final class FinalScoreSnapshot {
  private final List<PlayerFinalScoreSnapshot> playerScores;
  private final List<String> winnerIds;

  /**
   * Creates a final score snapshot.
   *
   * @param playerScores per-player scoring breakdowns
   * @param winnerIds winner identifiers
   */
  public FinalScoreSnapshot(
      List<PlayerFinalScoreSnapshot> playerScores, List<String> winnerIds) {
    this.playerScores = List.copyOf(playerScores);
    this.winnerIds = List.copyOf(winnerIds);
  }

  /** Creates a snapshot from domain final scoring results. */
  public static FinalScoreSnapshot from(FinalScore finalScore, Game game) {
    Objects.requireNonNull(finalScore, "finalScore");
    Objects.requireNonNull(game, "game");
    List<PlayerFinalScoreSnapshot> playerScores =
        game.players().stream()
            .map(player -> PlayerFinalScoreSnapshot.from(player, finalScore, game.board()))
            .toList();
    return new FinalScoreSnapshot(playerScores, finalScore.winnerIds());
  }

  /** Returns per-player scoring breakdowns. */
  public List<PlayerFinalScoreSnapshot> playerScores() {
    return Collections.unmodifiableList(playerScores);
  }

  /** Returns winner player identifiers. */
  public List<String> winnerIds() {
    return Collections.unmodifiableList(winnerIds);
  }

  /**
   * Immutable final scoring read model for one player.
   */
  public static final class PlayerFinalScoreSnapshot {
    private final String playerId;
    private final String playerName;
    private final PlayerColor playerColor;
    private final int routePoints;
    private final int ticketPoints;
    private final int longestPathLength;
    private final int longestPathBonus;
    private final int totalScore;
    private final int completedTicketCount;
    private final List<TicketScoreSnapshot> ticketScores;

    /**
     * Creates a per-player final score snapshot.
     *
     * @param playerId player identifier
     * @param playerName player display name
     * @param playerColor player colour
     * @param routePoints route points
     * @param ticketPoints net ticket points
     * @param longestPathLength longest path length
     * @param longestPathBonus longest path bonus
     * @param totalScore total final score
     * @param completedTicketCount completed ticket count
     * @param ticketScores per-ticket score results
     */
    public PlayerFinalScoreSnapshot(
        String playerId,
        String playerName,
        PlayerColor playerColor,
        int routePoints,
        int ticketPoints,
        int longestPathLength,
        int longestPathBonus,
        int totalScore,
        int completedTicketCount,
        List<TicketScoreSnapshot> ticketScores) {
      this.playerId = Text.requireNonBlank(playerId, "playerId");
      this.playerName = Text.requireNonBlank(playerName, "playerName");
      this.playerColor = Objects.requireNonNull(playerColor, "playerColor");
      this.routePoints = routePoints;
      this.ticketPoints = ticketPoints;
      this.longestPathLength = longestPathLength;
      this.longestPathBonus = longestPathBonus;
      this.totalScore = totalScore;
      this.completedTicketCount = completedTicketCount;
      this.ticketScores = List.copyOf(ticketScores);
    }

    private static PlayerFinalScoreSnapshot from(
        Player player, FinalScore finalScore, Board board) {
      FinalScore.PlayerFinalScore score = finalScore.scoresByPlayerId().get(player.id());
      if (score == null) {
        throw new IllegalStateException("missing final score for player " + player.id());
      }
      return new PlayerFinalScoreSnapshot(
          player.id(),
          player.name(),
          player.color(),
          score.routePoints(),
          score.ticketPoints(),
          score.longestPathLength(),
          score.longestPathBonus(),
          score.totalScore(),
          score.completedTicketCount(),
          score.ticketResults().stream()
              .map(ticketResult -> TicketScoreSnapshot.from(ticketResult, board))
              .toList());
    }

    /** Returns player identifier. */
    public String playerId() {
      return playerId;
    }

    /** Returns player name. */
    public String playerName() {
      return playerName;
    }

    /** Returns player colour. */
    public PlayerColor playerColor() {
      return playerColor;
    }

    /** Returns route points. */
    public int routePoints() {
      return routePoints;
    }

    /** Returns net destination ticket points. */
    public int ticketPoints() {
      return ticketPoints;
    }

    /** Returns longest continuous path length. */
    public int longestPathLength() {
      return longestPathLength;
    }

    /** Returns longest path bonus. */
    public int longestPathBonus() {
      return longestPathBonus;
    }

    /** Returns total final score. */
    public int totalScore() {
      return totalScore;
    }

    /** Returns completed ticket count. */
    public int completedTicketCount() {
      return completedTicketCount;
    }

    /** Returns per-ticket score results. */
    public List<TicketScoreSnapshot> ticketScores() {
      return Collections.unmodifiableList(ticketScores);
    }
  }

  /**
   * Immutable final scoring read model for one destination ticket.
   */
  public static final class TicketScoreSnapshot {
    private final String ticketId;
    private final String locationADisplayName;
    private final String locationBDisplayName;
    private final int points;
    private final boolean completed;
    private final int scoreContribution;

    /**
     * Creates a ticket final score snapshot.
     *
     * @param ticketId ticket identifier
     * @param locationADisplayName first endpoint display name
     * @param locationBDisplayName second endpoint display name
     * @param points printed points
     * @param completed whether the ticket was completed
     */
    public TicketScoreSnapshot(
        String ticketId,
        String locationADisplayName,
        String locationBDisplayName,
        int points,
        boolean completed) {
      this.ticketId = Text.requireNonBlank(ticketId, "ticketId");
      this.locationADisplayName = Text.requireNonBlank(locationADisplayName, "locationADisplayName");
      this.locationBDisplayName = Text.requireNonBlank(locationBDisplayName, "locationBDisplayName");
      this.points = points;
      this.completed = completed;
      this.scoreContribution = completed ? points : -points;
    }

    private static TicketScoreSnapshot from(
        FinalScore.TicketResult ticketResult, Board board) {
      return new TicketScoreSnapshot(
          ticketResult.ticketId(),
          displayName(board, ticketResult.locationAId()),
          displayName(board, ticketResult.locationBId()),
          ticketResult.points(),
          ticketResult.completed());
    }

    /** Returns ticket identifier. */
    public String ticketId() {
      return ticketId;
    }

    /** Returns first endpoint display name. */
    public String locationADisplayName() {
      return locationADisplayName;
    }

    /** Returns second endpoint display name. */
    public String locationBDisplayName() {
      return locationBDisplayName;
    }

    /** Returns printed points. */
    public int points() {
      return points;
    }

    /** Returns whether the ticket was completed. */
    public boolean completed() {
      return completed;
    }

    /** Returns signed ticket score contribution. */
    public int scoreContribution() {
      return scoreContribution;
    }
  }

  private static String displayName(Board board, String locationId) {
    return board.findLocation(locationId)
        .orElseThrow(() -> new IllegalStateException("unknown location " + locationId))
        .displayName();
  }
}
