package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import ttrlondon.application.dto.FinalScoreSnapshot;
import ttrlondon.application.dto.FinalScoreSnapshot.PlayerFinalScoreSnapshot;
import ttrlondon.application.dto.FinalScoreSnapshot.TicketScoreSnapshot;

/**
 * Modal presentation of final scoring and winner details.
 */
public final class FinalScoreDialog extends JDialog {
  /**
   * Creates a final scoring dialog.
   *
   * @param owner parent frame
   * @param finalScore final scoring read model
   */
  public FinalScoreDialog(MainFrame owner, FinalScoreSnapshot finalScore) {
    super(owner, "Final Scoring", true);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setContentPane(createContent(finalScore));
    setMinimumSize(new Dimension(640, 520));
    setLocationRelativeTo(owner);
    pack();
  }

  private JPanel createContent(FinalScoreSnapshot finalScore) {
    JPanel content = new JPanel(new BorderLayout(12, 12));
    content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    JLabel heading = new JLabel(winnerText(finalScore), SwingConstants.LEFT);
    heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    content.add(heading, BorderLayout.NORTH);

    JPanel scorePanel = new JPanel();
    scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
    for (PlayerFinalScoreSnapshot playerScore : finalScore.playerScores()) {
      scorePanel.add(createPlayerScorePanel(playerScore));
      scorePanel.add(Box.createVerticalStrut(12));
    }
    JScrollPane scrollPane = new JScrollPane(scorePanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    content.add(scrollPane, BorderLayout.CENTER);

    JLabel tieBreaker =
        new JLabel("Tie-breakers: completed tickets, then longest continuous path.");
    tieBreaker.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    content.add(tieBreaker, BorderLayout.SOUTH);
    return content;
  }

  private JPanel createPlayerScorePanel(PlayerFinalScoreSnapshot playerScore) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiSupport.playerColor(playerScore.playerColor()), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    JLabel heading =
        new JLabel(playerScore.playerName() + " - " + playerScore.totalScore() + " points");
    heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    heading.setForeground(UiSupport.playerColor(playerScore.playerColor()));
    panel.add(heading);
    panel.add(
        new JLabel(
            "Routes: "
                + playerScore.routePoints()
                + " | Tickets: "
                + signed(playerScore.ticketPoints())
                + " | Longest path: "
                + playerScore.longestPathLength()
                + " (bonus "
                + playerScore.longestPathBonus()
                + ")"));
    panel.add(new JLabel("Completed tickets: " + playerScore.completedTicketCount()));
    panel.add(Box.createVerticalStrut(6));
    for (TicketScoreSnapshot ticketScore : playerScore.ticketScores()) {
      panel.add(
          new JLabel(
              ticketScore.locationADisplayName()
                  + " to "
                  + ticketScore.locationBDisplayName()
                  + ": "
                  + (ticketScore.completed() ? "complete " : "incomplete ")
                  + signed(ticketScore.scoreContribution())));
    }
    return panel;
  }

  private String winnerText(FinalScoreSnapshot finalScore) {
    String names =
        finalScore.playerScores().stream()
            .filter(score -> finalScore.winnerIds().contains(score.playerId()))
            .map(PlayerFinalScoreSnapshot::playerName)
            .reduce((left, right) -> left + ", " + right)
            .orElse("No winner");
    if (finalScore.winnerIds().size() > 1) {
      return "Shared win: " + names;
    }
    return "Winner: " + names;
  }

  private String signed(int value) {
    return value >= 0 ? "+" + value : String.valueOf(value);
  }
}
