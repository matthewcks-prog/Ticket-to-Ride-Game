package ttrlondon.infrastructure.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ttrlondon.domain.board.Board;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.FaceUpDisplay;
import ttrlondon.domain.card.TransportCardDeck;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.domain.random.ShuffleStrategy;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.domain.ticket.DestinationTicketDeck;
import ttrlondon.domain.turn.TurnManager;
import ttrlondon.infrastructure.random.RandomShuffleStrategy;

/**
 * Coordinates complete Ticket to Ride: London game setup.
 */
public final class GameFactory {
  private static final int STARTING_TRANSPORT_CARDS = 2;
  private static final int STARTING_DESTINATION_TICKETS = 2;

  private GameFactory() {}

  /**
   * Creates a new London game with generated players and random deck shuffling.
   *
   * @param playerNames player names in clockwise order
   * @return fully initialised game
   */
  public static Game createNewGame(List<String> playerNames) {
    return createNewGame(playerNames, new RandomShuffleStrategy());
  }

  /**
   * Creates a new London game with generated players and injected shuffling.
   *
   * <p>The first supplied player starts. Initial destination ticket setup keeps both drawn tickets,
   * which satisfies the rules' keep-at-least-one requirement until a setup UI can collect choices.
   *
   * @param playerNames player names in clockwise order
   * @param shuffleStrategy strategy used to shuffle card decks
   * @return fully initialised game
   */
  public static Game createNewGame(List<String> playerNames, ShuffleStrategy shuffleStrategy) {
    List<Player> players = createPlayers(playerNames);
    return createNewGame(players, players.get(0).id(), shuffleStrategy);
  }

  /**
   * Creates a new London game using explicit player instances and starting player.
   *
   * @param players players in clockwise order
   * @param startingPlayerId player who takes the first turn
   * @param shuffleStrategy strategy used to shuffle card decks
   * @return fully initialised game
   */
  public static Game createNewGame(
      List<Player> players, String startingPlayerId, ShuffleStrategy shuffleStrategy) {
    Objects.requireNonNull(shuffleStrategy, "shuffleStrategy");
    List<Player> playerOrder = validatePlayers(players);
    if (playerOrder.stream().noneMatch(player -> player.id().equals(startingPlayerId))) {
      throw new IllegalArgumentException("starting player must be in player order");
    }
    Board board = BoardFactory.createLondonBoard();
    TransportCardDeck transportDeck = DeckFactory.createTransportDeck(shuffleStrategy);
    DestinationTicketDeck ticketDeck =
        DeckFactory.createDestinationTicketDeck(board, shuffleStrategy);
    return createConfiguredGame(playerOrder, startingPlayerId, board, transportDeck, ticketDeck);
  }

  static Game createNewGame(
      List<String> playerNames,
      String startingPlayerId,
      ShuffleStrategy shuffleStrategy,
      List<CardColor> transportCards,
      DestinationTicketDeck ticketDeck) {
    Objects.requireNonNull(shuffleStrategy, "shuffleStrategy");
    Objects.requireNonNull(transportCards, "transportCards");
    List<Player> playerOrder = validatePlayers(createPlayers(playerNames));
    if (playerOrder.stream().noneMatch(player -> player.id().equals(startingPlayerId))) {
      throw new IllegalArgumentException("starting player must be in player order");
    }
    return createConfiguredGame(
        playerOrder,
        startingPlayerId,
        BoardFactory.createLondonBoard(),
        new TransportCardDeck(shuffleStrategy.shuffle(transportCards), shuffleStrategy),
        ticketDeck);
  }

