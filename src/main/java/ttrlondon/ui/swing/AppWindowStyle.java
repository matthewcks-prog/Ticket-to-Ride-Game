package ttrlondon.ui.swing;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Shared window sizing and colour constants for the Swing client shell.
 */
final class AppWindowStyle {
  static final Dimension APP_WINDOW_SIZE = new Dimension(1280, 820);
  static final Dimension MINIMUM_APP_WINDOW_SIZE = new Dimension(1100, 720);
  static final Color APP_BACKGROUND = new Color(238, 238, 238);
  static final Color PANEL_BACKGROUND = new Color(244, 235, 215);
  static final Color TITLE_COLOR = new Color(92, 48, 34);
  static final Color TEXT_COLOR = new Color(44, 36, 30);

  private AppWindowStyle() {}
}
