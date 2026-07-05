package ttrlondon.application.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.domain.common.Text;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.ticket.DestinationTicket;

/**
 * Command that draws destination tickets and keeps the player-selected subset.
 */
public final class DrawDestinationTicketsCommand implements GameCommand {
  private static final int TICKETS_DRAWN_PER_ACTION = 2;

  private final String playerId;
  private final List<String> keptTicketIds;

  /**
   * Creates a destination-ticket draw command.
   *
   * @param playerId drawing player identifier
   * @param keptTicketIds identifiers of drawn tickets the player chooses to keep
   */
  public DrawDestinationTicketsCommand(String playerId, List<String> keptTicketIds) {
    this.playerId = Text.requireNonBlank(playerId, "playerId");
    this.keptTicketIds = List.copyOf(keptTicketIds);
    if (this.keptTicketIds.stream().anyMatch(id -> id == null || id.isBlank())) {
      throw new IllegalArgumentException("kept ticket ids must not be blank");
    }
  }

  /** Returns the drawing player identifier. */
  public String playerId() {
    return playerId;
  }

  /** Returns the kept ticket identifiers. */
  public List<String> keptTicketIds() {
    return List.copyOf(keptTicketIds);
  }

  @Override
  public CommandResult execute(Game game) {
    Objects.requireNonNull(game, "game");
    CommandResult validation = validate(game);
    if (validation.isFailure()) {
      return validation;
    }

    Player player = findPlayer(game, playerId);
    List<DestinationTicket> drawnTickets = game.destinationTicketDeck().drawForTurn();
    List<DestinationTicket> keptTickets = keptTicketsFrom(drawnTickets);
    player.addTickets(keptTickets);
    game.destinationTicketDeck().returnUnkeptToBottom(drawnTickets, keptTickets);
    game.endCurrentTurn();
    return CommandResult.success("Destination tickets drawn.");
  }

  private CommandResult validate(Game game) {
    if (!game.acceptsPlayerActions()) {
      return CommandResult.failure("Game is not accepting player actions.");
    }
    if (!game.turnManager().isCurrentPlayer(playerId)) {
      return CommandResult.failure("It is not this player's turn.");
    }
    if (game.findPlayer(playerId).isEmpty()) {
      return CommandResult.failure("Player does not exist.");
    }
    if (game.destinationTicketDeck().size() == 0) {
      return CommandResult.failure("Destination ticket deck is empty.");
    }
    if (keptTicketIds.isEmpty()) {
      return CommandResult.failure("At least one destination ticket must be kept.");
    }
    if (new HashSet<>(keptTicketIds).size() != keptTicketIds.size()) {
      return CommandResult.failure("Kept destination ticket ids must be unique.");
    }

    List<DestinationTicket> availableDraw =
        game.destinationTicketDeck().ticketsSnapshot().stream()
            .limit(TICKETS_DRAWN_PER_ACTION)
            .toList();
    Set<String> drawnTicketIds = new HashSet<>();
    for (DestinationTicket ticket : availableDraw) {
      drawnTicketIds.add(ticket.id());
    }
    if (!drawnTicketIds.containsAll(keptTicketIds)) {
      return CommandResult.failure("Kept tickets must come from the tickets drawn this action.");
    }
    return CommandResult.success("Destination ticket draw is valid.");
  }

  private List<DestinationTicket> keptTicketsFrom(List<DestinationTicket> drawnTickets) {
    Set<String> keptIds = new HashSet<>(keptTicketIds);
    List<DestinationTicket> keptTickets = new ArrayList<>();
    for (DestinationTicket ticket : drawnTickets) {
      if (keptIds.contains(ticket.id())) {
        keptTickets.add(ticket);
      }
    }
    return keptTickets;
  }

  private static Player findPlayer(Game game, String playerId) {
    return game.findPlayer(playerId)
        .orElseThrow(() -> new IllegalArgumentException("player does not exist"));
  }
}
