package ttrlondon.ui.viewmodel;

/**
 * Immutable offset expressed as a proportion of the board width and height.
 */
public final class NormalizedOffset {
  private final double dx;
  private final double dy;

  /**
   * Creates a normalized offset.
   *
   * @param dx horizontal offset as a proportion of board width
   * @param dy vertical offset as a proportion of board height
   */
  public NormalizedOffset(double dx, double dy) {
    this.dx = dx;
    this.dy = dy;
  }

  /** Returns the horizontal offset. */
  public double dx() {
    return dx;
  }

  /** Returns the vertical offset. */
  public double dy() {
    return dy;
  }
}
