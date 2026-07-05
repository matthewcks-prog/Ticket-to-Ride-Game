package ttrlondon.ui.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import ttrlondon.ui.viewmodel.ScoreMarkerViewModel;

/**
 * Draws player score markers on top of the board-edge score track.
 */
public final class ScoreTrackRenderer {
  /**
   * Draws score markers for all players.
   *
   * @param graphics graphics context
   * @param boardBounds rendered board bounds
   * @param markers marker view models
   */
  public void drawScoreMarkers(
      Graphics2D graphics, Rectangle boardBounds, List<ScoreMarkerViewModel> markers) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int diameter = Math.max(18, Math.round(boardBounds.height / 34.0f));
      int stagger = Math.max(5, diameter / 5);
      for (int index = 0; index < markers.size(); index++) {
        drawMarker(g2, boardBounds, markers.get(index), index, diameter, stagger);
      }
    } finally {
      g2.dispose();
    }
  }

  private void drawMarker(
      Graphics2D g2,
      Rectangle boardBounds,
      ScoreMarkerViewModel marker,
      int markerIndex,
      int diameter,
      int stagger) {
    int centerX = (int) Math.round(boardBounds.x + marker.position().x() * boardBounds.width);
    int centerY = (int) Math.round(boardBounds.y + marker.position().y() * boardBounds.height);
    int offset = (markerIndex - 1) * stagger;
    int x = centerX - diameter / 2 + offset;
    int y = centerY - diameter / 2 + offset;

    Color fill = BoardPalette.playerColor(marker.playerColor());
    g2.setColor(new Color(255, 255, 255, 230));
    g2.fillOval(x - 3, y - 3, diameter + 6, diameter + 6);
    g2.setColor(new Color(44, 36, 30));
    g2.setStroke(new BasicStroke(Math.max(2.0f, diameter / 10.0f)));
    g2.drawOval(x - 3, y - 3, diameter + 6, diameter + 6);
    g2.setColor(fill);
    g2.fillOval(x, y, diameter, diameter);
    g2.setColor(BoardPalette.textColorFor(fill));
    FontMetrics metrics = g2.getFontMetrics();
    String label = Integer.toString(marker.score());
    int textX = x + (diameter - metrics.stringWidth(label)) / 2;
    int textY = y + ((diameter - metrics.getHeight()) / 2) + metrics.getAscent();
    g2.drawString(label, textX, textY);
  }
}
