package ttrlondon.domain.board;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.common.Text;

/**
 * A single printed route between two board locations, including claim state.
 */
public final class Route {
  private final String id;
  private final Location locationA;
  private final Location locationB;
  private final int length;
  private final RouteColor color;
  private final RouteKind kind;
  private final int requiredBusSymbols;
  private final String doubleGroupId;
  private final RouteRequirement requirement;
  private String claimedBy;

  /**
   * Creates a route.
   *
   * @param id stable route identifier
   * @param locationA first endpoint
   * @param locationB second endpoint
   * @param length route length in bus spaces
   * @param color printed route colour
   * @param doubleGroupId optional double-route group identifier
   * @param requirement payment requirement strategy
   */
  public Route(
      String id,
      Location locationA,
      Location locationB,
      int length,
      RouteColor color,
      String doubleGroupId,
      RouteRequirement requirement) {
    this(id, locationA, locationB, length, color, RouteKind.STANDARD, 0, doubleGroupId, requirement);
  }

  /**
   * Creates a route.
   *
   * @param id stable route identifier
   * @param locationA first endpoint
   * @param locationB second endpoint
   * @param length route length in bus spaces
   * @param color printed route colour
   * @param kind route payment category
   * @param requiredBusSymbols number of required Bus symbols
   * @param doubleGroupId optional double-route group identifier
   * @param requirement payment requirement strategy
   */
  public Route(
      String id,
      Location locationA,
      Location locationB,
      int length,
      RouteColor color,
      RouteKind kind,
      int requiredBusSymbols,
      String doubleGroupId,
      RouteRequirement requirement) {
    this.id = Text.requireNonBlank(id, "id");
    this.locationA = Objects.requireNonNull(locationA, "locationA");
    this.locationB = Objects.requireNonNull(locationB, "locationB");
    if (locationA.equals(locationB)) {
      throw new IllegalArgumentException("route endpoints must be different");
    }
    if (length <= 0) {
      throw new IllegalArgumentException("length must be positive");
    }
    this.length = length;
    this.color = Objects.requireNonNull(color, "color");
    this.kind = Objects.requireNonNull(kind, "kind");
    if (requiredBusSymbols < 0 || requiredBusSymbols > length) {
      throw new IllegalArgumentException("requiredBusSymbols must be between 0 and route length");
    }
    if (kind == RouteKind.STANDARD && requiredBusSymbols != 0) {
      throw new IllegalArgumentException("standard routes cannot require Bus symbols");
    }
    if (kind == RouteKind.FERRY && requiredBusSymbols == 0) {
      throw new IllegalArgumentException("ferry routes must require at least one Bus symbol");
    }
    this.requiredBusSymbols = requiredBusSymbols;
    this.doubleGroupId = Text.normalizeOptional(doubleGroupId);
    this.requirement = Objects.requireNonNull(requirement, "requirement");
  }

  /** Returns the stable route identifier. */
  public String id() {
    return id;
  }

  /** Returns the first endpoint. */
  public Location locationA() {
    return locationA;
  }

  /** Returns the second endpoint. */
  public Location locationB() {
    return locationB;
  }

  /** Returns the route length in bus spaces. */
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

  /** Returns the number of required Bus symbols for this route. */
  public int requiredBusSymbols() {
    return requiredBusSymbols;
  }

  /** Returns the double-route group identifier when this route belongs to one. */
  public Optional<String> doubleGroupId() {
    return Optional.ofNullable(doubleGroupId);
  }

  /** Returns the route payment requirement. */
  public RouteRequirement requirement() {
    return requirement;
  }

  /** Returns the claiming player identifier when the route has been claimed. */
  public Optional<String> claimedBy() {
    return Optional.ofNullable(claimedBy);
  }

  /** Returns whether this route is already claimed. */
  public boolean isClaimed() {
    return claimedBy != null;
  }

  /**
   * Returns whether this route can be claimed with the supplied context.
   *
   * @param playerId claiming player identifier
   * @param playerCount number of players in the game
   * @param routesInDoubleGroup routes sharing this route's double-route group
   * @param payment card payment offered
   * @param busesRemaining claiming player's remaining bus count
   * @return true when the claim is legal
   */
  public boolean canBeClaimed(
      String playerId,
      int playerCount,
      List<Route> routesInDoubleGroup,
      CardPayment payment,
      int busesRemaining) {
    Text.requireNonBlank(playerId, "playerId");
    Objects.requireNonNull(routesInDoubleGroup, "routesInDoubleGroup");
    Objects.requireNonNull(payment, "payment");
    if (isClaimed() || busesRemaining < length || !requirement.isSatisfiedBy(payment)) {
      return false;
    }
    if (doubleGroupId == null) {
      return true;
    }
    for (Route route : routesInDoubleGroup) {
      if (!route.doubleGroupId().filter(doubleGroupId::equals).isPresent()) {
        continue;
      }
      if (route.claimedBy().filter(playerId::equals).isPresent()) {
        return false;
      }
      if (playerCount == 2 && route.isClaimed()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Claims this route for the supplied player.
   *
   * @param playerId claiming player identifier
   */
  public void claim(String playerId) {
    if (isClaimed()) {
      throw new IllegalStateException("route is already claimed");
    }
    claimedBy = Text.requireNonBlank(playerId, "playerId");
  }

  /**
   * Restores this route's mutable claim state from a trusted game memento.
   *
   * @param restoredClaimedBy restored claiming player identifier, or null for unclaimed
   */
  public void restoreClaim(String restoredClaimedBy) {
    claimedBy = Text.normalizeOptional(restoredClaimedBy);
  }
}
