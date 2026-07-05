package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.ticket.DestinationTicket;
import ttrlondon.infrastructure.config.GameFactory;
import ttrlondon.infrastructure.config.GameSetupDraft;
import ttrlondon.infrastructure.random.RandomShuffleStrategy;

/**
 * Collects pre-game player configuration and initial destination ticket choices.
 */
public final class GameSetupDialog {
  private GameSetupDialog() {}

  /**
   * Runs the complete setup flow and returns a configured game.
   *
   * @param parent parent component for modal dialogs
   * @return configured game result, or null if setup is cancelled
   */
  public static SetupResult showSetup(Component parent) {
    SetupWorkflowDialog dialog = new SetupWorkflowDialog(resolveOwnerFrame(parent));
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
    return dialog.result();
  }

  private static Frame resolveOwnerFrame(Component parent) {
    if (parent instanceof Frame frame) {
      return frame;
    }
    if (parent == null) {
      return null;
    }
    Component ancestor = SwingUtilities.getWindowAncestor(parent);
    if (ancestor instanceof Frame frame) {
      return frame;
    }
    return null;
  }

  /**
   * Result of the setup flow.
   */
  public record SetupResult(ttrlondon.domain.game.Game game, Rectangle windowBounds) {}

  private static final class SetupWorkflowDialog extends JDialog {
    private static final String PLAYER_STEP = "players";
    private static final String TICKET_STEP = "tickets";

    private final AtomicReference<SetupResult> result = new AtomicReference<>();
    private final GameSetupForm setupForm = new GameSetupForm();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JLabel titleLabel = new JLabel("New Game Setup");
    private final JLabel messageLabel = new JLabel("Configure players, then choose starting tickets.");
    private final JButton backButton = new JButton("Back");
    private final JButton continueButton = new JButton("Continue");
    private final JButton startButton = new JButton("Start Game");
    private final JButton cancelButton = new JButton("Cancel");
    private GameSetupDraft draft;
    private String startingPlayerId;
    private Map<String, Map<String, JCheckBox>> ticketCheckBoxesByPlayerId = Map.of();

    SetupWorkflowDialog(Frame owner) {
      super(owner, "New Game Setup", true);
      build();
    }

    SetupResult result() {
      return result.get();
    }

    private void build() {
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setMinimumSize(AppWindowStyle.MINIMUM_APP_WINDOW_SIZE);
      setPreferredSize(AppWindowStyle.APP_WINDOW_SIZE);

      Image backgroundImage = UiImageLoader.loadSetupBackground().orElse(null);
      JPanel content = new ImageBackgroundPanel(backgroundImage);
      content.setLayout(new GridBagLayout());
      content.setBackground(AppWindowStyle.APP_BACKGROUND);

      JPanel shell = new JPanel(new BorderLayout(0, 18));
      shell.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(new java.awt.Color(190, 170, 145)),
              BorderFactory.createEmptyBorder(24, 28, 22, 28)));
      shell.setBackground(AppWindowStyle.PANEL_BACKGROUND);
      shell.add(createHeader(), BorderLayout.NORTH);

      cards.setOpaque(true);
      cards.setBackground(AppWindowStyle.PANEL_BACKGROUND);
      cards.add(wrapStep(setupForm.component()), PLAYER_STEP);
      shell.add(cards, BorderLayout.CENTER);
      shell.add(createButtonPanel(), BorderLayout.SOUTH);

