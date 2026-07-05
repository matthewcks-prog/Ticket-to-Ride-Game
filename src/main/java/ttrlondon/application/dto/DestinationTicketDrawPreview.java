package ttrlondon.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable application result for previewing tickets drawn by the active player.
 */
public final class DestinationTicketDrawPreview {
  private final CommandResult commandResult;
  private final List<DestinationTicketSnapshot> tickets;

  /**
   * Creates a destination-ticket draw preview.
   *
   * @param commandResult validation outcome for starting a ticket draw interaction
   * @param tickets tickets the player may choose from when validation succeeds
   */
  public DestinationTicketDrawPreview(
      CommandResult commandResult, List<DestinationTicketSnapshot> tickets) {
    this.commandResult = Objects.requireNonNull(commandResult, "commandResult");
    this.tickets = List.copyOf(tickets);
  }

  /**
   * Creates a successful destination-ticket draw preview.
   *
   * @param tickets tickets available to keep
   * @return successful preview
   */
  public static DestinationTicketDrawPreview success(List<DestinationTicketSnapshot> tickets) {
    return new DestinationTicketDrawPreview(
        CommandResult.success("Choose at least one destination ticket to keep."), tickets);
  }

  /**
   * Creates a failed destination-ticket draw preview.
   *
   * @param message user-facing failure message
   * @return failed preview
   */
  public static DestinationTicketDrawPreview failure(String message) {
    return new DestinationTicketDrawPreview(CommandResult.failure(message), List.of());
  }

  /** Returns the validation result for the preview request. */
  public CommandResult commandResult() {
    return commandResult;
  }

  /** Returns tickets available in this preview. */
  public List<DestinationTicketSnapshot> tickets() {
    return Collections.unmodifiableList(tickets);
  }
}
