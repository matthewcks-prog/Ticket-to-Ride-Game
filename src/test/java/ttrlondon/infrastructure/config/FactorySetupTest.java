package ttrlondon.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.board.RouteKind;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/** Tests London board, deck, and game setup factories. */
final class FactorySetupTest {
  @Test
  void boardFactoryCreatesAllLondonLocationsAndRoutes() {
    Board board = BoardFactory.createLondonBoard();

    assertEquals(17, board.locations().size());
    assertEquals(43, board.routes().size());
    assertRoute(
        board,
        "R06",
        BoardFactory.BAKER_STREET,
        BoardFactory.PICCADILLY_CIRCUS,
        RouteColor.GREY,
        4);
    assertRoute(
        board,
        "R16",
        BoardFactory.PICCADILLY_CIRCUS,
        BoardFactory.HYDE_PARK,
        RouteColor.GREY,
        2);
    assertRoute(
        board,
        "R41",
        BoardFactory.PICCADILLY_CIRCUS,
        BoardFactory.HYDE_PARK,
        RouteColor.GREY,
        2);
    assertEquals(
        Set.of(
            BoardFactory.REGENTS_PARK,
            BoardFactory.BAKER_STREET,
            BoardFactory.HYDE_PARK,
            BoardFactory.KINGS_CROSS,
            BoardFactory.BRITISH_MUSEUM,
            BoardFactory.COVENT_GARDEN,
            BoardFactory.PICCADILLY_CIRCUS,
            BoardFactory.TRAFALGAR_SQUARE,
            BoardFactory.BUCKINGHAM_PALACE,
            BoardFactory.BIG_BEN,
            BoardFactory.WATERLOO,
            BoardFactory.GLOBE_THEATRE,
            BoardFactory.ELEPHANT_CASTLE,
            BoardFactory.THE_CHARTERHOUSE,
            BoardFactory.ST_PAULS,
            BoardFactory.BRICK_LANE,
            BoardFactory.TOWER_OF_LONDON),
        board.locations().stream().map(location -> location.id()).collect(Collectors.toSet()));
  }

  @Test
  void boardFactoryEncodesAllDoubleRouteGroups() {
    Board board = BoardFactory.createLondonBoard();

    assertDoubleGroup(
        board,
        "D1",
        routeSpec(BoardFactory.PICCADILLY_CIRCUS, BoardFactory.COVENT_GARDEN, RouteColor.GREEN, 1),
        routeSpec(
            BoardFactory.PICCADILLY_CIRCUS, BoardFactory.COVENT_GARDEN, RouteColor.YELLOW, 1));
    assertDoubleGroup(
        board,
        "D2",
        routeSpec(
            BoardFactory.PICCADILLY_CIRCUS,
            BoardFactory.TRAFALGAR_SQUARE,
            RouteColor.BLUE,
            1),
        routeSpec(
            BoardFactory.PICCADILLY_CIRCUS,
            BoardFactory.TRAFALGAR_SQUARE,
            RouteColor.ORANGE,
            1));
    assertDoubleGroup(
        board,
        "D3",
        routeSpec(BoardFactory.COVENT_GARDEN, BoardFactory.TRAFALGAR_SQUARE, RouteColor.BLACK, 1),
        routeSpec(BoardFactory.COVENT_GARDEN, BoardFactory.TRAFALGAR_SQUARE, RouteColor.PINK, 1));
    assertDoubleGroup(
        board,
        "D4",
        routeSpec(BoardFactory.COVENT_GARDEN, BoardFactory.ST_PAULS, RouteColor.GREY, 4),
        routeSpec(BoardFactory.COVENT_GARDEN, BoardFactory.ST_PAULS, RouteColor.GREY, 4));
    assertDoubleGroup(
        board,
        "D5",
        routeSpec(BoardFactory.HYDE_PARK, BoardFactory.BUCKINGHAM_PALACE, RouteColor.YELLOW, 1),
        routeSpec(BoardFactory.HYDE_PARK, BoardFactory.BUCKINGHAM_PALACE, RouteColor.ORANGE, 1));
    assertDoubleGroup(
        board,
        "D6",
        routeSpec(BoardFactory.ST_PAULS, BoardFactory.TOWER_OF_LONDON, RouteColor.PINK, 3),
        routeSpec(BoardFactory.ST_PAULS, BoardFactory.TOWER_OF_LONDON, RouteColor.YELLOW, 3));
    assertDoubleGroup(
        board,
        "D7",
        routeSpec(BoardFactory.PICCADILLY_CIRCUS, BoardFactory.HYDE_PARK, RouteColor.GREY, 2),
        routeSpec(BoardFactory.PICCADILLY_CIRCUS, BoardFactory.HYDE_PARK, RouteColor.GREY, 2));
    assertDoubleGroup(
        board,
        "D8",
        routeSpec(BoardFactory.ST_PAULS, BoardFactory.GLOBE_THEATRE, RouteColor.GREY, 1),
        routeSpec(BoardFactory.ST_PAULS, BoardFactory.GLOBE_THEATRE, RouteColor.GREY, 1));
  }

