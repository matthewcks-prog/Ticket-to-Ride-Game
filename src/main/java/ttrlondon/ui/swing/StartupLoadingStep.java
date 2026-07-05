package ttrlondon.ui.swing;

/**
 * Deterministic startup milestones shown by the loading dialog.
 */
public enum StartupLoadingStep {
  LOAD_MAP_RESOURCES(1, "Loading map resources..."),
  PREPARE_SETUP(2, "Preparing setup workflow..."),
  PREPARE_RENDERING(3, "Preparing board rendering..."),
  OPEN_SETUP(4, "Opening setup...");

  private final int completedSteps;
  private final String description;

  StartupLoadingStep(int completedSteps, String description) {
    this.completedSteps = completedSteps;
    this.description = description;
  }

  /** Returns the completed step count represented by this startup milestone. */
  public int completedSteps() {
    return completedSteps;
  }

  /** Returns the user-facing loading message. */
  public String description() {
    return description;
  }

  /** Returns the total number of startup milestones. */
  public static int totalSteps() {
    return values().length;
  }
}