      content.add(shell, shellConstraints());
      setContentPane(content);
      showPlayerStep();
      pack();
      setSize(AppWindowStyle.APP_WINDOW_SIZE);
    }

    private JComponent createHeader() {
      JPanel header = new JPanel(new BorderLayout(0, 6));
      header.setOpaque(false);
      titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26.0f));
      titleLabel.setForeground(AppWindowStyle.TITLE_COLOR);
      messageLabel.setForeground(AppWindowStyle.TEXT_COLOR);
      header.add(titleLabel, BorderLayout.NORTH);
      header.add(messageLabel, BorderLayout.SOUTH);
      return header;
    }

    private JPanel createButtonPanel() {
      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
      buttons.setOpaque(false);
      backButton.addActionListener(event -> showPlayerStep());
      continueButton.addActionListener(event -> prepareTicketStep());
      startButton.addActionListener(event -> startGame());
      cancelButton.addActionListener(event -> dispose());
      buttons.add(backButton);
      buttons.add(continueButton);
      buttons.add(startButton);
      buttons.add(cancelButton);
      return buttons;
    }

    private void showPlayerStep() {
      titleLabel.setText("New Game Setup");
      messageLabel.setText("Configure players, then choose starting destination tickets.");
      backButton.setVisible(false);
      continueButton.setVisible(true);
      startButton.setVisible(false);
      cardLayout.show(cards, PLAYER_STEP);
    }

    private void prepareTicketStep() {
      try {
        List<Player> players = setupForm.players();
        draft = GameFactory.createSetupDraft(players, new RandomShuffleStrategy());
        startingPlayerId = setupForm.startingPlayerId(players);
        cards.add(createTicketStep(), TICKET_STEP);
        titleLabel.setText("Initial Destination Tickets");
        messageLabel.setText("Choose at least one starting ticket for each player.");
        backButton.setVisible(true);
        continueButton.setVisible(false);
        startButton.setVisible(true);
        cardLayout.show(cards, TICKET_STEP);
      } catch (IllegalArgumentException exception) {
        messageLabel.setText(exception.getMessage());
      }
    }

    private JComponent createTicketStep() {
      JPanel ticketPanel = new JPanel(new GridBagLayout());
      ticketPanel.setOpaque(true);
      ticketPanel.setBackground(AppWindowStyle.PANEL_BACKGROUND);
      Map<String, Map<String, JCheckBox>> checkBoxes = new LinkedHashMap<>();
      int row = 0;
      for (Player player : draft.players()) {
        List<DestinationTicket> tickets = draft.initialTicketOptionsByPlayerId().get(player.id());
        JPanel playerTickets = createPlayerTicketPanel(player, tickets);
        checkBoxes.put(player.id(), collectCheckBoxes(playerTickets));
        ticketPanel.add(playerTickets, ticketConstraints(row++));
      }
      ticketCheckBoxesByPlayerId = checkBoxes;
      JScrollPane scrollPane = new JScrollPane(ticketPanel);
      scrollPane.setPreferredSize(new java.awt.Dimension(860, 440));
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      scrollPane.setOpaque(true);
      scrollPane.setBackground(AppWindowStyle.PANEL_BACKGROUND);
      scrollPane.getViewport().setOpaque(true);
      scrollPane.getViewport().setBackground(AppWindowStyle.PANEL_BACKGROUND);
      return scrollPane;
    }

    private JPanel createPlayerTicketPanel(Player player, List<DestinationTicket> tickets) {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBackground(new java.awt.Color(252, 238, 194, 228));
      panel.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(new java.awt.Color(142, 84, 44)),
              BorderFactory.createEmptyBorder(10, 12, 10, 12)));

      JLabel playerLabel = new JLabel(player.name() + " (" + player.color() + ")");
      playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 16.0f));
      panel.add(playerLabel, ticketOptionConstraints(0, 0, 1));
      for (int index = 0; index < tickets.size(); index++) {
        DestinationTicket ticket = tickets.get(index);
        JCheckBox checkBox =
            new JCheckBox(
                ticket.points()
                    + " points: "
                    + ticket.locationA().displayName()
                    + " to "
                    + ticket.locationB().displayName(),
                true);
        checkBox.setName(ticket.id());
        checkBox.setOpaque(false);
        panel.add(checkBox, ticketOptionConstraints(0, index + 1, 1));
      }
      return panel;
    }

    private Map<String, JCheckBox> collectCheckBoxes(JPanel playerTickets) {
      Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
      for (Component component : playerTickets.getComponents()) {
        if (component instanceof JCheckBox checkBox) {
          checkBoxes.put(checkBox.getName(), checkBox);
        }
      }
      return checkBoxes;
    }

    private void startGame() {
      Map<String, List<String>> keptTicketsByPlayerId = new LinkedHashMap<>();
      for (Map.Entry<String, Map<String, JCheckBox>> playerEntry :
          ticketCheckBoxesByPlayerId.entrySet()) {
        List<String> keptTicketIds =
            playerEntry.getValue().entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
        if (keptTicketIds.isEmpty()) {
          messageLabel.setText("Each player must keep at least one destination ticket.");
          return;
        }
        keptTicketsByPlayerId.put(playerEntry.getKey(), keptTicketIds);
      }
      result.set(
          new SetupResult(
              GameFactory.createNewGame(draft, startingPlayerId, keptTicketsByPlayerId),
              getBounds()));
      dispose();
    }

    private JComponent wrapStep(JComponent component) {
      JPanel wrapper = new JPanel(new GridBagLayout());
      wrapper.setOpaque(true);
      wrapper.setBackground(AppWindowStyle.PANEL_BACKGROUND);
      wrapper.add(component, centeredConstraints());
      return wrapper;
    }

    private GridBagConstraints shellConstraints() {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.insets = new Insets(70, 120, 70, 120);
      return constraints;
    }

    private GridBagConstraints centeredConstraints() {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.anchor = GridBagConstraints.CENTER;
      return constraints;
    }

    private GridBagConstraints ticketConstraints(int row) {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = row;
      constraints.weightx = 1.0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 12, 0);
      return constraints;
    }

    private GridBagConstraints ticketOptionConstraints(int x, int y, int width) {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = x;
      constraints.gridy = y;
      constraints.gridwidth = width;
      constraints.weightx = 1.0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.insets = new Insets(3, 3, 3, 3);
      return constraints;
    }
  }
}
