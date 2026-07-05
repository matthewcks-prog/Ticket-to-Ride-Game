package ttrlondon.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import org.junit.jupiter.api.Test;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.domain.game.Game;
import ttrlondon.infrastructure.config.GameFactory;
import ttrlondon.infrastructure.random.FixedOrderShuffleStrategy;

/** Tests transportation-card market interactions exposed by the Swing panel. */
final class CardMarketPanelTest {
  @Test
  void blindDeckIsVisibleAndSubmitsBlindDrawIntentForActivePlayer() {
    AtomicReference<String> drawnPlayerId = new AtomicReference<>();
    CardMarketPanel panel =
        new CardMarketPanel((playerId, index) -> {}, drawnPlayerId::set);
    Game game =
        GameFactory.createNewGame(
            List.of("Ada", "Grace"), new FixedOrderShuffleStrategy());

    panel.onGameStateChanged(GameSnapshot.from(game));

    JButton blindDeckButton = findButton(panel, "blindDeckButton");
    assertNotNull(blindDeckButton);
    assertTrue(blindDeckButton.isEnabled());
    assertTrue(blindDeckButton.getText().contains("Blind Deck"));

    blindDeckButton.doClick();

    assertEquals("P1", drawnPlayerId.get());
  }

  private static JButton findButton(Container container, String name) {
    for (Component component : container.getComponents()) {
      if (component instanceof JButton button && name.equals(button.getName())) {
        return button;
      }
      if (component instanceof Container childContainer) {
        JButton nestedButton = findButton(childContainer, name);
        if (nestedButton != null) {
          return nestedButton;
        }
      }
    }
    return null;
  }
}
