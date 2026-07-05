package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * Determinate startup loading dialog shown before the new-game setup workflow.
 */
public final class LoadingDialog extends JDialog {
  private static final int DEFAULT_MINIMUM_DISPLAY_MILLIS = 950;

  private final JLabel stepLabel;
  private final JProgressBar progressBar;
  private final int totalSteps;
  private long shownAtMillis;

  public LoadingDialog(Window owner, int totalSteps) {
    super(owner, "Ticket to Ride: London", ModalityType.MODELESS);
    this.totalSteps = totalSteps;
    stepLabel = new JLabel("Starting...");
    progressBar = new JProgressBar(0, totalSteps);
    build();
  }

  public void showLoading() {
    pack();
    setLocationRelativeTo(getOwner());
    shownAtMillis = System.currentTimeMillis();
    setVisible(true);
  }

  /**
   * Updates progress to the supplied startup step.
   *
   * @param step startup milestone
   */
  public void updateStep(StartupLoadingStep step) {
    updateStep(step.completedSteps(), step.description());
  }

  /**
   * Updates progress to the supplied step count and description.
   *
   * @param completedSteps number of completed steps
   * @param description user-facing loading message
   */
  public void updateStep(int completedSteps, String description) {
    stepLabel.setText(description);
    progressBar.setValue(completedSteps);
    progressBar.setString(completedSteps + " / " + totalSteps);
    progressBar.paintImmediately(progressBar.getBounds());
    stepLabel.paintImmediately(stepLabel.getBounds());
  }

  /**
   * Disposes the dialog after the default minimum display time, then runs a continuation.
   *
   * @param continuation action to run after the dialog closes
   */
  public void disposeAfterMinimumDisplay(Runnable continuation) {
    disposeAfterMinimumDisplay(DEFAULT_MINIMUM_DISPLAY_MILLIS, continuation);
  }

  /**
   * Disposes the dialog after a minimum display time, then runs a continuation.
   *
   * @param minimumMillis minimum visible time in milliseconds
   * @param continuation action to run after the dialog closes
   */
  public void disposeAfterMinimumDisplay(int minimumMillis, Runnable continuation) {
    long elapsedMillis = System.currentTimeMillis() - shownAtMillis;
    int remainingMillis = (int) Math.max(0, minimumMillis - elapsedMillis);
    Timer timer =
        new Timer(
            remainingMillis,
            event -> {
              dispose();
              continuation.run();
            });
    timer.setRepeats(false);
    timer.start();
  }

  private void build() {
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setMinimumSize(AppWindowStyle.MINIMUM_APP_WINDOW_SIZE);
    setPreferredSize(AppWindowStyle.APP_WINDOW_SIZE);

    Image loadingImage = UiImageLoader.loadLoadingScreen().orElse(null);
    JPanel content = new ImageBackgroundPanel(loadingImage);
    content.setLayout(new GridBagLayout());
    content.setBackground(AppWindowStyle.PANEL_BACKGROUND);

    JLabel title = new JLabel("Ticket to Ride: London");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 28.0f));
    title.setForeground(AppWindowStyle.TITLE_COLOR);

    stepLabel.setForeground(AppWindowStyle.TEXT_COLOR);
    progressBar.setStringPainted(true);

    JPanel progressPanel = new JPanel(new BorderLayout(12, 10));
    progressPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
    progressPanel.setBackground(new Color(244, 235, 215, 232));
    progressPanel.add(title, BorderLayout.NORTH);
    progressPanel.add(stepLabel, BorderLayout.CENTER);
    progressPanel.add(progressBar, BorderLayout.SOUTH);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 120, 52, 120);
    content.add(progressPanel, constraints);

    setContentPane(content);
    setResizable(false);
  }
}
