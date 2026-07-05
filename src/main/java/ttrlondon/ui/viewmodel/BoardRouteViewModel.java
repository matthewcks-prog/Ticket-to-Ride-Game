package ttrlondon.ui.viewmodel;

import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.player.PlayerColor;

/**
 * Immutable rendering read model for one printed board route.
 */
public final class BoardRouteViewModel {
  private final String id;
  private final BoardLocationViewModel locationA;
  private final BoardLocationViewModel locationB;
  private final int length;
  private final RouteColor color;
  private final RouteKind kind;
  private final int requiredBusSymbols;
  private final int laneOffset;
  private final double curveOffset;
  private final String claimedBy;
  private final PlayerColor claimingPlayerColor;
  private final boolean rushHourAffected;

  /**
   * Creates a route rendering view model.
   *
   * @param id route identifier
   * @param locationA first endpoint
   * @param locationB second endpoint
   * @param length number of route slots
   * @param color printed route colour
   * @param kind route payment category
   * @param requiredBusSymbols required Bus symbols for ferry routes
   * @param laneOffset perpendicular lane offset for parallel routes
   * @param curveOffset normalized curve offset for non-straight routes
   * @param claimedBy optional claiming player identifier
   * @param claimingPlayerColor optional claiming player colour
   * @param rushHourAffected whether Rush Hour currently affects this route
   */
  public BoardRouteViewModel(
      String id,
      BoardLocationViewModel locationA,
      BoardLocationViewModel locationB,
      int length,
      RouteColor color,
      RouteKind kind,
      int requiredBusSymbols,
      int laneOffset,
      double curveOffset,
      String claimedBy,
      PlayerColor claimingPlayerColor,
      boolean rushHourAffected) {
    this.id = Text.requireNonBlank(id, "id");
    this.locationA = Objects.requireNonNull(locationA, "locationA");
    this.locationB = Objects.requireNonNull(locationB, "locationB");
    this.length = length;
    this.color = Objects.requireNonNull(color, "color");
    this.kind = Objects.requireNonNull(kind, "kind");
    if (requiredBusSymbols < 0) {
      throw new IllegalArgumentException("requiredBusSymbols must not be negative");
    }
    this.requiredBusSymbols = requiredBusSymbols;
    this.laneOffset = laneOffset;
    this.curveOffset = curveOffset;
    this.claimedBy = Text.normalizeOptional(claimedBy);
    this.claimingPlayerColor = claimingPlayerColor;
    this.rushHourAffected = rushHourAffected;
    if (this.claimedBy != null && this.claimingPlayerColor == null) {
      throw new IllegalArgumentException("claimed routes must include claiming player colour");
    }
  }

  /** Returns the route identifier. */
  public String id() {
    return id;
  }

  /** Returns the first endpoint view model. */
  public BoardLocationViewModel locationA() {
    return locationA;
  }

  /** Returns the second endpoint view model. */
  public BoardLocationViewModel locationB() {
    return locationB;
  }

  /** Returns the number of slots in the route. */
  public int length() {
    return length;
  }

  /** Returns the printed route colour. */
  public RouteColor color() {
    return color;
  }

  /** Returns the route payment category. */
  public RouteKind kind() {
    return kind;
  }

  /** Returns whether this route is a ferry route. */
  public boolean isFerry() {
    return kind == RouteKind.FERRY;
  }

  /** Returns required Bus symbols for ferry routes. */
  public int requiredBusSymbols() {
    return requiredBusSymbols;
  }

  /** Returns the perpendicular lane offset used for parallel routes. */
  public int laneOffset() {
    return laneOffset;
  }

  /** Returns the normalized curve offset used for curved routes. */
  public double curveOffset() {
    return curveOffset;
  }

  /** Returns the claiming player identifier when the route has been claimed. */
  public Optional<String> claimedBy() {
    return Optional.ofNullable(claimedBy);
  }

  /** Returns the claiming player's colour when the route has been claimed. */
  public Optional<PlayerColor> claimingPlayerColor() {
    return Optional.ofNullable(claimingPlayerColor);
  }

  /** Returns whether Rush Hour currently affects this route. */
  public boolean rushHourAffected() {
    return rushHourAffected;
  }
}
