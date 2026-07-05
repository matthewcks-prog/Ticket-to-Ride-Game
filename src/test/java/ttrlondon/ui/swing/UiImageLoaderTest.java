package ttrlondon.ui.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class UiImageLoaderTest {
  @Test
  void londonMapIsPackagedAsClasspathResource() {
    assertNotNull(UiImageLoader.class.getResource(UiImageLoader.LONDON_MAP_RESOURCE));
    assertTrue(UiImageLoader.loadClasspathImage(UiImageLoader.LONDON_MAP_RESOURCE).isPresent());
  }

  @Test
  void loadingScreenIsPackagedAsClasspathResource() {
    assertNotNull(UiImageLoader.class.getResource(UiImageLoader.LOADING_SCREEN_RESOURCE));
    assertTrue(UiImageLoader.loadClasspathImage(UiImageLoader.LOADING_SCREEN_RESOURCE).isPresent());
  }

  @Test
  void setupBackgroundIsPackagedAsClasspathResource() {
    assertNotNull(UiImageLoader.class.getResource(UiImageLoader.SETUP_BACKGROUND_RESOURCE));
    assertTrue(UiImageLoader.loadClasspathImage(UiImageLoader.SETUP_BACKGROUND_RESOURCE).isPresent());
  }
}
