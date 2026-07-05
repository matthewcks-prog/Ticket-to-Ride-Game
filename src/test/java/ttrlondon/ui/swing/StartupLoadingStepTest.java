package ttrlondon.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

final class StartupLoadingStepTest {
  @Test
  void exposesStableDeterminateStartupProgress() {
    assertEquals(4, StartupLoadingStep.totalSteps());
    assertEquals(1, StartupLoadingStep.LOAD_MAP_RESOURCES.completedSteps());
    assertEquals(4, StartupLoadingStep.OPEN_SETUP.completedSteps());
    assertFalse(StartupLoadingStep.OPEN_SETUP.description().isBlank());
  }
}
