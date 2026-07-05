package ttrlondon.ui.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import ttrlondon.domain.player.PlayerColor;
import ttrlondon.ui.viewmodel.BoardRouteViewModel;
import ttrlondon.ui.viewmodel.NormalizedPoint;

/**
 * Draws printed route slots and claimed-route bus overlays.
 */
public final class RouteRenderer {
  private static final Color SLOT_BORDER = new Color(50, 38, 28, 115);
  private static final Color HOVER_BORDER = new Color(255, 245, 129);
  private static final Color RUSH_HOUR_BORDER = new Color(210, 48, 36);
  private static final Color SHADOW = new Color(60, 43, 32, 76);
  private static final Image FERRY_SYMBOL = loadFerrySymbol();

  /**
   * Draws a route and returns the aggregate hit shape for hover detection.
   *
   * @param graphics graphics context
   * @param route route to draw
   * @param boardBounds aspect-correct board drawing bounds
   * @param hovered whether this route is currently hovered
   * @return hit shape covering all route slots
   */
  public Shape drawRoute(
      Graphics2D graphics, BoardRouteViewModel route, Rectangle boardBounds, boolean hovered) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Area hitArea = new Area();
      RoutePath path = routePath(route, boardBounds);
      double slotLength =
          Math.max(24.0, Math.min(42.0, Math.min(boardBounds.width, boardBounds.height) / 18.5));
      double slotWidth = Math.max(12.0, slotLength * 0.48);
      double start = 0.5 / route.length();

