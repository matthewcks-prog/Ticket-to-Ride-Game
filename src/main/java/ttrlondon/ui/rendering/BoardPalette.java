package ttrlondon.ui.rendering;

import java.awt.Color;
import ttrlondon.domain.board.RouteColor;
import ttrlondon.domain.player.PlayerColor;

final class BoardPalette {
  private BoardPalette() {}

  static Color routeColor(RouteColor routeColor) {
    return switch (routeColor) {
      case BLUE -> new Color(34, 93, 158);
      case ORANGE -> new Color(225, 117, 36);
      case YELLOW -> new Color(239, 204, 86);
      case GREEN -> new Color(36, 118, 68);
      case PINK -> new Color(198, 61, 139);
      case BLACK -> new Color(40, 38, 36);
      case GREY -> new Color(142, 145, 140);
    };
  }

  static Color playerColor(PlayerColor playerColor) {
    return switch (playerColor) {
      case RED -> new Color(184, 42, 43);
      case WHITE -> new Color(247, 244, 235);
      case BLUE -> new Color(38, 89, 178);
      case YELLOW -> new Color(232, 187, 42);
    };
  }

  static Color districtColor(int district) {
    return switch (district) {
      case 1 -> new Color(32, 30, 29);
      case 2 -> new Color(28, 128, 73);
      case 3 -> new Color(38, 102, 177);
      case 4 -> new Color(228, 147, 44);
      case 5 -> new Color(186, 48, 46);
      default -> new Color(90, 90, 90);
    };
  }

  static Color textColorFor(Color background) {
    double luminance =
        (0.299 * background.getRed()
                + 0.587 * background.getGreen()
                + 0.114 * background.getBlue())
            / 255.0;
    return luminance > 0.58 ? Color.BLACK : Color.WHITE;
  }
}
