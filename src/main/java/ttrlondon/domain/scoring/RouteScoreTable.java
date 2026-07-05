package ttrlondon.domain.scoring;

/**
 * Single source of truth for route length scoring.
 */
public final class RouteScoreTable {
  /** Returns points for a London route length. */
  public int pointsForLength(int length) {
    if (length == 1) {
      return 1;
    }
    if (length == 2) {
      return 2;
    }
    if (length == 3) {
      return 4;
    }
    if (length == 4) {
      return 7;
    }
    throw new IllegalArgumentException("unsupported London route length: " + length);
  }
}