  /**
   * Creates pre-game setup state for UI-driven initial ticket choices.
   *
   * @param players players in clockwise order
   * @param shuffleStrategy strategy used to shuffle card decks
   * @return setup draft containing initial ticket options
   */
  public static GameSetupDraft createSetupDraft(
      List<Player> players, ShuffleStrategy shuffleStrategy) {
    Objects.requireNonNull(shuffleStrategy, "shuffleStrategy");
    List<Player> playerOrder = validatePlayers(players);
    Board board = BoardFactory.createLondonBoard();
    TransportCardDeck transportDeck = DeckFactory.createTransportDeck(shuffleStrategy);
    DestinationTicketDeck ticketDeck =
        DeckFactory.createDestinationTicketDeck(board, shuffleStrategy);
    dealStartingTransportCards(playerOrder, transportDeck);
    FaceUpDisplay faceUpDisplay = createFaceUpDisplay(transportDeck);
    Map<String, List<DestinationTicket>> ticketOptions =
        drawInitialDestinationTicketOptions(playerOrder, ticketDeck);
    return new GameSetupDraft(
        board, playerOrder, transportDeck, faceUpDisplay, ticketDeck, ticketOptions);
  }

  /**
   * Completes a setup draft after players choose initial tickets.
   *
   * @param draft pre-game setup draft
   * @param startingPlayerId player who takes the first turn
   * @param keptTicketIdsByPlayerId kept initial destination ticket identifiers by player
   * @return fully initialised running game
   */
  public static Game createNewGame(
      GameSetupDraft draft,
      String startingPlayerId,
      Map<String, List<String>> keptTicketIdsByPlayerId) {
    Objects.requireNonNull(draft, "draft");
    Objects.requireNonNull(keptTicketIdsByPlayerId, "keptTicketIdsByPlayerId");
    if (draft.players().stream().noneMatch(player -> player.id().equals(startingPlayerId))) {
      throw new IllegalArgumentException("starting player must be in player order");
    }
    applyInitialDestinationTicketChoices(draft, keptTicketIdsByPlayerId);
    TurnManager turnManager =
        new TurnManager(draft.players().stream().map(Player::id).toList(), startingPlayerId);
    return new Game(
        draft.board(),
        draft.players(),
        draft.transportCardDeck(),
        draft.faceUpDisplay(),
        draft.destinationTicketDeck(),
        turnManager,
        RushHourEventFactory.createLondonRushHourManager(new RandomShuffleStrategy()),
        GamePhase.RUNNING);
  }

  private static Game createConfiguredGame(
      List<Player> playerOrder,
      String startingPlayerId,
      Board board,
      TransportCardDeck transportDeck,
      DestinationTicketDeck ticketDeck) {
    dealStartingTransportCards(playerOrder, transportDeck);
    FaceUpDisplay faceUpDisplay = createFaceUpDisplay(transportDeck);
    dealStartingDestinationTickets(playerOrder, ticketDeck);
    TurnManager turnManager =
        new TurnManager(playerOrder.stream().map(Player::id).toList(), startingPlayerId);
    return new Game(
        board,
        playerOrder,
        transportDeck,
        faceUpDisplay,
        ticketDeck,
        turnManager,
        RushHourEventFactory.createLondonRushHourManager(transportDeck.shuffleStrategy()),
        GamePhase.RUNNING);
  }

  private static List<Player> createPlayers(List<String> playerNames) {
    Objects.requireNonNull(playerNames, "playerNames");
    if (playerNames.size() < PlayerSetupConfiguration.MIN_PLAYERS
        || playerNames.size() > PlayerSetupConfiguration.MAX_PLAYERS) {
      throw new IllegalArgumentException("London supports two to four players");
    }
    List<PlayerColor> colors =
        List.of(PlayerColor.RED, PlayerColor.WHITE, PlayerColor.BLUE, PlayerColor.YELLOW);
    List<Player> players = new ArrayList<>();
    for (int index = 0; index < playerNames.size(); index++) {
      String name = playerNames.get(index);
      players.add(new Player("P" + (index + 1), name, colors.get(index)));
    }
    return players;
  }

  private static List<Player> validatePlayers(List<Player> players) {
    Objects.requireNonNull(players, "players");
    if (players.size() < PlayerSetupConfiguration.MIN_PLAYERS
        || players.size() > PlayerSetupConfiguration.MAX_PLAYERS) {
      throw new IllegalArgumentException("London supports two to four players");
    }
    Set<PlayerColor> colors = EnumSet.noneOf(PlayerColor.class);
    List<String> ids = new ArrayList<>();
    for (Player player : players) {
      Objects.requireNonNull(player, "player");
      if (!colors.add(player.color())) {
        throw new IllegalArgumentException("player colours must be unique");
      }
      if (ids.contains(player.id())) {
        throw new IllegalArgumentException("player ids must be unique");
      }
      ids.add(player.id());
    }
    return List.copyOf(players);
  }

