package ttrlondon.ui.viewmodel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.LocationSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.dto.RouteSnapshot;
import ttrlondon.domain.player.PlayerColor;

/**
 * Immutable board rendering model that separates London visual coordinates from domain rules.
 */
public final class BoardViewModel {
  private static final Map<String, LocationGeometry> LOCATION_GEOMETRY = createLocationGeometry();
  private static final Map<String, RouteGeometry> ROUTE_GEOMETRY = createRouteGeometry();

  private final List<BoardLocationViewModel> locations;
  private final List<BoardRouteViewModel> routes;
  private final List<ScoreMarkerViewModel> scoreMarkers;

  /**
   * Creates a board view model.
   *
   * @param locations renderable locations in stable order
   * @param routes renderable routes in stable order
   * @param scoreMarkers renderable player score markers
   */
  public BoardViewModel(
      List<BoardLocationViewModel> locations,
      List<BoardRouteViewModel> routes,
      List<ScoreMarkerViewModel> scoreMarkers) {
    this.locations = List.copyOf(locations);
    this.routes = List.copyOf(routes);
    this.scoreMarkers = List.copyOf(scoreMarkers);
  }

  /**
   * Creates a board view model from an immutable game snapshot.
   *
   * @param snapshot game snapshot containing board and player read models
   * @return a renderable board view model
   */
  public static BoardViewModel from(GameSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot");
    Map<String, PlayerColor> playerColors =
        snapshot.players().stream()
            .collect(Collectors.toUnmodifiableMap(PlayerSnapshot::id, PlayerSnapshot::color));
    Map<String, BoardLocationViewModel> locationIndex = createLocations(snapshot.locations());
    Set<String> affectedRouteIds = Set.copyOf(snapshot.rushHourAffectedRouteIds());
    List<BoardRouteViewModel> routeModels =
        snapshot.routes().stream()
            .map(route -> createRoute(route, locationIndex, playerColors, affectedRouteIds))
            .toList();
    List<ScoreMarkerViewModel> scoreMarkerModels =
        snapshot.players().stream().map(BoardViewModel::createScoreMarker).toList();
    return new BoardViewModel(List.copyOf(locationIndex.values()), routeModels, scoreMarkerModels);
  }

  /** Returns all renderable locations. */
  public List<BoardLocationViewModel> locations() {
    return Collections.unmodifiableList(locations);
  }

  /** Returns all renderable routes. */
  public List<BoardRouteViewModel> routes() {
    return Collections.unmodifiableList(routes);
  }

  /** Returns all renderable score markers. */
  public List<ScoreMarkerViewModel> scoreMarkers() {
    return Collections.unmodifiableList(scoreMarkers);
  }

  private static Map<String, BoardLocationViewModel> createLocations(
      List<LocationSnapshot> snapshots) {
    Map<String, BoardLocationViewModel> locations = new LinkedHashMap<>();
    for (LocationSnapshot location : snapshots) {
      LocationGeometry geometry = geometryForLocation(location.id());
      locations.put(
          location.id(),
          new BoardLocationViewModel(
              location.id(),
              location.displayName(),
              location.district(),
              geometry.position(),
              geometry.labelOffset()));
    }
    return Collections.unmodifiableMap(locations);
  }

  private static BoardRouteViewModel createRoute(
      RouteSnapshot route,
      Map<String, BoardLocationViewModel> locations,
      Map<String, PlayerColor> playerColors,
      Set<String> affectedRouteIds) {
    BoardLocationViewModel locationA = requireLocation(locations, route.locationAId());
    BoardLocationViewModel locationB = requireLocation(locations, route.locationBId());
    RouteGeometry geometry = ROUTE_GEOMETRY.getOrDefault(route.id(), RouteGeometry.straight());
    String claimedBy = route.claimedBy().orElse(null);
    PlayerColor playerColor = claimedBy == null ? null : playerColors.get(claimedBy);
    if (claimedBy != null && playerColor == null) {
      throw new IllegalArgumentException("unknown claiming player id: " + claimedBy);
    }
    return new BoardRouteViewModel(
        route.id(),
        locationA,
        locationB,
        route.length(),
        route.color(),
        route.kind(),
        route.requiredBusSymbols(),
        geometry.laneOffset(),
        geometry.curveOffset(),
        claimedBy,
        playerColor,
        affectedRouteIds.contains(route.id()));
  }

  private static ScoreMarkerViewModel createScoreMarker(PlayerSnapshot player) {
    return new ScoreMarkerViewModel(
        player.id(),
        player.color(),
        player.score(),
        ScoreTrackPositioner.positionForScore(player.score()));
  }

  private static LocationGeometry geometryForLocation(String locationId) {
    LocationGeometry geometry = LOCATION_GEOMETRY.get(locationId);
    if (geometry == null) {
      throw new IllegalArgumentException("missing board geometry for location: " + locationId);
    }
    return geometry;
  }

  private static BoardLocationViewModel requireLocation(
      Map<String, BoardLocationViewModel> locations, String locationId) {
    BoardLocationViewModel location = locations.get(locationId);
    if (location == null) {
      throw new IllegalArgumentException("route references unknown location: " + locationId);
    }
    return location;
  }

