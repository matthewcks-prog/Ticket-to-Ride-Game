package ttrlondon.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.infrastructure.config.PlayerSetupConfiguration;
import ttrlondon.infrastructure.config.PlayerSetupEntry;

/**
 * Pre-game player configuration panel used by {@link GameSetupDialog}.
 *
 * <p>Only the first {@code N} player rows are active, where {@code N} is the selected player
 * count. The selected count is controlled by explicit 2/3/4 player buttons rather than a numeric
 * spinner, keeping setup choices obvious and avoiding tiny arrow controls.
 */
final class GameSetupForm implements FormConfirmDialog.ResizableForm {
  private final JPanel panel;
  private final JPanel rowsPanel;
  private final List<JToggleButton> playerCountButtons;
  private final List<PlayerSetupRow> playerRows;
  private final JComboBox<String> firstPlayerField;
  private final List<Runnable> layoutChangeListeners = new ArrayList<>();
  private int playerCount = PlayerSetupConfiguration.MIN_PLAYERS;

  GameSetupForm() {
    panel = new JPanel(new GridBagLayout());
    rowsPanel = new JPanel(new GridBagLayout());
    playerCountButtons = new ArrayList<>();
    playerRows = new ArrayList<>();
    firstPlayerField = new JComboBox<>();
    build();
  }

  @Override
  public JPanel component() {
    rebuildFirstPlayerOptions();
    return panel;
  }

  @Override
  public void onLayoutChange(Runnable repackDialog) {
    layoutChangeListeners.add(repackDialog);
  }

  JPanel panel() {
    return component();
  }

  List<Player> players() {
    List<PlayerSetupEntry> entries = new ArrayList<>(playerCount);
    for (int index = 0; index < playerCount; index++) {
      PlayerSetupRow row = playerRows.get(index);
      entries.add(new PlayerSetupEntry(row.name(), row.color()));
    }
    return PlayerSetupConfiguration.createPlayers(entries);
  }

  String startingPlayerId(List<Player> players) {
    int selectedIndex = firstPlayerField.getSelectedIndex();
    if (selectedIndex <= 0) {
      List<Player> shuffled = new ArrayList<>(players);
      Collections.shuffle(shuffled);
      return shuffled.get(0).id();
    }
    int playerIndex = Math.min(selectedIndex - 1, players.size() - 1);
    return players.get(playerIndex).id();
  }

  /** Returns whether the player row at the supplied index is mounted and editable. */
  boolean isPlayerRowActive(int index) {
    return index >= 0 && index < playerCount && index < playerRows.size();
  }

  /** Returns the number of player rows currently mounted in the form layout. */
  int mountedPlayerRowCount() {
    return rowsPanel.getComponentCount();
  }

  /** Applies a player count and synchronises row visibility. Intended for tests. */
  void applyPlayerCount(int playerCount) {
    if (playerCount < PlayerSetupConfiguration.MIN_PLAYERS
        || playerCount > PlayerSetupConfiguration.MAX_PLAYERS) {
      throw new IllegalArgumentException("playerCount out of range: " + playerCount);
    }
    this.playerCount = playerCount;
    selectPlayerCountButton(playerCount);
    syncActivePlayerRows();
    rebuildFirstPlayerOptions();
  }

