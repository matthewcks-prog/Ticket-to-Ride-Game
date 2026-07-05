package ttrlondon.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import ttrlondon.DemoApplicationFactory;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.commands.ClaimRoutePayment;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;

/** Verifies manual player viewing in the player panel. */
final class PlayerPanelTest {
  @Test
  void selectedViewedPlayerIsPreservedWhenActivePlayerChanges() {
    GameApplicationService service = DemoApplicationFactory.createDemoApplicationService();
    PlayerPanel panel = new PlayerPanel();
    GameSnapshot initial = service.getSnapshot();

    panel.onGameStateChanged(initial);
    assertEquals("Score: 0", scoreText(panel));

    service.executeCommand(
        new ClaimRouteCommand(
            "P1",
            "R28",
            new ClaimRoutePayment(
                new CardPayment(List.of(CardColor.BUS)),
                new CardPayment(List.of(CardColor.BLUE)))));
    panel.onGameStateChanged(service.getSnapshot());

    assertEquals("Matthew", selectedPlayerName(panel));
    assertEquals("Score: 3", scoreText(panel));
  }

  @SuppressWarnings("unchecked")
  private static String selectedPlayerName(PlayerPanel panel) {
    JComboBox<Object> comboBox = (JComboBox<Object>) findComponent(panel, JComboBox.class);
    return comboBox.getSelectedItem().toString();
  }

  private static String scoreText(PlayerPanel panel) {
    for (JLabel label : findComponents(panel, JLabel.class)) {
      if (label.getText().startsWith("Score:")) {
        return label.getText();
      }
    }
    throw new AssertionError("score label not found");
  }

  private static Component findComponent(Component component, Class<?> componentType) {
    if (componentType.isInstance(component)) {
      return component;
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        Component match = findComponent(child, componentType);
        if (match != null) {
          return match;
        }
      }
    }
    return null;
  }

  private static List<JLabel> findComponents(Component component, Class<JLabel> componentType) {
    List<JLabel> matches = new ArrayList<>();
    collectComponents(component, componentType, matches);
    return matches;
  }

  private static <T> void collectComponents(
      Component component, Class<T> componentType, List<T> matches) {
    if (componentType.isInstance(component)) {
      matches.add(componentType.cast(component));
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        collectComponents(child, componentType, matches);
      }
    }
  }
}
