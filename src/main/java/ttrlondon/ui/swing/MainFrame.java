package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import ttrlondon.application.commands.DrawTransportCardCommand;
import ttrlondon.application.commands.GameCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.FinalScoreSnapshot;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.domain.game.GamePhase;
import ttrlondon.ui.rendering.BoardRenderer;
import ttrlondon.ui.rendering.LocationRenderer;
import ttrlondon.ui.rendering.RouteRenderer;

/**
 * Main Swing window that composes the UI panels and wires them to application snapshots.
 */
public final class MainFrame extends JFrame implements GameStateListener {
  private final BoardPanel boardPanel;
  private final PlayerPanel playerPanel;
  private final CardMarketPanel cardMarketPanel;
  private final DestinationTicketPanel destinationTicketPanel;
  private final RushHourPanel rushHourPanel;
  private final ActionPanel actionPanel;
  private final GameStatusPanel gameStatusPanel;
  private final GameApplicationService applicationService;
  private boolean finalScorePresented;

  /**
   * Creates the main frame and registers all panels as game-state listeners.
   *
   * @param applicationService application entry point used for snapshot publication
   */
  public MainFrame(GameApplicationService applicationService) {
    this(applicationService, null);
  }

  /**
   * Creates the main frame at the supplied initial bounds and registers all panels as game-state
   * listeners.
   *
   * @param applicationService application entry point used for snapshot publication
   * @param initialBounds bounds inherited from the setup window, or null for platform placement
   */
  public MainFrame(GameApplicationService applicationService, Rectangle initialBounds) {
    super("Ticket to Ride: London");
    this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
    this.finalScorePresented = false;

    boardPanel =
        new BoardPanel(
            new BoardRenderer(new RouteRenderer(), new LocationRenderer(), loadBackgroundImage()),
            this::selectRoute);
    playerPanel = new PlayerPanel();
    cardMarketPanel =
        new CardMarketPanel(
            (playerId, index) -> executeCommand(DrawTransportCardCommand.faceUp(playerId, index)),
            playerId -> executeCommand(DrawTransportCardCommand.blind(playerId)));
    destinationTicketPanel = new DestinationTicketPanel();
    rushHourPanel = new RushHourPanel();
    actionPanel = new ActionPanel(applicationService, this::showCommandResult);
    gameStatusPanel = new GameStatusPanel();

    configureFrame();
    setContentPane(createContentPane());
    registerListeners(applicationService);
    publishInitialSnapshot(applicationService.getSnapshot());
    pack();
    applyInitialBounds(initialBounds);
  }

  private void configureFrame() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setMinimumSize(AppWindowStyle.MINIMUM_APP_WINDOW_SIZE);
    setPreferredSize(AppWindowStyle.APP_WINDOW_SIZE);
    setLocationByPlatform(true);
  }

  private void applyInitialBounds(Rectangle initialBounds) {
    if (initialBounds == null) {
      return;
    }
    setLocationByPlatform(false);
    setBounds(initialBounds);
  }

  private JPanel createContentPane() {
    JPanel contentPane = new JPanel(new BorderLayout(12, 12));
    contentPane.setBackground(AppWindowStyle.APP_BACKGROUND);
    contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    contentPane.add(gameStatusPanel, BorderLayout.NORTH);
    contentPane.add(createMainSplitPane(), BorderLayout.CENTER);
    contentPane.add(cardMarketPanel, BorderLayout.SOUTH);
    return contentPane;
  }

  private JSplitPane createMainSplitPane() {
    JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardPanel, createSidePanel());
    splitPane.setResizeWeight(0.68);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setContinuousLayout(true);
    return splitPane;
  }

  private JPanel createSidePanel() {
    JPanel sidePanel = new JPanel(new GridBagLayout());
    JPanel statusPanel = new JPanel(new GridBagLayout());
    statusPanel.add(playerPanel, horizontalStatusConstraints(0));
    statusPanel.add(rushHourPanel, horizontalStatusConstraints(1));
    sidePanel.add(statusPanel, sidePanelConstraints(0, 0.0, GridBagConstraints.HORIZONTAL));
    sidePanel.add(destinationTicketPanel, sidePanelConstraints(1, 1.0, GridBagConstraints.BOTH));
    sidePanel.add(actionPanel, sidePanelConstraints(2, 0.0, GridBagConstraints.HORIZONTAL));
    sidePanel.setPreferredSize(new Dimension(330, 640));
    return sidePanel;
  }

  private GridBagConstraints horizontalStatusConstraints(int x) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    return constraints;
  }

  private GridBagConstraints sidePanelConstraints(int y, double weighty, int fill) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = y;
    constraints.weightx = 1.0;
    constraints.weighty = weighty;
    constraints.fill = fill;
    constraints.anchor = GridBagConstraints.NORTH;
    return constraints;
  }

  private void registerListeners(GameApplicationService applicationService) {
    for (GameStateListener listener : listeners()) {
      applicationService.addListener(listener);
    }
  }

  private void publishInitialSnapshot(GameSnapshot snapshot) {
    for (GameStateListener listener : listeners()) {
      listener.onGameStateChanged(snapshot);
    }
  }

  private void executeCommand(GameCommand command) {
    showCommandResult(applicationService.executeCommand(command));
  }

  private void showCommandResult(CommandResult result) {
    gameStatusPanel.showMessage(result);
  }

  private void selectRoute(String routeId) {
    actionPanel.selectRoute(routeId);
    gameStatusPanel.showMessage(CommandResult.success("Selected route " + routeId + "."));
  }

  private static Image loadBackgroundImage() {
    return UiImageLoader.loadLondonMap().orElse(null);
  }

  private List<GameStateListener> listeners() {
    return List.of(
        boardPanel,
        playerPanel,
        rushHourPanel,
        cardMarketPanel,
        destinationTicketPanel,
        actionPanel,
        gameStatusPanel,
        this);
  }

  /**
   * Presents final scoring once the domain reaches the scoring phase.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    if (snapshot.phase() != GamePhase.SCORING) {
      finalScorePresented = false;
      return;
    }
    if (finalScorePresented) {
      return;
    }
    finalScorePresented = true;
    FinalScoreSnapshot finalScore = applicationService.finalScoreSnapshot();
    new FinalScoreDialog(this, finalScore).setVisible(true);
    showCommandResult(applicationService.completeScoring());
  }
}
