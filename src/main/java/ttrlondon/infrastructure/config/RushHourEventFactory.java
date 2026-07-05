package ttrlondon.infrastructure.config;

import static ttrlondon.infrastructure.config.BoardFactory.BAKER_STREET;
import static ttrlondon.infrastructure.config.BoardFactory.BIG_BEN;
import static ttrlondon.infrastructure.config.BoardFactory.BRICK_LANE;
import static ttrlondon.infrastructure.config.BoardFactory.BRITISH_MUSEUM;
import static ttrlondon.infrastructure.config.BoardFactory.BUCKINGHAM_PALACE;
import static ttrlondon.infrastructure.config.BoardFactory.COVENT_GARDEN;
import static ttrlondon.infrastructure.config.BoardFactory.ELEPHANT_CASTLE;
import static ttrlondon.infrastructure.config.BoardFactory.GLOBE_THEATRE;
import static ttrlondon.infrastructure.config.BoardFactory.HYDE_PARK;
import static ttrlondon.infrastructure.config.BoardFactory.KINGS_CROSS;
import static ttrlondon.infrastructure.config.BoardFactory.PICCADILLY_CIRCUS;
import static ttrlondon.infrastructure.config.BoardFactory.REGENTS_PARK;
import static ttrlondon.infrastructure.config.BoardFactory.ST_PAULS;
import static ttrlondon.infrastructure.config.BoardFactory.THE_CHARTERHOUSE;
import static ttrlondon.infrastructure.config.BoardFactory.TOWER_OF_LONDON;
import static ttrlondon.infrastructure.config.BoardFactory.TRAFALGAR_SQUARE;
import static ttrlondon.infrastructure.config.BoardFactory.WATERLOO;

import java.util.List;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;
import ttrlondon.domain.rushhour.RouteSelectors;
import ttrlondon.domain.rushhour.RushHourEvent;
import ttrlondon.domain.rushhour.RushHourManager;
import ttrlondon.domain.random.ShuffleStrategy;

/**
 * Creates data-driven Rush Hour events for the London board.
 */
public final class RushHourEventFactory {
  private RushHourEventFactory() {}

  /** Creates the London Rush Hour manager with shuffled event order. */
  public static RushHourManager createLondonRushHourManager(ShuffleStrategy shuffleStrategy) {
    List<RushHourEvent> events = createLondonEvents();
    return new RushHourManager(
        events, shuffleStrategy.shuffle(events.stream().map(RushHourEvent::id).toList()), shuffleStrategy);
  }

  /** Creates the eight London Rush Hour event definitions. */
  public static List<RushHourEvent> createLondonEvents() {
    return List.of(
        event(
            "RH01",
            "Central Gridlock",
            "Traffic locks up the central theatre district.",
            RouteSelectors.touchingLocations(
                PICCADILLY_CIRCUS, COVENT_GARDEN, TRAFALGAR_SQUARE)),
        event(
            "RH02",
            "Royal Procession",
            "A royal route diverts buses around Westminster.",
            RouteSelectors.touchingLocations(BUCKINGHAM_PALACE, BIG_BEN, HYDE_PARK)),
        event(
            "RH03",
            "Museum Crowds",
            "Visitors flood the museum and market streets.",
            RouteSelectors.touchingLocations(BRITISH_MUSEUM, COVENT_GARDEN, THE_CHARTERHOUSE)),
        event(
            "RH04",
            "East End Market",
            "Market traffic slows the eastern corridors.",
            RouteSelectors.touchingLocations(BRICK_LANE, ST_PAULS, TOWER_OF_LONDON)),
        event(
            "RH05",
            "South Bank Surge",
            "South Bank crowds tighten Thames crossings.",
            RouteSelectors.touchingLocations(WATERLOO, GLOBE_THEATRE, ELEPHANT_CASTLE)),
        event(
            "RH06",
            "Northern Commuters",
            "Morning commuters crowd the northern lines.",
            RouteSelectors.touchingLocations(REGENTS_PARK, BAKER_STREET, KINGS_CROSS)),
        event(
            "RH07",
            "Thames Tide",
            "River traffic disrupts Thames-side and Ferry routes.",
            RouteSelectors.anyOf(
                RouteSelectors.byRouteIds("R28", "R36", "R39", "R42", "R43"),
                RouteSelectors.byKind(RouteKind.FERRY))),
        event(
            "RH08",
            "Colour Bottleneck",
            "Neutral routes become costly detours.",
            RouteSelectors.byColor(RouteColor.GREY)));
  }

  private static RushHourEvent event(String id, String title, String description, ttrlondon.domain.rushhour.RouteSelector selector) {
    return new RushHourEvent(id, title, description, selector, 1, 2);
  }
}
