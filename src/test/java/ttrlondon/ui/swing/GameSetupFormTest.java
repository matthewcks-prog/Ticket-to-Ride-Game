package ttrlondon.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import ttrlondon.infrastructure.config.PlayerSetupConfiguration;

/** Tests new-game setup form behaviour without opening modal dialogs. */
final class GameSetupFormTest {
  @Test
  void defaultPlayerCountShowsOnlyFirstTwoRows() throws Exception {
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();

          assertEquals(2, form.mountedPlayerRowCount());
          assertTrue(form.isPlayerRowActive(0));
          assertTrue(form.isPlayerRowActive(1));
          assertFalse(form.isPlayerRowActive(2));
          assertFalse(form.isPlayerRowActive(3));
        });
  }

  @Test
  void increasingPlayerCountActivatesAdditionalRows() throws Exception {
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();
          form.applyPlayerCount(4);

          assertEquals(4, form.mountedPlayerRowCount());
          assertTrue(form.isPlayerRowActive(0));
          assertTrue(form.isPlayerRowActive(1));
          assertTrue(form.isPlayerRowActive(2));
          assertTrue(form.isPlayerRowActive(3));
        });
  }

  @Test
  void decreasingPlayerCountDeactivatesExtraRows() throws Exception {
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();
          form.applyPlayerCount(4);
          form.applyPlayerCount(2);

          assertEquals(2, form.mountedPlayerRowCount());
          assertTrue(form.isPlayerRowActive(0));
          assertTrue(form.isPlayerRowActive(1));
          assertFalse(form.isPlayerRowActive(2));
          assertFalse(form.isPlayerRowActive(3));
        });
  }

  @Test
  void playersUsesOnlyActiveRowCount() throws Exception {
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();
          form.applyPlayerCount(2);

          assertEquals(2, form.players().size());
          assertEquals(PlayerSetupConfiguration.MIN_PLAYERS, form.players().size());
        });
  }

  @Test
  void formHeightGrowsWithPlayerCount() throws Exception {
    Dimension twoPlayers = packedDialogSizeOnEdt(2);
    Dimension threePlayers = packedDialogSizeOnEdt(3);
    Dimension fourPlayers = packedDialogSizeOnEdt(4);

    assertTrue(
        threePlayers.height > twoPlayers.height,
        "three-player dialog should be taller than two-player dialog");
    assertTrue(
        fourPlayers.height > threePlayers.height,
        "four-player dialog should be taller than three-player dialog");
  }

  @Test
  void layoutChangeListenerRunsWhenPlayerCountChanges() throws Exception {
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();
          int[] notifications = {0};
          form.onLayoutChange(() -> notifications[0]++);

          form.applyPlayerCount(3);

          assertTrue(notifications[0] >= 1);
        });
  }

  private static Dimension packedDialogSizeOnEdt(int playerCount) throws Exception {
    AtomicReference<Dimension> size = new AtomicReference<>();
    runOnEdt(
        () -> {
          GameSetupForm form = new GameSetupForm();
          form.applyPlayerCount(playerCount);
          JDialog dialog = new JDialog((Frame) null, true);
          dialog.setContentPane(form.panel());
          dialog.pack();
          size.set(dialog.getSize());
          dialog.dispose();
        });
    return size.get();
  }

  private static void runOnEdt(Runnable action) throws Exception {
    SwingUtilities.invokeAndWait(action);
  }
}
