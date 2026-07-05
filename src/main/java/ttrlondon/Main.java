package ttrlondon;

import java.util.Arrays;
import javax.swing.SwingUtilities;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.domain.game.Game;
import ttrlondon.ui.swing.GameSetupDialog;
import ttrlondon.ui.swing.GameSetupDialog.SetupResult;
import ttrlondon.ui.swing.LoadingDialog;
import ttrlondon.ui.swing.MainFrame;
import ttrlondon.ui.swing.StartupLoadingStep;
import ttrlondon.ui.swing.UiImageLoader;

/**
 * Application entry point for the Ticket to Ride: London Swing client.
 */
public final class Main {
  private static final String DEMO_MODE_ARG = "--demo";

  private Main() {}

  /**
   * Opens the setup workflow and launches the Swing client when setup is confirmed.
   *
   * @param args command-line arguments; pass {@code --demo} to open the deterministic demo state
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> createAndShowApplication(args));
  }

  private static void createAndShowApplication(String[] args) {
    if (isDemoMode(args)) {
      showDemoApplication();
      return;
    }
    LoadingDialog loadingDialog = new LoadingDialog(null, StartupLoadingStep.totalSteps());
    loadingDialog.showLoading();
    loadingDialog.updateStep(StartupLoadingStep.LOAD_MAP_RESOURCES);
    UiImageLoader.loadLondonMap();
    loadingDialog.updateStep(StartupLoadingStep.PREPARE_SETUP);
    loadingDialog.updateStep(StartupLoadingStep.PREPARE_RENDERING);
    loadingDialog.updateStep(StartupLoadingStep.OPEN_SETUP);
    loadingDialog.disposeAfterMinimumDisplay(2200, Main::showSetupWorkflow);
  }

  private static boolean isDemoMode(String[] args) {
    return args != null && Arrays.asList(args).contains(DEMO_MODE_ARG);
  }

  private static void showDemoApplication() {
    GameApplicationService applicationService =
        DemoApplicationFactory.createDemoApplicationService();
    MainFrame frame = new MainFrame(applicationService);
    frame.setVisible(true);
  }

  private static void showSetupWorkflow() {
    SetupResult setupResult = GameSetupDialog.showSetup(null);
    if (setupResult == null) {
      return;
    }
    Game game = setupResult.game();
    GameApplicationService applicationService = new GameApplicationService(game);
    MainFrame frame = new MainFrame(applicationService, setupResult.windowBounds());
    frame.setVisible(true);
  }
}