  @Test
  void boardFactoryMarksSelectedThamesRoutesAsFerries() {
    Board board = BoardFactory.createLondonBoard();

    assertFerryRoute(board, "R28", RouteColor.BLUE, 1, 1);
    assertFerryRoute(board, "R39", RouteColor.GREY, 3, 1);
    assertFerryRoute(board, "R42", RouteColor.GREY, 1, 1);
    assertFerryRoute(board, "R43", RouteColor.GREY, 1, 1);
    assertEquals(RouteKind.STANDARD, board.findRoute("R27").orElseThrow().kind());
  }

  @Test
  void deckFactoryCreatesExpectedTransportAndDestinationDecks() {
    Board board = BoardFactory.createLondonBoard();
    TransportCardDeck transportDeck =
        DeckFactory.createTransportDeck(new FixedOrderShuffleStrategy());
    DestinationTicketDeck ticketDeck =
        DeckFactory.createDestinationTicketDeck(board, new FixedOrderShuffleStrategy());

    assertEquals(44, transportDeck.drawPileSize());
    assertEquals(expectedCardCounts(), countCards(transportDeck.drawPileSnapshot()));
    assertEquals(20, ticketDeck.size());
    assertEquals(
        List.of(
            "T01",
            "T02",
            "T03",
            "T04",
            "T05",
            "T06",
            "T07",
            "T08",
            "T09",
            "T10",
            "T11",
            "T12",
            "T13",
            "T14",
            "T15",
            "T16",
            "T17",
            "T18",
            "T19",
            "T20"),
        ticketDeck.ticketsSnapshot().stream().map(ticket -> ticket.id()).toList());
  }

  @Test
  void gameFactoryExecutesLondonSetupWithDeterministicShuffling() {
    Game game =
        GameFactory.createNewGame(
            List.of("Ada", "Grace", "Katherine"), new FixedOrderShuffleStrategy());

    assertEquals(17, game.board().locations().size());
    assertEquals(43, game.board().routes().size());
    assertEquals("P1", game.turnManager().currentPlayerId());
    assertEquals(5, game.faceUpDisplay().visibleCards().size());
    assertTrue(game.faceUpDisplay().busCount() < 3);
    assertEquals(20 - (2 * game.players().size()), game.destinationTicketDeck().size());

    for (Player player : game.players()) {
      assertEquals(2, player.hand().size());
      assertEquals(2, player.tickets().size());
      assertEquals(Player.STARTING_BUSES, player.busesRemaining());
      assertEquals(0, player.score());
    }
  }

  @Test
  void gameFactoryAppliesInitialBusFlushDuringFaceUpReveal() {
    Game game =
        GameFactory.createNewGame(
            List.of("Ada", "Grace"),
            "P1",
            new FixedOrderShuffleStrategy(),
            List.of(
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.BUS,
                CardColor.YELLOW,
                CardColor.ORANGE,
                CardColor.BLUE,
                CardColor.GREEN,
                CardColor.BLACK,
                CardColor.PINK,
                CardColor.YELLOW),
            DeckFactory.createDestinationTicketDeck(
                BoardFactory.createLondonBoard(), new FixedOrderShuffleStrategy()));

    assertEquals(
        List.of(
            CardColor.BLUE,
            CardColor.GREEN,
            CardColor.BLACK,
            CardColor.PINK,
            CardColor.YELLOW),
        game.faceUpDisplay().visibleCards());
    assertEquals(5, game.transportCardDeck().discardPileSize());
  }

