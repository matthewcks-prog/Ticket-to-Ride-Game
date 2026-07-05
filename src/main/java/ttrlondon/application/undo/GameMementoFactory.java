package ttrlondon.application.undo;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import ttrlondon.application.dto.TransportDrawProgress;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.game.Game;
import ttrlondon.domain.player.Player;

/**
 * Captures and restores application-level game mementos.
 */
public final class GameMementoFactory {
  /** Captures a memento from the supplied game and application draw progress. */
  public GameMemento capture(Game game, TransportDrawProgress drawProgress) {
    Objects.requireNonNull(game, "game");
    Objects.requireNonNull(drawProgress, "drawProgress");
    return new GameMemento(
        game.phase(),
        new GameMemento.TurnState(
            game.turnManager().currentPlayerId(),
            game.turnManager().isFinalRoundActive(),
            game.turnManager().triggeringPlayerId(),
            game.turnManager().finalTurnsRemaining()),
        game.players().stream()
            .map(
                player ->
                    new GameMemento.PlayerState(
                        player.id(),
                        player.hand(),
                        player.tickets(),
                        player.busesRemaining(),
                        player.score()))
            .toList(),
        game.board().routes().stream()
            .map(route -> new GameMemento.RouteState(route.id(), route.claimedBy().orElse(null)))
            .toList(),
        game.transportCardDeck().drawPileSnapshot(),
        game.transportCardDeck().discardPileSnapshot(),
        game.faceUpDisplay().visibleCards(),
        game.destinationTicketDeck().ticketsSnapshot(),
        new GameMemento.RushHourState(
            game.rushHourManager().phase(),
            game.rushHourManager().forecastEventId().orElse(null),
            game.rushHourManager().activeEventId().orElse(null),
            game.rushHourManager().turnsRemaining(),
            game.rushHourManager().eventDeckSnapshot(),
            game.rushHourManager().eventDiscardSnapshot(),
            game.rushHourManager().bonusPointsByPlayerId()),
        drawProgress);
  }

  /** Restores a previously captured memento into the supplied game aggregate. */
  public void restore(Game game, GameMemento memento) {
    Objects.requireNonNull(game, "game");
    Objects.requireNonNull(memento, "memento");
    Map<String, Player> playersById =
        game.players().stream().collect(Collectors.toMap(Player::id, Function.identity()));
    for (GameMemento.PlayerState playerState : memento.players()) {
      Player player = playersById.get(playerState.id());
      if (player == null) {
        throw new IllegalStateException("memento references unknown player: " + playerState.id());
      }
      player.restoreState(
          playerState.hand(),
          playerState.tickets(),
          playerState.busesRemaining(),
          playerState.score());
    }

    for (GameMemento.RouteState routeState : memento.routes()) {
      Route route = game.board().findRoute(routeState.id()).orElse(null);
      if (route == null) {
        throw new IllegalStateException("memento references unknown route: " + routeState.id());
      }
      route.restoreClaim(routeState.claimedBy());
    }

    game.transportCardDeck().restoreState(memento.transportDrawPile(), memento.transportDiscardPile());
    game.faceUpDisplay().restoreState(memento.faceUpCards());
    game.destinationTicketDeck().restoreState(memento.destinationTickets());
    game.rushHourManager().restoreState(
        memento.rushHourState().phase(),
        memento.rushHourState().forecastEventId(),
        memento.rushHourState().activeEventId(),
        memento.rushHourState().turnsRemaining(),
        memento.rushHourState().eventDeck(),
        memento.rushHourState().eventDiscard(),
        memento.rushHourState().bonusPointsByPlayerId());
    game.turnManager().restoreState(
        memento.turnState().currentPlayerId(),
        memento.turnState().finalRoundActive(),
        memento.turnState().triggeringPlayerId(),
        memento.turnState().finalTurnsRemaining());
    game.transitionTo(memento.phase());
  }
}
