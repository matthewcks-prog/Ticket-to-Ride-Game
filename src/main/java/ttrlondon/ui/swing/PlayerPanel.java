package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.domain.card.CardColor;

/**
 * Displays a selected player's public state and hand summary.
 */
public final class PlayerPanel extends JPanel implements GameStateListener {
  private final JComboBox<PlayerSelection> playerSelector;
  private final JLabel headingLabel;
  private final JLabel scoreLabel;
  private final JLabel busesLabel;
  private final JPanel handSummaryPanel;
  private GameSnapshot snapshot;
  private String selectedPlayerId;
  private boolean updatingSelector;

  /**
   * Creates a panel for viewing a player's score, buses, and card counts.
   */
  public PlayerPanel() {
    super(new BorderLayout(8, 8));
    setBorder(UiSupport.titledBorder("Player"));

    playerSelector = new JComboBox<>();
    headingLabel = new JLabel();
    headingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    scoreLabel = new JLabel();
    busesLabel = new JLabel();
    handSummaryPanel = new JPanel();
    handSummaryPanel.setLayout(new BoxLayout(handSummaryPanel, BoxLayout.Y_AXIS));

    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.add(scoreLabel);
    detailsPanel.add(busesLabel);
    detailsPanel.add(Box.createVerticalStrut(8));
    detailsPanel.add(handSummaryPanel);

    JPanel headerPanel = new JPanel(new BorderLayout(6, 6));
    headerPanel.add(playerSelector, BorderLayout.NORTH);
    headerPanel.add(headingLabel, BorderLayout.CENTER);

    add(headerPanel, BorderLayout.NORTH);
    add(detailsPanel, BorderLayout.CENTER);
    playerSelector.addActionListener(event -> selectViewedPlayer());
  }

  /**
   * Updates viewed player information from the latest snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    this.snapshot = snapshot;
    if (selectedPlayerId == null || findPlayer(snapshot, selectedPlayerId) == null) {
      selectedPlayerId = UiSupport.activePlayer(snapshot).id();
    }
    rebuildPlayerSelector(snapshot);
    refreshViewedPlayer();
  }

  private void selectViewedPlayer() {
    if (updatingSelector) {
      return;
    }
    PlayerSelection selection = (PlayerSelection) playerSelector.getSelectedItem();
    if (selection == null) {
      return;
    }
    selectedPlayerId = selection.id();
    refreshViewedPlayer();
  }

  private void rebuildPlayerSelector(GameSnapshot snapshot) {
    updatingSelector = true;
    try {
      playerSelector.removeAllItems();
      for (PlayerSnapshot player : snapshot.players()) {
        playerSelector.addItem(new PlayerSelection(player.id(), player.name()));
      }
      selectComboItem(selectedPlayerId);
    } finally {
      updatingSelector = false;
    }
  }

  private void selectComboItem(String playerId) {
    for (int index = 0; index < playerSelector.getItemCount(); index++) {
      PlayerSelection selection = playerSelector.getItemAt(index);
      if (selection.id().equals(playerId)) {
        playerSelector.setSelectedIndex(index);
        return;
      }
    }
  }

  private void refreshViewedPlayer() {
    if (snapshot == null) {
      return;
    }
    PlayerSnapshot viewedPlayer = findPlayer(snapshot, selectedPlayerId);
    if (viewedPlayer == null) {
      return;
    }
    headingLabel.setText(viewedPlayer.name() + " (" + viewedPlayer.color() + ")");
    headingLabel.setForeground(UiSupport.playerColor(viewedPlayer.color()));
    scoreLabel.setText("Score: " + viewedPlayer.score());
    busesLabel.setText("Buses remaining: " + viewedPlayer.busesRemaining());
    rebuildHandSummary(viewedPlayer);
  }

  private PlayerSnapshot findPlayer(GameSnapshot snapshot, String playerId) {
    for (PlayerSnapshot player : snapshot.players()) {
      if (player.id().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  private void rebuildHandSummary(PlayerSnapshot activePlayer) {
    handSummaryPanel.removeAll();
    handSummaryPanel.add(new JLabel("Transportation cards"));
    JPanel cardGrid = new JPanel(new GridLayout(0, 2, 8, 2));
    Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
    for (CardColor cardColor : activePlayer.handCards()) {
      counts.merge(cardColor, 1, Integer::sum);
    }
    for (CardColor cardColor : CardColor.values()) {
      JLabel row = new JLabel(cardColor + ": " + counts.getOrDefault(cardColor, 0));
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      cardGrid.add(row);
    }
    handSummaryPanel.add(cardGrid);
    handSummaryPanel.revalidate();
    handSummaryPanel.repaint();
  }

  private record PlayerSelection(String id, String name) {
    @Override
    public String toString() {
      return name;
    }
  }
}