  @Test
  void setupDraftDefersInitialDestinationTicketChoices() {
    List<Player> players =
        List.of(
            new Player("P1", "Ada", PlayerColor.RED),
            new Player("P2", "Grace", PlayerColor.BLUE));

    GameSetupDraft draft =
        GameFactory.createSetupDraft(players, new FixedOrderShuffleStrategy());

    assertEquals(2, draft.initialTicketOptionsByPlayerId().get("P1").size());
    assertEquals(2, draft.initialTicketOptionsByPlayerId().get("P2").size());
    assertEquals(16, draft.destinationTicketDeck().size());
    assertTrue(draft.players().stream().allMatch(player -> player.tickets().isEmpty()));
  }

  @Test
  void setupCompletionKeepsSelectedInitialTicketsAndReturnsUnkeptToBottom() {
    List<Player> players =
        List.of(
            new Player("P1", "Ada", PlayerColor.RED),
            new Player("P2", "Grace", PlayerColor.BLUE));
    GameSetupDraft draft =
        GameFactory.createSetupDraft(players, new FixedOrderShuffleStrategy());
    Map<String, List<String>> keptTicketIdsByPlayerId = new LinkedHashMap<>();
    keptTicketIdsByPlayerId.put(
        "P1", List.of(draft.initialTicketOptionsByPlayerId().get("P1").get(0).id()));
    keptTicketIdsByPlayerId.put(
        "P2", List.of(draft.initialTicketOptionsByPlayerId().get("P2").get(1).id()));
    List<String> unkeptIds =
        List.of(
            draft.initialTicketOptionsByPlayerId().get("P1").get(1).id(),
            draft.initialTicketOptionsByPlayerId().get("P2").get(0).id());

    Game game = GameFactory.createNewGame(draft, "P2", keptTicketIdsByPlayerId);

    assertEquals("P2", game.turnManager().currentPlayerId());
    assertEquals(1, game.players().get(0).tickets().size());
    assertEquals(1, game.players().get(1).tickets().size());
    List<String> ticketDeckIds =
        new ArrayList<>(
            game.destinationTicketDeck().ticketsSnapshot().stream()
                .map(ticket -> ticket.id())
                .toList());
    assertEquals(unkeptIds, ticketDeckIds.subList(ticketDeckIds.size() - 2, ticketDeckIds.size()));
  }

  private static void assertDoubleGroup(
      Board board, String groupId, RouteSpec first, RouteSpec second) {
    List<Route> routes = board.routesInDoubleGroup(groupId);

    assertEquals(2, routes.size());
    assertTrue(containsRoute(routes, first));
    assertTrue(containsRoute(routes, second));
  }

  private static void assertRoute(
      Board board,
      String routeId,
      String locationA,
      String locationB,
      RouteColor color,
      int length) {
    Route route = board.findRoute(routeId).orElseThrow();

    assertEquals(locationA, route.locationA().id());
    assertEquals(locationB, route.locationB().id());
    assertEquals(color, route.color());
    assertEquals(length, route.length());
  }

  private static void assertFerryRoute(
      Board board, String routeId, RouteColor color, int length, int requiredBusSymbols) {
    Route route = board.findRoute(routeId).orElseThrow();

    assertEquals(RouteKind.FERRY, route.kind());
    assertEquals(color, route.color());
    assertEquals(length, route.length());
    assertEquals(requiredBusSymbols, route.requiredBusSymbols());
  }

  private static boolean containsRoute(List<Route> routes, RouteSpec expected) {
    for (Route route : routes) {
      boolean endpointsMatch =
          route.locationA().id().equals(expected.locationA())
              && route.locationB().id().equals(expected.locationB());
      if (endpointsMatch
          && route.color() == expected.color()
          && route.length() == expected.length()) {
        return true;
      }
    }
    return false;
  }

  private static RouteSpec routeSpec(
      String locationA, String locationB, RouteColor color, int length) {
    return new RouteSpec(locationA, locationB, color, length);
  }

  private static Map<CardColor, Integer> expectedCardCounts() {
    Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
    counts.put(CardColor.BLUE, 6);
    counts.put(CardColor.GREEN, 6);
    counts.put(CardColor.BLACK, 6);
    counts.put(CardColor.PINK, 6);
    counts.put(CardColor.YELLOW, 6);
    counts.put(CardColor.ORANGE, 6);
    counts.put(CardColor.BUS, 8);
    return counts;
  }

  private static Map<CardColor, Integer> countCards(List<CardColor> cards) {
    Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
    for (CardColor color : CardColor.values()) {
      counts.put(color, 0);
    }
    for (CardColor card : cards) {
      counts.merge(card, 1, Integer::sum);
    }
    return counts;
  }

  private record RouteSpec(String locationA, String locationB, RouteColor color, int length) {}
}
