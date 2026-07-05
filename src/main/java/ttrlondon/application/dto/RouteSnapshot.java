package ttrlondon.application.dto;

import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;
import ttrlondon.domain.common.Text;

/**
 * Immutable read model of a printed board route and its claim state.
 */
public final class RouteSnapshot {
  private final String id;
  private final String locationAId;
  private final String locationBId;
  private final int length;
  private final RouteColor color;
  private final RouteKind kind;
  private final int requiredBusSymbols;
  private final String doubleGroupId;
  private final String claimedBy;

  /**
   * Creates a route snapshot.
   *
   * @param id route identifier
   * @param locationAId first endpoint identifier
   * @param locationBId second endpoint identifier
   * @param length route length
   * @param color printed route colour
   * @param kind route payment category
   * @param requiredBusSymbols required Bus symbols for ferry routes
   * @param doubleGroupId optional double-route group identifier
   * @param claimedBy optional claiming player identifier
   */
  public RouteSnapshot(
      String id,
      String locationAId,
      String locationBId,
      int length,
      RouteColor color,
      RouteKind kind,
      int requiredBusSymbols,
      String doubleGroupId,
      String claimedBy) {
    this.id = Text.requireNonBlank(id, "id");
    this.locationAId = Text.requireNonBlank(locationAId, "locationAId");
    this.locationBId = Text.requireNonBlank(locationBId, "locationBId");
    this.length = length;
    this.color = Objects.requireNonNull(color, "color");
    this.kind = Objects.requireNonNull(kind, "kind");
    if (requiredBusSymbols < 0) {
      throw new IllegalArgumentException("requiredBusSymbols must not be negative");
    }
    this.requiredBusSymbols = requiredBusSymbols;
    this.doubleGroupId = Text.normalizeOptional(doubleGroupId);
    this.claimedBy = Text.normalizeOptional(claimedBy);
  }

  /** Creates a snapshot from a domain route. */
  public static RouteSnapshot from(Route route) {
    Objects.requireNonNull(route, "route");
    return new RouteSnapshot(
        route.id(),
        route.locationA().id(),
        route.locationB().id(),
        route.length(),
        route.color(),
        route.kind(),
        route.requiredBusSymbols(),
        route.doubleGroupId().orElse(null),
        route.claimedBy().orElse(null));
  }

  /** Returns the route identifier. */
  public String id() {
    return id;
  }

  /** Returns the first endpoint identifier. */
  public String locationAId() {
    return locationAId;
  }

  /** Returns the second endpoint identifier. */
  public String locationBId() {
    return locationBId;
  }

  /** Returns the route length. */
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

  /** Returns the double-route group identifier when present. */
  public Optional<String> doubleGroupId() {
    return Optional.ofNullable(doubleGroupId);
  }

  /** Returns the claiming player identifier when present. */
  public Optional<String> claimedBy() {
    return Optional.ofNullable(claimedBy);
  }
}
