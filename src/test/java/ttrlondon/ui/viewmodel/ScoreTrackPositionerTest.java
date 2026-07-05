package ttrlondon.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class ScoreTrackPositionerTest {
  private static final double EPSILON = 0.0001;
  private static final double LEFT = 0.035;
  private static final double RIGHT = 0.965;
  private static final double TOP = 0.035;
  private static final double BOTTOM = 0.965;

  @Test
  void mapsScoreAnchorsToBoardCorners() {
    assertPoint(LEFT, BOTTOM, ScoreTrackPositioner.positionForScore(0));
    assertPoint(LEFT, TOP, ScoreTrackPositioner.positionForScore(10));
    assertPoint(RIGHT, TOP, ScoreTrackPositioner.positionForScore(25));
    assertPoint(RIGHT, BOTTOM, ScoreTrackPositioner.positionForScore(35));
    assertPoint(LEFT, BOTTOM, ScoreTrackPositioner.positionForScore(50));
  }

  @Test
  void spacesIntermediateScoresEvenlyAcrossTrackEdges() {
    assertPoint(LEFT, 0.5, ScoreTrackPositioner.positionForScore(5));
    assertPoint(0.655, TOP, ScoreTrackPositioner.positionForScore(20));
    assertPoint(RIGHT, 0.5, ScoreTrackPositioner.positionForScore(30));
    assertPoint(0.655, BOTTOM, ScoreTrackPositioner.positionForScore(40));
  }

  @Test
  void wrapsScoresBeyondOneLapBackOntoTrack() {
    assertPoint(LEFT, 0.5, ScoreTrackPositioner.positionForScore(55));
    assertPoint(LEFT, BOTTOM, ScoreTrackPositioner.positionForScore(100));
  }

  private static void assertPoint(double expectedX, double expectedY, NormalizedPoint actual) {
    assertEquals(expectedX, actual.x(), EPSILON);
    assertEquals(expectedY, actual.y(), EPSILON);
  }
}
