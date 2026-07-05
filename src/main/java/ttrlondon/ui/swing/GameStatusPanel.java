package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.service.GameStateListener;

/**
 * Displays current turn, active player, phase, final-round status, and status message.
 */
public final class GameStatusPanel extends JPanel implements GameStateListener {
  private final JLabel activePlayerLabel;
  private final JLabel phaseLabel;
  private final JLabel finalRoundLabel;
  private final JLabel messageLabel;

  /**
   * Creates a status panel for top-level game state.
   */
  public GameStatusPanel() {
    super(new BorderLayout(12, 4));
    setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));

    activePlayerLabel = new JLabel();
    activePlayerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    phaseLabel = new JLabel();
    finalRoundLabel = new JLabel();
    messageLabel = new JLabel("Ready.");

    JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
    summaryPanel.add(activePlayerLabel);
    summaryPanel.add(phaseLabel);
    summaryPanel.add(finalRoundLabel);
    add(summaryPanel, BorderLayout.WEST);
    add(messageLabel, BorderLayout.EAST);
  }

  /**
   * Updates the status display from the latest snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    Color playerColor = UiSupport.playerColor(activePlayer.color());
    activePlayerLabel.setText("Turn: " + activePlayer.name());
    activePlayerLabel.setForeground(playerColor);
    phaseLabel.setText("Phase: " + snapshot.phase());
    finalRoundLabel.setText(finalRoundText(snapshot));
  }

  /**
   * Displays the latest command result for player feedback.
   *
   * @param result command outcome
   */
  public void showMessage(CommandResult result) {
    messageLabel.setText(result.message());
    messageLabel.setForeground(result.isSuccess() ? new Color(34, 107, 60) : new Color(168, 40, 40));
  }

  private String finalRoundText(GameSnapshot snapshot) {
    if (!snapshot.finalRoundActive()) {
      return "Final round: inactive";
    }
    String triggerName =
        snapshot
            .triggeringPlayerId()
            .flatMap(
                playerId ->
                    snapshot.players().stream()
                        .filter(player -> player.id().equals(playerId))
                        .findFirst()
                        .map(PlayerSnapshot::name))
            .orElse("unknown");
    return "Final round: "
        + snapshot.finalTurnsRemaining()
        + " turns left, triggered by "
        + triggerName;
  }
}
