package ttrlondon.infrastructure.config;

import java.util.List;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.ColouredRouteRequirement;
import ttrlondon.domain.board.FerryRouteRequirement;
import ttrlondon.domain.board.GreyRouteRequirement;
import ttrlondon.domain.board.Location;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;

/**
 * Creates board definitions from the authoritative London board data.
 */
public final class BoardFactory {
  public static final String REGENTS_PARK = "REGENTS_PARK";
  public static final String BAKER_STREET = "BAKER_STREET";
  public static final String HYDE_PARK = "HYDE_PARK";
  public static final String KINGS_CROSS = "KINGS_CROSS";
  public static final String BRITISH_MUSEUM = "BRITISH_MUSEUM";
  public static final String COVENT_GARDEN = "COVENT_GARDEN";
  public static final String PICCADILLY_CIRCUS = "PICCADILLY_CIRCUS";
  public static final String TRAFALGAR_SQUARE = "TRAFALGAR_SQUARE";
  public static final String BUCKINGHAM_PALACE = "BUCKINGHAM_PALACE";
  public static final String BIG_BEN = "BIG_BEN";
  public static final String WATERLOO = "WATERLOO";
  public static final String GLOBE_THEATRE = "GLOBE_THEATRE";
  public static final String ELEPHANT_CASTLE = "ELEPHANT_CASTLE";
  public static final String THE_CHARTERHOUSE = "THE_CHARTERHOUSE";
  public static final String ST_PAULS = "ST_PAULS";
  public static final String BRICK_LANE = "BRICK_LANE";
  public static final String TOWER_OF_LONDON = "TOWER_OF_LONDON";

  private BoardFactory() {}

  /** Creates the complete Ticket to Ride: London board. */
  public static Board createLondonBoard() {
    List<Location> locations = createLondonLocations();
    return new Board(locations, createLondonRoutes(locations));
  }

  /** Creates the 17 London board locations in stable board order. */
  public static List<Location> createLondonLocations() {
    return List.of(
        new Location(REGENTS_PARK, "Regent's Park", 5),
        new Location(BAKER_STREET, "Baker Street", 5),
        new Location(HYDE_PARK, "Hyde Park", 5),
        new Location(KINGS_CROSS, "King's Cross", 5),
        new Location(BRITISH_MUSEUM, "British Museum", 1),
        new Location(COVENT_GARDEN, "Covent Garden", 1),
        new Location(PICCADILLY_CIRCUS, "Piccadilly Circus", 2),
        new Location(TRAFALGAR_SQUARE, "Trafalgar Square", 2),
        new Location(BUCKINGHAM_PALACE, "Buckingham Palace", 2),
        new Location(BIG_BEN, "Big Ben", 2),
        new Location(WATERLOO, "Waterloo", 3),
        new Location(GLOBE_THEATRE, "Globe Theatre", 3),
        new Location(ELEPHANT_CASTLE, "Elephant & Castle", 3),
        new Location(THE_CHARTERHOUSE, "The Charterhouse", 4),
        new Location(ST_PAULS, "St Paul's", 4),
        new Location(BRICK_LANE, "Brick Lane", 4),
        new Location(TOWER_OF_LONDON, "Tower of London", 4));
  }

