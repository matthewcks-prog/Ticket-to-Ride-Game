package ttrlondon.ui.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import ttrlondon.ui.viewmodel.BoardLocationViewModel;
import ttrlondon.ui.viewmodel.NormalizedOffset;
import ttrlondon.ui.viewmodel.NormalizedPoint;

/**
 * Draws London board station markers and labels.
 */
public final class LocationRenderer {
  private static final Color LABEL_COLOR = new Color(88, 38, 29);
  private static final Color CENTRAL_LABEL_COLOR = new Color(36, 32, 29);
  private static final Color OUTER_RING_COLOR = new Color(250, 247, 238);
  private static final Color OUTLINE_COLOR = new Color(74, 57, 44);

  /**
   * Draws a location marker and its text label.
   *
   * @param graphics graphics context
   * @param location location to draw
   * @param boardBounds aspect-correct board drawing bounds
   */
  public void drawLocation(
      Graphics2D graphics, BoardLocationViewModel location, Rectangle boardBounds) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int markerSize = markerSize(boardBounds);
      NormalizedPoint position = location.position();
      int centerX = toPixelX(position, boardBounds);
      int centerY = toPixelY(position, boardBounds);
      int radius = markerSize / 2;

      drawMarker(g2, location, centerX, centerY, radius);
      drawLabel(g2, location, centerX, centerY, markerSize, boardBounds);
    } finally {
      g2.dispose();
    }
  }

  private void drawMarker(
      Graphics2D g2, BoardLocationViewModel location, int centerX, int centerY, int radius) {
    Color districtColor = BoardPalette.districtColor(location.district());
    Ellipse2D outer =
        new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0);
    Ellipse2D inner =
        new Ellipse2D.Double(
            centerX - radius * 0.68,
            centerY - radius * 0.68,
            radius * 1.36,
            radius * 1.36);

    g2.setColor(OUTER_RING_COLOR);
    g2.fill(outer);
    g2.setColor(OUTLINE_COLOR);
    g2.setStroke(new BasicStroke(1.6f));
    g2.draw(outer);
    g2.setColor(districtColor);
    g2.fill(inner);

    String district = String.valueOf(location.district());
    g2.setFont(new Font(Font.SERIF, Font.BOLD, Math.max(15, (int) (radius * 0.95))));
    FontMetrics metrics = g2.getFontMetrics();
    g2.setColor(BoardPalette.textColorFor(districtColor));
    int textX = centerX - metrics.stringWidth(district) / 2;
    int textY = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
    g2.drawString(district, textX, textY);
  }

  private void drawLabel(
      Graphics2D g2,
      BoardLocationViewModel location,
      int centerX,
      int centerY,
      int markerSize,
      Rectangle boardBounds) {
    NormalizedOffset offset = location.labelOffset();
    int labelX = centerX + (int) Math.round(offset.dx() * boardBounds.width);
    int labelY = centerY + (int) Math.round(offset.dy() * boardBounds.height);
    Font font = new Font(Font.SERIF, Font.BOLD, Math.max(12, Math.min(22, markerSize / 2)));
    g2.setFont(font);
    FontMetrics metrics = g2.getFontMetrics();
    int minX = boardBounds.x + 8;
    int maxX = boardBounds.x + boardBounds.width - metrics.stringWidth(location.displayName()) - 8;
    int minY = boardBounds.y + metrics.getAscent() + 8;
    int maxY = boardBounds.y + boardBounds.height - 8;
    int x = Math.max(minX, Math.min(labelX, maxX));
    int y = Math.max(minY, Math.min(labelY, maxY));
    Color labelColor = location.district() <= 3 ? CENTRAL_LABEL_COLOR : LABEL_COLOR;
    drawOutlinedString(g2, location.displayName(), x, y, labelColor);
  }

  private void drawOutlinedString(Graphics2D g2, String text, int x, int y, Color labelColor) {
    g2.setColor(new Color(247, 238, 212, 190));
    g2.drawString(text, x - 1, y);
    g2.drawString(text, x + 1, y);
    g2.drawString(text, x, y - 1);
    g2.drawString(text, x, y + 1);
    g2.setColor(labelColor);
    g2.drawString(text, x, y);
  }

  private static int markerSize(Rectangle boardBounds) {
    return Math.max(26, Math.min(42, Math.min(boardBounds.width, boardBounds.height) / 18));
  }

  private static int toPixelX(NormalizedPoint point, Rectangle boardBounds) {
    return (int) Math.round(boardBounds.x + point.x() * boardBounds.width);
  }

  private static int toPixelY(NormalizedPoint point, Rectangle boardBounds) {
    return (int) Math.round(boardBounds.y + point.y() * boardBounds.height);
  }
}