  private static void dealStartingTransportCards(
      List<Player> players, TransportCardDeck transportDeck) {
    for (int round = 0; round < STARTING_TRANSPORT_CARDS; round++) {
      for (Player player : players) {
        CardColor card =
            transportDeck.draw()
                .orElseThrow(() -> new IllegalStateException("transport deck exhausted in setup"));
        player.addCards(List.of(card));
      }
    }
  }

  private static FaceUpDisplay createFaceUpDisplay(TransportCardDeck transportDeck) {
    List<CardColor> visibleCards = new ArrayList<>();
    for (int slot = 0; slot < FaceUpDisplay.MAX_VISIBLE_CARDS; slot++) {
      transportDeck.draw().ifPresent(visibleCards::add);
    }
    FaceUpDisplay display = new FaceUpDisplay(visibleCards);
    display.enforceBusFlush(transportDeck);
    return display;
  }

  private static void dealStartingDestinationTickets(
      List<Player> players, DestinationTicketDeck ticketDeck) {
    for (Player player : players) {
      List<DestinationTicket> tickets = ticketDeck.draw(STARTING_DESTINATION_TICKETS);
      if (tickets.isEmpty()) {
        throw new IllegalStateException("destination ticket deck exhausted in setup");
      }
      player.addTickets(tickets);
    }
  }

  private static Map<String, List<DestinationTicket>> drawInitialDestinationTicketOptions(
      List<Player> players, DestinationTicketDeck ticketDeck) {
    Map<String, List<DestinationTicket>> ticketsByPlayerId = new LinkedHashMap<>();
    for (Player player : players) {
      List<DestinationTicket> tickets = ticketDeck.draw(STARTING_DESTINATION_TICKETS);
      if (tickets.isEmpty()) {
        throw new IllegalStateException("destination ticket deck exhausted in setup");
      }
      ticketsByPlayerId.put(player.id(), tickets);
    }
    return ticketsByPlayerId;
  }

  private static void applyInitialDestinationTicketChoices(
      GameSetupDraft draft, Map<String, List<String>> keptTicketIdsByPlayerId) {
    for (Player player : draft.players()) {
      if (!player.tickets().isEmpty()) {
        throw new IllegalStateException("setup draft has already been completed");
      }
      List<DestinationTicket> drawnTickets =
          draft.initialTicketOptionsByPlayerId().get(player.id());
      if (drawnTickets == null || drawnTickets.isEmpty()) {
        throw new IllegalArgumentException("missing initial tickets for player " + player.id());
      }
      List<String> keptIds = keptTicketIdsByPlayerId.get(player.id());
      if (keptIds == null || keptIds.isEmpty()) {
        throw new IllegalArgumentException("at least one initial ticket must be kept");
      }
      if (new HashSet<>(keptIds).size() != keptIds.size()) {
        throw new IllegalArgumentException("kept initial ticket ids must be unique");
      }
      List<DestinationTicket> keptTickets = keptTicketsFrom(drawnTickets, keptIds);
      player.addTickets(keptTickets);
      draft.destinationTicketDeck().returnUnkeptToBottom(drawnTickets, keptTickets);
    }
  }

  private static List<DestinationTicket> keptTicketsFrom(
      List<DestinationTicket> drawnTickets, List<String> keptIds) {
    Set<String> drawnIds = new HashSet<>();
    for (DestinationTicket ticket : drawnTickets) {
      drawnIds.add(ticket.id());
    }
    if (!drawnIds.containsAll(keptIds)) {
      throw new IllegalArgumentException("kept initial tickets must come from the dealt tickets");
    }
    Set<String> keptIdSet = new HashSet<>(keptIds);
    List<DestinationTicket> keptTickets = new ArrayList<>();
    for (DestinationTicket ticket : drawnTickets) {
      if (keptIdSet.contains(ticket.id())) {
        keptTickets.add(ticket);
      }
    }
    return keptTickets;
  }
}