  private static Map<String, LocationGeometry> createLocationGeometry() {
    Map<String, LocationGeometry> geometry = new LinkedHashMap<>();
    putLocation(geometry, "REGENTS_PARK", 0.209, 0.147, -0.135, -0.025);
    putLocation(geometry, "BAKER_STREET", 0.077, 0.309, 0.055, -0.032);
    putLocation(geometry, "HYDE_PARK", 0.133, 0.738, -0.050, 0.038);
    putLocation(geometry, "KINGS_CROSS", 0.457, 0.137, -0.012, -0.036);
    putLocation(geometry, "BRITISH_MUSEUM", 0.393, 0.377, -0.185, 0.020);
    putLocation(geometry, "COVENT_GARDEN", 0.444, 0.521, 0.026, 0.030);
    putLocation(geometry, "PICCADILLY_CIRCUS", 0.312, 0.580, -0.205, -0.010);
    putLocation(geometry, "TRAFALGAR_SQUARE", 0.393, 0.635, 0.064, -0.030);
    putLocation(geometry, "BUCKINGHAM_PALACE", 0.249, 0.801, -0.055, 0.078);
    putLocation(geometry, "BIG_BEN", 0.439, 0.802, -0.034, 0.043);
    putLocation(geometry, "WATERLOO", 0.558, 0.699, 0.025, 0.004);
    putLocation(geometry, "GLOBE_THEATRE", 0.711, 0.620, 0.021, -0.006);
    putLocation(geometry, "ELEPHANT_CASTLE", 0.682, 0.879, 0.020, 0.038);
    putLocation(geometry, "THE_CHARTERHOUSE", 0.683, 0.278, -0.194, 0.018);
    putLocation(geometry, "ST_PAULS", 0.691, 0.469, -0.090, -0.030);
    putLocation(geometry, "BRICK_LANE", 0.915, 0.282, -0.040, -0.052);
    putLocation(geometry, "TOWER_OF_LONDON", 0.918, 0.609, -0.105, -0.020);
    return Collections.unmodifiableMap(geometry);
  }

  private static void putLocation(
      Map<String, LocationGeometry> geometry,
      String id,
      double x,
      double y,
      double labelOffsetX,
      double labelOffsetY) {
    geometry.put(
        id,
        new LocationGeometry(
            new NormalizedPoint(x, y), createLabelOffset(labelOffsetX, labelOffsetY)));
  }

  private static NormalizedOffset createLabelOffset(double x, double y) {
    return new NormalizedOffset(x, y);
  }

  private static Map<String, RouteGeometry> createRouteGeometry() {
    Map<String, RouteGeometry> geometry = new LinkedHashMap<>();
    putRoute(geometry, "R01", 0, -0.03);
    putRoute(geometry, "R02", 0, 0.00);
    putRoute(geometry, "R03", 0, -0.03);
    putRoute(geometry, "R04", 0, 0.02);
    putRoute(geometry, "R05", 0, 0.08);
    putRoute(geometry, "R06", 0, -0.02);
    putRoute(geometry, "R07", 0, 0.02);
    putRoute(geometry, "R08", 0, -0.06);
    putRoute(geometry, "R09", 0, 0.05);
    putRoute(geometry, "R10", 0, -0.04);
    putRoute(geometry, "R11", 0, -0.02);
    putRoute(geometry, "R12", -1, 0.0);
    putRoute(geometry, "R13", 1, 0.0);
    putRoute(geometry, "R14", -1, 0.0);
    putRoute(geometry, "R15", 1, 0.0);
    putRoute(geometry, "R16", -1, -0.07);
    putRoute(geometry, "R18", -1, 0.0);
    putRoute(geometry, "R19", 1, 0.0);
    putRoute(geometry, "R20", -1, 0.0);
    putRoute(geometry, "R21", 1, 0.0);
    putRoute(geometry, "R22", 0, 0.02);
    putRoute(geometry, "R23", 0, -0.02);
    putRoute(geometry, "R24", 0, -0.03);
    putRoute(geometry, "R25", 0, -0.02);
    putRoute(geometry, "R26", -1, 0.0);
    putRoute(geometry, "R27", 1, 0.0);
    putRoute(geometry, "R28", 0, 0.0);
    putRoute(geometry, "R29", 0, 0.03);
    putRoute(geometry, "R30", 0, -0.05);
    putRoute(geometry, "R31", 0, 0.0);
    putRoute(geometry, "R32", 0, 0.02);
    putRoute(geometry, "R33", 1, 0.0);
    putRoute(geometry, "R34", -1, 0.0);
    putRoute(geometry, "R35", 0, 0.0);
    putRoute(geometry, "R36", 0, -0.07);
    putRoute(geometry, "R37", 0, 0.02);
    putRoute(geometry, "R38", 0, 0.0);
    putRoute(geometry, "R39", 0, 0.08);
    putRoute(geometry, "R40", 0, 0.09);
    putRoute(geometry, "R41", 1, -0.07);
    putRoute(geometry, "R42", -1, 0.0);
    putRoute(geometry, "R43", 1, 0.0);
    return Collections.unmodifiableMap(geometry);
  }

  private static void putRoute(
      Map<String, RouteGeometry> geometry, String id, int laneOffset, double curveOffset) {
    geometry.put(id, new RouteGeometry(laneOffset, curveOffset));
  }

  private record LocationGeometry(NormalizedPoint position, NormalizedOffset labelOffset) {}

  private record RouteGeometry(int laneOffset, double curveOffset) {
    static RouteGeometry straight() {
      return new RouteGeometry(0, 0.0);
    }
  }
}
