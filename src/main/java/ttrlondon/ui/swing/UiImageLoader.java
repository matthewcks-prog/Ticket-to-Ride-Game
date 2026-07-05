package ttrlondon.ui.swing;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;

/**
 * Loads Swing image assets from the packaged classpath with an optional development-file fallback.
 */
public final class UiImageLoader {
  /** Packaged London board map resource. */
  public static final String LONDON_MAP_RESOURCE = "/ttrlondon/ui/london_map.png";
  /** Packaged startup loading screen resource. */
  public static final String LOADING_SCREEN_RESOURCE = "/ttrlondon/ui/loading_screen.png";
  /** Packaged new-game setup background resource. */
  public static final String SETUP_BACKGROUND_RESOURCE = "/ttrlondon/ui/setup_background.png";

  private UiImageLoader() {}

  /**
   * Loads an image from the classpath.
   *
   * @param resourcePath absolute classpath resource path
   * @return loaded image when present and readable
   */
  public static Optional<Image> loadClasspathImage(String resourcePath) {
    URL resource = UiImageLoader.class.getResource(resourcePath);
    if (resource == null) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(ImageIO.read(resource));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  /**
   * Loads an image from the classpath, falling back to a local file for development launches.
   *
   * @param resourcePath absolute classpath resource path
   * @param fallbackPath local fallback path
   * @return loaded image when present and readable
   */
  public static Optional<Image> loadClasspathImageWithFallback(
      String resourcePath, Path fallbackPath) {
    Optional<Image> classpathImage = loadClasspathImage(resourcePath);
    if (classpathImage.isPresent()) {
      return classpathImage;
    }
    if (!Files.isRegularFile(fallbackPath)) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(ImageIO.read(fallbackPath.toFile()));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  /** Returns the London map image using the packaged resource and repository-root fallback. */
  public static Optional<Image> loadLondonMap() {
    return loadClasspathImageWithFallback(LONDON_MAP_RESOURCE, Path.of("london_map.png"));
  }

  /** Returns the loading screen image using the packaged resource. */
  public static Optional<Image> loadLoadingScreen() {
    return loadClasspathImage(LOADING_SCREEN_RESOURCE);
  }

  /** Returns the setup screen background image using the packaged resource. */
  public static Optional<Image> loadSetupBackground() {
    return loadClasspathImage(SETUP_BACKGROUND_RESOURCE);
  }
}