  private static List<Route> createLondonRoutes(List<Location> locations) {
    LocationIndex index = new LocationIndex(locations);
    return List.of(
        route("R01", index, REGENTS_PARK, BAKER_STREET, RouteColor.BLUE, 2, null),
        route("R02", index, REGENTS_PARK, KINGS_CROSS, RouteColor.GREEN, 3, null),
        route("R03", index, REGENTS_PARK, BRITISH_MUSEUM, RouteColor.YELLOW, 3, null),
        route("R04", index, BAKER_STREET, BRITISH_MUSEUM, RouteColor.ORANGE, 4, null),
        route("R05", index, BAKER_STREET, HYDE_PARK, RouteColor.BLACK, 4, null),
        route("R06", index, BAKER_STREET, PICCADILLY_CIRCUS, RouteColor.GREY, 4, null),
        route("R07", index, KINGS_CROSS, BRITISH_MUSEUM, RouteColor.BLACK, 2, null),
        route("R08", index, KINGS_CROSS, THE_CHARTERHOUSE, RouteColor.PINK, 3, null),
        route("R09", index, BRITISH_MUSEUM, THE_CHARTERHOUSE, RouteColor.BLUE, 4, null),
        route("R10", index, BRITISH_MUSEUM, PICCADILLY_CIRCUS, RouteColor.GREY, 2, null),
        route("R11", index, BRITISH_MUSEUM, COVENT_GARDEN, RouteColor.GREY, 2, null),
        route("R12", index, PICCADILLY_CIRCUS, COVENT_GARDEN, RouteColor.GREEN, 1, "D1"),
        route("R13", index, PICCADILLY_CIRCUS, COVENT_GARDEN, RouteColor.YELLOW, 1, "D1"),
        route("R14", index, PICCADILLY_CIRCUS, TRAFALGAR_SQUARE, RouteColor.BLUE, 1, "D2"),
        route("R15", index, PICCADILLY_CIRCUS, TRAFALGAR_SQUARE, RouteColor.ORANGE, 1, "D2"),
        route("R16", index, PICCADILLY_CIRCUS, HYDE_PARK, RouteColor.GREY, 2, "D7"),
        route("R17", index, PICCADILLY_CIRCUS, BUCKINGHAM_PALACE, RouteColor.PINK, 3, null),
        route("R18", index, COVENT_GARDEN, TRAFALGAR_SQUARE, RouteColor.BLACK, 1, "D3"),
        route("R19", index, COVENT_GARDEN, TRAFALGAR_SQUARE, RouteColor.PINK, 1, "D3"),
        route("R20", index, COVENT_GARDEN, ST_PAULS, RouteColor.GREY, 4, "D4"),
        route("R21", index, COVENT_GARDEN, ST_PAULS, RouteColor.GREY, 4, "D4"),
        route("R22", index, TRAFALGAR_SQUARE, BIG_BEN, RouteColor.GREY, 2, null),
        route("R23", index, TRAFALGAR_SQUARE, WATERLOO, RouteColor.GREY, 2, null),
        route("R24", index, BUCKINGHAM_PALACE, BIG_BEN, RouteColor.GREEN, 3, null),
        route("R25", index, BUCKINGHAM_PALACE, TRAFALGAR_SQUARE, RouteColor.GREY, 2, null),
        route("R26", index, HYDE_PARK, BUCKINGHAM_PALACE, RouteColor.YELLOW, 1, "D5"),
        route("R27", index, HYDE_PARK, BUCKINGHAM_PALACE, RouteColor.ORANGE, 1, "D5"),
        ferry("R28", index, BIG_BEN, WATERLOO, RouteColor.BLUE, 1, 1, null),
        route("R29", index, BIG_BEN, ELEPHANT_CASTLE, RouteColor.YELLOW, 4, null),
        route("R30", index, THE_CHARTERHOUSE, BRICK_LANE, RouteColor.GREEN, 3, null),
        route("R31", index, THE_CHARTERHOUSE, ST_PAULS, RouteColor.BLACK, 1, null),
        route("R32", index, ST_PAULS, BRICK_LANE, RouteColor.ORANGE, 3, null),
        route("R33", index, ST_PAULS, TOWER_OF_LONDON, RouteColor.PINK, 3, "D6"),
        route("R34", index, ST_PAULS, TOWER_OF_LONDON, RouteColor.YELLOW, 3, "D6"),
        route("R35", index, BRICK_LANE, TOWER_OF_LONDON, RouteColor.BLUE, 3, null),
        route("R36", index, WATERLOO, GLOBE_THEATRE, RouteColor.PINK, 3, null),
        route("R37", index, WATERLOO, ELEPHANT_CASTLE, RouteColor.ORANGE, 3, null),
        route("R38", index, GLOBE_THEATRE, ELEPHANT_CASTLE, RouteColor.GREEN, 3, null),
        ferry("R39", index, GLOBE_THEATRE, TOWER_OF_LONDON, RouteColor.GREY, 3, 1, null),
        route("R40", index, ELEPHANT_CASTLE, TOWER_OF_LONDON, RouteColor.BLACK, 4, null),
        route("R41", index, PICCADILLY_CIRCUS, HYDE_PARK, RouteColor.GREY, 2, "D7"),
        ferry("R42", index, ST_PAULS, GLOBE_THEATRE, RouteColor.GREY, 1, 1, "D8"),
        ferry("R43", index, ST_PAULS, GLOBE_THEATRE, RouteColor.GREY, 1, 1, "D8"));
  }

  private static Route route(
      String id,
      LocationIndex index,
      String locationA,
      String locationB,
      RouteColor color,
      int length,
      String doubleGroupId) {
    return new Route(
        id,
        index.get(locationA),
        index.get(locationB),
        length,
        color,
        doubleGroupId,
        color == RouteColor.GREY
            ? new GreyRouteRequirement(length)
            : new ColouredRouteRequirement(color, length));
  }

  private static Route ferry(
      String id,
      LocationIndex index,
      String locationA,
      String locationB,
      RouteColor color,
      int length,
      int requiredBusSymbols,
      String doubleGroupId) {
    return new Route(
        id,
        index.get(locationA),
        index.get(locationB),
        length,
        color,
        RouteKind.FERRY,
        requiredBusSymbols,
        doubleGroupId,
        new FerryRouteRequirement(color, length, requiredBusSymbols));
  }

  private record LocationIndex(List<Location> locations) {
    Location get(String locationId) {
      for (Location location : locations) {
        if (location.id().equals(locationId)) {
          return location;
        }
      }
      throw new IllegalArgumentException("unknown location id: " + locationId);
    }
  }
}
