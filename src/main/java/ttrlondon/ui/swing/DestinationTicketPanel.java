package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ttrlondon.application.dto.DestinationTicketSnapshot;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.service.GameStateListener;

/**
 * Displays the active player's destination tickets.
 */
public final class DestinationTicketPanel extends JPanel implements GameStateListener {
  private final JPanel ticketListPanel;

  /**
   * Creates a destination ticket panel with printed-ticket styling.
   */
  public DestinationTicketPanel() {
    super(new BorderLayout());
    setBorder(UiSupport.titledBorder("Destination Tickets"));

    ticketListPanel = new JPanel();
    ticketListPanel.setLayout(new BoxLayout(ticketListPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(ticketListPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setPreferredSize(new Dimension(300, 300));
    add(scrollPane, BorderLayout.CENTER);
  }

  /**
   * Updates the active player's destination tickets from the latest snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    ticketListPanel.removeAll();
    for (DestinationTicketSnapshot ticket : activePlayer.destinationTickets()) {
      ticketListPanel.add(createTicketCard(ticket));
      ticketListPanel.add(Box.createVerticalStrut(8));
    }
    ticketListPanel.revalidate();
    ticketListPanel.repaint();
  }

  private JPanel createTicketCard(DestinationTicketSnapshot ticket) {
    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(new Color(252, 238, 194));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(142, 84, 44), 1),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

    JLabel pointsLabel = new JLabel(String.valueOf(ticket.points()));
    pointsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
    pointsLabel.setForeground(new Color(118, 38, 28));

    JLabel locationsLabel =
        new JLabel(
            "<html><b>"
                + ticket.locationADisplayName()
                + "</b><br>"
                + ticket.locationBDisplayName()
                + "</html>");
    locationsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    locationsLabel.setForeground(new Color(54, 43, 32));

    GridBagConstraints pointsConstraints = new GridBagConstraints();
    pointsConstraints.gridx = 0;
    pointsConstraints.gridy = 0;
    pointsConstraints.insets = new Insets(0, 0, 0, 12);
    card.add(pointsLabel, pointsConstraints);

    GridBagConstraints locationsConstraints = new GridBagConstraints();
    locationsConstraints.gridx = 1;
    locationsConstraints.gridy = 0;
    locationsConstraints.weightx = 1.0;
    locationsConstraints.fill = GridBagConstraints.HORIZONTAL;
    card.add(locationsLabel, locationsConstraints);
    return card;
  }
}
