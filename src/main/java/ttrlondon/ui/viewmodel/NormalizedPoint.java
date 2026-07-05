package ttrlondon.ui.viewmodel;

/**
 * Immutable point expressed as normalized board coordinates between 0.0 and 1.0.
 */
public final class NormalizedPoint {
  private final double x;
  private final double y;

  /**
   * Creates a normalized point.
   *
   * @param x horizontal coordinate from 0.0 to 1.0
   * @param y vertical coordinate from 0.0 to 1.0
   */
  public NormalizedPoint(double x, double y) {
    if (x < 0.0 || x > 1.0 || y < 0.0 || y > 1.0) {
      throw new IllegalArgumentException("normalized coordinates must be between 0.0 and 1.0");
    }
    this.x = x;
    this.y = y;
  }

  /** Returns the normalized horizontal coordinate. */
  public double x() {
    return x;
  }

  /** Returns the normalized vertical coordinate. */
  public double y() {
    return y;
  }
}
