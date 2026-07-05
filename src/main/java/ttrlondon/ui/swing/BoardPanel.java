package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.ui.rendering.BoardRenderer;
import ttrlondon.ui.rendering.LocationRenderer;
import ttrlondon.ui.rendering.RouteRenderer;
import ttrlondon.ui.viewmodel.BoardViewModel;

/**
 * Swing board canvas that renders immutable board view models and captures route hover state.
 */
public final class BoardPanel extends JPanel implements GameStateListener {
  private final BoardRenderer boardRenderer;
  private final Consumer<String> routeSelectionHandler;
  private BoardViewModel boardViewModel;
  private Map<String, Shape> routeShapes = Collections.emptyMap();
  private String hoveredRouteId;
  private String selectedRouteId;

  /**
   * Creates a board panel with the default London board renderer.
   */
  public BoardPanel() {
    this(
        new BoardRenderer(new RouteRenderer(), new LocationRenderer(), loadBackgroundImage()),
        routeId -> {});
  }

  /**
   * Creates a board panel with an injected renderer.
   *
   * @param boardRenderer renderer used to draw the board
   */
  public BoardPanel(BoardRenderer boardRenderer) {
    this(boardRenderer, routeId -> {});
  }

  /**
   * Creates a board panel with an injected renderer and route selection handler.
   *
   * @param boardRenderer renderer used to draw the board
   * @param routeSelectionHandler callback invoked when a route is selected
   */
  public BoardPanel(BoardRenderer boardRenderer, Consumer<String> routeSelectionHandler) {
    super(new BorderLayout());
    this.boardRenderer = Objects.requireNonNull(boardRenderer, "boardRenderer");
    this.routeSelectionHandler =
        Objects.requireNonNull(routeSelectionHandler, "routeSelectionHandler");
    setBorder(BorderFactory.createLineBorder(new Color(116, 92, 67), 2));
    setBackground(new Color(238, 225, 197));
    setPreferredSize(new Dimension(760, 560));
    setMinimumSize(new Dimension(560, 420));
    addMouseMotionListener(new RouteHoverListener());
    addMouseListener(new RouteMouseListener());
  }

  /**
   * Updates the board rendering model from the latest game snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    boardViewModel = BoardViewModel.from(snapshot);
    repaint();
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    if (boardViewModel == null) {
      return;
    }
    routeShapes =
        boardRenderer.render(
            (Graphics2D) graphics, getSize(), boardViewModel, hoveredRouteId, selectedRouteId);
  }

  private void updateHoveredRoute(Point point) {
    String newHoveredRouteId = findHoveredRouteId(point);
    if (!Objects.equals(hoveredRouteId, newHoveredRouteId)) {
      hoveredRouteId = newHoveredRouteId;
      setToolTipText(hoveredRouteId);
      repaint();
    }
  }

  private String findHoveredRouteId(Point point) {
    for (Map.Entry<String, Shape> entry : routeShapes.entrySet()) {
      if (entry.getValue().contains(point)) {
        return entry.getKey();
      }
    }
    return null;
  }

  private static Image loadBackgroundImage() {
    return UiImageLoader.loadLondonMap().orElse(null);
  }

  private final class RouteHoverListener extends MouseAdapter {
    @Override
    public void mouseMoved(MouseEvent event) {
      updateHoveredRoute(event.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent event) {
      updateHoveredRoute(event.getPoint());
    }
  }

  private void selectRoute(Point point) {
    String newSelectedRouteId = findHoveredRouteId(point);
    if (newSelectedRouteId == null) {
      return;
    }
    selectedRouteId = newSelectedRouteId;
    routeSelectionHandler.accept(selectedRouteId);
    repaint();
  }

  private final class RouteMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent event) {
      selectRoute(event.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent event) {
      if (hoveredRouteId != null) {
        hoveredRouteId = null;
        setToolTipText(null);
        repaint();
      }
    }
  }
}