      for (int index = 0; index < route.length(); index++) {
        double t = start + (double) index / route.length();
        Point2D center = path.pointAt(t);
        double angle = Math.atan2(path.tangentAt(t).getY(), path.tangentAt(t).getX());
        Shape slot = slotShape(center, slotLength, slotWidth, angle);
        hitArea.add(new Area(slot));
        drawSlot(g2, route, slot, slotWidth, hovered, index);
      }
      return hitArea;
    } finally {
      g2.dispose();
    }
  }

  private void drawSlot(
      Graphics2D g2,
      BoardRouteViewModel route,
      Shape slot,
      double slotWidth,
      boolean hovered,
      int slotIndex) {
    g2.setColor(SHADOW);
    AffineTransform shadowTransform = AffineTransform.getTranslateInstance(1.5, 2.0);
    g2.fill(shadowTransform.createTransformedShape(slot));

    g2.setColor(BoardPalette.routeColor(route.color()));
    g2.fill(slot);
    g2.setStroke(new BasicStroke(hovered || route.rushHourAffected() ? 3.0f : 0.9f));
    g2.setColor(hovered ? HOVER_BORDER : route.rushHourAffected() ? RUSH_HOUR_BORDER : SLOT_BORDER);
    g2.draw(slot);

    if (route.isFerry() && slotIndex < route.requiredBusSymbols()) {
      drawFerrySymbol(g2, slot);
    }
    route.claimingPlayerColor().ifPresent(color -> drawBusOverlay(g2, slot, slotWidth, color));
  }

  private void drawFerrySymbol(Graphics2D g2, Shape slot) {
    java.awt.Rectangle bounds = slot.getBounds();
    int iconSize = (int) Math.max(10.0, Math.min(bounds.width, bounds.height) * 0.78);
    int x = (int) Math.round(bounds.getCenterX() - iconSize / 2.0);
    int y = (int) Math.round(bounds.getCenterY() - iconSize / 2.0);
    if (FERRY_SYMBOL != null) {
      g2.drawImage(FERRY_SYMBOL, x, y, iconSize, iconSize, null);
      return;
    }

    Ellipse2D marker =
        new Ellipse2D.Double(
            bounds.getCenterX() - iconSize / 2.0,
            bounds.getCenterY() - iconSize / 2.0,
            iconSize,
            iconSize);
    g2.setColor(new Color(255, 248, 226, 220));
    g2.fill(marker);
    g2.setStroke(new BasicStroke(1.2f));
    g2.setColor(new Color(28, 51, 82));
    g2.draw(marker);
  }

  private void drawBusOverlay(Graphics2D g2, Shape slot, double slotWidth, PlayerColor playerColor) {
    java.awt.Rectangle bounds = slot.getBounds();
    double busWidth = bounds.width * 0.64;
    double busHeight = Math.max(7.0, slotWidth * 0.42);
    RoundRectangle2D bus =
        new RoundRectangle2D.Double(
            bounds.getCenterX() - busWidth / 2.0,
            bounds.getCenterY() - busHeight / 2.0,
            busWidth,
            busHeight,
            busHeight,
            busHeight);

    Color fill = BoardPalette.playerColor(playerColor);
    g2.setColor(fill);
    g2.fill(bus);
    g2.setStroke(new BasicStroke(1.4f));
    g2.setColor(playerColor == PlayerColor.WHITE ? new Color(45, 45, 45) : Color.WHITE);
    g2.draw(bus);
  }

  private Shape slotShape(Point2D center, double slotLength, double slotWidth, double angle) {
    RoundRectangle2D slot =
        new RoundRectangle2D.Double(
            -slotLength / 2.0,
            -slotWidth / 2.0,
            slotLength,
            slotWidth,
            slotWidth * 0.8,
            slotWidth * 0.8);
    AffineTransform transform = new AffineTransform();
    transform.translate(center.getX(), center.getY());
    transform.rotate(angle);
    return transform.createTransformedShape(slot);
  }

  private RoutePath routePath(BoardRouteViewModel route, Rectangle boardBounds) {
    Point2D start = toPixel(route.locationA().position(), boardBounds);
    Point2D end = toPixel(route.locationB().position(), boardBounds);
    double dx = end.getX() - start.getX();
    double dy = end.getY() - start.getY();
    double distance = Math.max(1.0, Math.hypot(dx, dy));
    double normalX = -dy / distance;
    double normalY = dx / distance;
    double lanePixels =
        route.laneOffset() * Math.max(13.0, Math.min(boardBounds.width, boardBounds.height) / 42.0);
    double curvePixels = route.curveOffset() * Math.min(boardBounds.width, boardBounds.height);
    Point2D laneStart =
        new Point2D.Double(
            start.getX() + normalX * lanePixels, start.getY() + normalY * lanePixels);
    Point2D laneEnd =
        new Point2D.Double(
            end.getX() + normalX * lanePixels, end.getY() + normalY * lanePixels);
    Point2D control =
        new Point2D.Double(
            (start.getX() + end.getX()) / 2.0 + normalX * (lanePixels + curvePixels),
            (start.getY() + end.getY()) / 2.0 + normalY * (lanePixels + curvePixels));
    return new RoutePath(laneStart, control, laneEnd);
  }

  private Point2D toPixel(NormalizedPoint point, Rectangle boardBounds) {
    return new Point2D.Double(
        boardBounds.x + point.x() * boardBounds.width,
        boardBounds.y + point.y() * boardBounds.height);
  }

  private static Image loadFerrySymbol() {
    try {
      return ImageIO.read(
          Objects.requireNonNull(
              RouteRenderer.class.getResource("/ttrlondon/ui/ferry-symbol.png"),
              "ferry-symbol.png"));
    } catch (IOException | NullPointerException exception) {
      return null;
    }
  }

  private record RoutePath(Point2D start, Point2D control, Point2D end) {
    Point2D pointAt(double t) {
      double inverse = 1.0 - t;
      double x =
          inverse * inverse * start.getX()
              + 2.0 * inverse * t * control.getX()
              + t * t * end.getX();
      double y =
          inverse * inverse * start.getY()
              + 2.0 * inverse * t * control.getY()
              + t * t * end.getY();
      return new Point2D.Double(x, y);
    }

    Point2D tangentAt(double t) {
      double x =
          2.0 * (1.0 - t) * (control.getX() - start.getX())
              + 2.0 * t * (end.getX() - control.getX());
      double y =
          2.0 * (1.0 - t) * (control.getY() - start.getY())
              + 2.0 * t * (end.getY() - control.getY());
      return new Point2D.Double(x, y);
    }
  }
}