  private void build() {
    panel.setOpaque(true);
    panel.setBackground(new Color(244, 235, 215));
    panel.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

    JLabel heading = new JLabel("Players");
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18.0f));
    heading.setForeground(AppWindowStyle.TITLE_COLOR);
    panel.add(heading, formConstraints(0, 0, 1, GridBagConstraints.WEST));
    panel.add(createPlayerCountSelector(), formConstraints(1, 0, 2, GridBagConstraints.WEST));

    rowsPanel.setOpaque(false);
    PlayerColor[] colors = PlayerColor.values();
    for (int index = 0; index < PlayerSetupConfiguration.MAX_PLAYERS; index++) {
      playerRows.add(new PlayerSetupRow(index + 1, colors, index));
    }
    panel.add(rowsPanel, formConstraints(0, 1, 3, GridBagConstraints.CENTER));

    JLabel firstPlayerLabel = new JLabel("First player");
    firstPlayerLabel.setFont(firstPlayerLabel.getFont().deriveFont(Font.BOLD));
    panel.add(firstPlayerLabel, formConstraints(0, 2, 1, GridBagConstraints.WEST));
    firstPlayerField.setPreferredSize(new Dimension(190, 30));
    panel.add(firstPlayerField, formConstraints(1, 2, 2, GridBagConstraints.WEST));

    syncActivePlayerRows();
  }

  private JComponent createPlayerCountSelector() {
    JPanel selector = new JPanel(new GridBagLayout());
    selector.setOpaque(false);
    ButtonGroup buttonGroup = new ButtonGroup();
    for (int count = PlayerSetupConfiguration.MIN_PLAYERS;
        count <= PlayerSetupConfiguration.MAX_PLAYERS;
        count++) {
      JToggleButton button = new JToggleButton(count + " Players");
      button.setFocusPainted(false);
      button.setPreferredSize(new Dimension(104, 34));
      int selectedCount = count;
      button.addActionListener(event -> applyPlayerCount(selectedCount));
      buttonGroup.add(button);
      playerCountButtons.add(button);
      selector.add(button, selectorConstraints(count - PlayerSetupConfiguration.MIN_PLAYERS));
    }
    selectPlayerCountButton(playerCount);
    return selector;
  }

  private void syncActivePlayerRows() {
    rowsPanel.removeAll();
    for (int index = 0; index < playerCount; index++) {
      PlayerSetupRow row = playerRows.get(index);
      row.setEditable(true);
      rowsPanel.add(row.panel(), playerRowConstraints(index));
    }
    for (int index = playerCount; index < playerRows.size(); index++) {
      playerRows.get(index).setEditable(false);
    }
    refreshLayout();
    notifyLayoutChanged();
  }

  private void refreshLayout() {
    rowsPanel.revalidate();
    rowsPanel.repaint();
    panel.revalidate();
    panel.repaint();
  }

  private void rebuildFirstPlayerOptions() {
    Object selected = firstPlayerField.getSelectedItem();
    firstPlayerField.removeAllItems();
    firstPlayerField.addItem("Random");
    for (int index = 0; index < playerCount; index++) {
      firstPlayerField.addItem(playerRows.get(index).name());
    }
    if (selected != null) {
      firstPlayerField.setSelectedItem(selected);
    }
  }

  private void selectPlayerCountButton(int selectedCount) {
    for (int index = 0; index < playerCountButtons.size(); index++) {
      int count = index + PlayerSetupConfiguration.MIN_PLAYERS;
      playerCountButtons.get(index).setSelected(count == selectedCount);
    }
  }

  private void notifyLayoutChanged() {
    for (Runnable listener : layoutChangeListeners) {
      listener.run();
    }
  }

  private static GridBagConstraints formConstraints(int x, int y, int width, int anchor) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = width;
    constraints.weightx = x == 0 ? 0.0 : 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = anchor;
    constraints.insets = new Insets(7, 7, 7, 7);
    return constraints;
  }

  private static GridBagConstraints selectorConstraints(int x) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = 0;
    constraints.insets = new Insets(0, 0, 0, 8);
    return constraints;
  }

  private static GridBagConstraints playerRowConstraints(int y) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = y;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(4, 0, 4, 0);
    return constraints;
  }

  /** One editable player row in the setup form. */
  private static final class PlayerSetupRow {
    private final JPanel rowPanel;
    private final JTextField nameField;
    private final JComboBox<PlayerColor> colorField;

    private PlayerSetupRow(int playerNumber, PlayerColor[] colors, int defaultColorIndex) {
      JLabel label = new JLabel("Player " + playerNumber);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      nameField = new JTextField("Player " + playerNumber, 18);
      colorField = new JComboBox<>(colors);
      colorField.setSelectedIndex(defaultColorIndex);
      colorField.setPreferredSize(new Dimension(150, 30));

      rowPanel = new JPanel(new GridBagLayout());
      rowPanel.setOpaque(false);
      rowPanel.add(label, rowConstraints(0, 0.0));
      rowPanel.add(nameField, rowConstraints(1, 1.0));
      rowPanel.add(colorField, rowConstraints(2, 0.0));
    }

    private JPanel panel() {
      return rowPanel;
    }

    private void setEditable(boolean editable) {
      nameField.setEnabled(editable);
      colorField.setEnabled(editable);
    }

    private String name() {
      return nameField.getText();
    }

    private PlayerColor color() {
      return (PlayerColor) colorField.getSelectedItem();
    }

    private static GridBagConstraints rowConstraints(int x, double weightx) {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = x;
      constraints.gridy = 0;
      constraints.weightx = weightx;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(2, 6, 2, 6);
      return constraints;
    }
  }
}
