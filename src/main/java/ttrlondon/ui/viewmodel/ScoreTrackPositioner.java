package ttrlondon.ui.viewmodel;

/**
 * Maps player scores to normalized positions around the London board-edge score track.
 */
public final class ScoreTrackPositioner {
  private static final int TRACK_MAX_SCORE = 50;
  private static final double EDGE_INSET = 0.035;
  private static final double LEFT = EDGE_INSET;
  private static final double RIGHT = 1.0 - EDGE_INSET;
  private static final double TOP = EDGE_INSET;
  private static final double BOTTOM = 1.0 - EDGE_INSET;

  private ScoreTrackPositioner() {}

  /**
   * Returns the normalized marker position for the supplied score.
   *
   * <p>The track runs from bottom-left upward to 10, across the top to 25, down the right edge to
   * 35, then back across the bottom to 50/0.
   *
   * @param score player score
   * @return normalized board position
   */
  public static NormalizedPoint positionForScore(int score) {
    int trackScore = Math.floorMod(score, TRACK_MAX_SCORE);
    if (score > 0 && trackScore == 0) {
      trackScore = TRACK_MAX_SCORE;
    }
    if (trackScore <= 10) {
      double fraction = trackScore / 10.0;
      return new NormalizedPoint(LEFT, interpolate(BOTTOM, TOP, fraction));
    }
    if (trackScore <= 25) {
      double fraction = (trackScore - 10) / 15.0;
      return new NormalizedPoint(interpolate(LEFT, RIGHT, fraction), TOP);
    }
    if (trackScore <= 35) {
      double fraction = (trackScore - 25) / 10.0;
      return new NormalizedPoint(RIGHT, interpolate(TOP, BOTTOM, fraction));
    }
    double fraction = (trackScore - 35) / 15.0;
    return new NormalizedPoint(interpolate(RIGHT, LEFT, fraction), BOTTOM);
  }

  private static double interpolate(double start, double end, double fraction) {
    return start + (end - start) * fraction;
  }
}
