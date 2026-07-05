package ttrlondon.ui.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.Map;
import ttrlondon.ui.viewmodel.BoardRouteViewModel;
import ttrlondon.ui.viewmodel.BoardViewModel;

/**
 * Orchestrates drawing of the London board background, routes, and locations.
 */
public final class BoardRenderer {
  private static final double LONDON_MAP_ASPECT_RATIO = 1559.0 / 1206.0;

  private final RouteRenderer routeRenderer;
  private final LocationRenderer locationRenderer;
  private final ScoreTrackRenderer scoreTrackRenderer;
  private final Image backgroundImage;

  /**
   * Creates a board renderer.
   *
   * @param routeRenderer renderer for route slots
   * @param locationRenderer renderer for station markers
   * @param backgroundImage optional board background image
   */
  public BoardRenderer(
      RouteRenderer routeRenderer, LocationRenderer locationRenderer, Image backgroundImage) {
    this.routeRenderer = routeRenderer;
    this.locationRenderer = locationRenderer;
    this.scoreTrackRenderer = new ScoreTrackRenderer();
    this.backgroundImage = backgroundImage;
  }

  /**
   * Draws the board and returns route hit shapes keyed by route identifier.
   *
   * @param graphics graphics context
   * @param size current board canvas size
   * @param board board rendering model
   * @param hoveredRouteId currently hovered route identifier, or null
   * @param selectedRouteId currently selected route identifier, or null
   * @return route hit shapes
   */
  public Map<String, Shape> render(
      Graphics2D graphics,
      Dimension size,
      BoardViewModel board,
      String hoveredRouteId,
      String selectedRouteId) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle boardBounds = boardBounds(size);
      drawBackground(g2, size, boardBounds);
      scoreTrackRenderer.drawScoreMarkers(g2, boardBounds, board.scoreMarkers());
      Map<String, Shape> routeShapes = new LinkedHashMap<>();
      for (BoardRouteViewModel route : board.routes()) {
        boolean isHighlighted =
            route.id().equals(hoveredRouteId) || route.id().equals(selectedRouteId);
        Shape shape =
            routeRenderer.drawRoute(g2, route, boardBounds, isHighlighted);
        routeShapes.put(route.id(), shape);
      }
      board.locations()
          .forEach(location -> locationRenderer.drawLocation(g2, location, boardBounds));
      return routeShapes;
    } finally {
      g2.dispose();
    }
  }

  private Rectangle boardBounds(Dimension size) {
    double panelAspectRatio = (double) size.width / Math.max(1, size.height);
    int boardWidth;
    int boardHeight;
    if (panelAspectRatio > LONDON_MAP_ASPECT_RATIO) {
      boardHeight = size.height;
      boardWidth = (int) Math.round(boardHeight * LONDON_MAP_ASPECT_RATIO);
    } else {
      boardWidth = size.width;
      boardHeight = (int) Math.round(boardWidth / LONDON_MAP_ASPECT_RATIO);
    }
    return new Rectangle(
        (size.width - boardWidth) / 2,
        (size.height - boardHeight) / 2,
        boardWidth,
        boardHeight);
  }

  private void drawBackground(Graphics2D g2, Dimension size, Rectangle boardBounds) {
    g2.setColor(new Color(42, 34, 28));
    g2.fillRect(0, 0, size.width, size.height);
    if (backgroundImage != null) {
      g2.drawImage(
          backgroundImage,
          boardBounds.x,
          boardBounds.y,
          boardBounds.width,
          boardBounds.height,
          null);
      g2.setColor(new Color(255, 248, 226, 36));
      g2.fillRect(boardBounds.x, boardBounds.y, boardBounds.width, boardBounds.height);
      return;
    }
    g2.setColor(new Color(238, 225, 197));
    g2.fillRect(boardBounds.x, boardBounds.y, boardBounds.width, boardBounds.height);
    drawRiver(g2, boardBounds);
    drawScoreTrackHint(g2, boardBounds);
  }

  private void drawRiver(Graphics2D g2, Rectangle boardBounds) {
    Path2D river = new Path2D.Double();
    river.moveTo(x(boardBounds, 0.34), y(boardBounds, 0.76));
    river.curveTo(
        x(boardBounds, 0.45),
        y(boardBounds, 0.68),
        x(boardBounds, 0.58),
        y(boardBounds, 0.78),
        x(boardBounds, 0.72),
        y(boardBounds, 0.67));
    river.curveTo(
        x(boardBounds, 0.82),
        y(boardBounds, 0.60),
        x(boardBounds, 0.92),
        y(boardBounds, 0.66),
        x(boardBounds, 1.0),
        y(boardBounds, 0.58));
    g2.setStroke(
        new BasicStroke(
            Math.max(26.0f, boardBounds.height / 18.0f),
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND));
    g2.setColor(new Color(109, 163, 186, 150));
    g2.draw(river);
  }

  private void drawScoreTrackHint(Graphics2D g2, Rectangle boardBounds) {
    g2.setColor(new Color(115, 74, 50, 100));
    g2.setStroke(new BasicStroke(3.0f));
    g2.drawRoundRect(
        boardBounds.x + 8,
        boardBounds.y + 8,
        boardBounds.width - 16,
        boardBounds.height - 16,
        14,
        14);
  }

  private static double x(Rectangle boardBounds, double normalizedX) {
    return boardBounds.x + normalizedX * boardBounds.width;
  }

  private static double y(Rectangle boardBounds, double normalizedY) {
    return boardBounds.y + normalizedY * boardBounds.height;
  }
}
