package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.domain.card.CardColor;

/**
 * Displays the face-up transportation card market and deck counts.
 */
public final class CardMarketPanel extends JPanel implements GameStateListener {
  private final JPanel faceUpCardsPanel;
  private final JButton blindDeckButton;
  private final JLabel discardPileLabel;
  private final BiConsumer<String, Integer> faceUpDrawHandler;
  private final Consumer<String> blindDrawHandler;
  private String activePlayerId;

  /**
   * Creates the transportation card market panel.
   */
  public CardMarketPanel() {
    this((playerId, index) -> {}, playerId -> {});
  }

  /**
   * Creates the transportation card market panel with interaction handlers.
   *
   * @param faceUpDrawHandler callback for face-up card selections
   * @param blindDrawHandler callback for blind draw-pile selections
   */
  public CardMarketPanel(
      BiConsumer<String, Integer> faceUpDrawHandler, Consumer<String> blindDrawHandler) {
    super(new BorderLayout(12, 8));
    this.faceUpDrawHandler = Objects.requireNonNull(faceUpDrawHandler, "faceUpDrawHandler");
    this.blindDrawHandler = Objects.requireNonNull(blindDrawHandler, "blindDrawHandler");
    setBorder(UiSupport.titledBorder("Transportation Cards"));

    faceUpCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    blindDeckButton = new JButton();
    discardPileLabel = new JLabel();
    activePlayerId = "";
    configureBlindDeckButton();

    JPanel pilePanel = new JPanel(new GridLayout(1, 2, 10, 0));
    pilePanel.add(blindDeckButton);
    pilePanel.add(discardPileLabel);

    add(faceUpCardsPanel, BorderLayout.CENTER);
    add(pilePanel, BorderLayout.EAST);
  }

  /**
   * Updates visible cards and deck counts from the latest snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    faceUpCardsPanel.removeAll();
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    for (int index = 0; index < snapshot.faceUpCards().size(); index++) {
      CardColor cardColor = snapshot.faceUpCards().get(index);
      faceUpCardsPanel.add(createCardLabel(snapshot, activePlayer.id(), cardColor, index));
    }
    activePlayerId = activePlayer.id();
    updateBlindDeckButton(snapshot);
    discardPileLabel.setText("Discard: " + snapshot.transportDiscardPileSize());
    discardPileLabel.setHorizontalAlignment(SwingConstants.CENTER);
    faceUpCardsPanel.revalidate();
    faceUpCardsPanel.repaint();
  }

  private JLabel createCardLabel(
      GameSnapshot snapshot, String activePlayerId, CardColor cardColor, int index) {
    JLabel label = new JLabel(UiSupport.displayName(cardColor), SwingConstants.CENTER);
    label.setOpaque(true);
    label.setPreferredSize(new Dimension(88, 44));
    label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    label.setBackground(UiSupport.cardColor(cardColor));
    label.setForeground(UiSupport.textColorFor(UiSupport.cardColor(cardColor)));
    label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    boolean unavailable = isUnavailableForSecondDraw(snapshot, cardColor, index);
    boolean canDraw = snapshot.acceptsPlayerActions() && !unavailable;
    label.setEnabled(canDraw);
    label.setToolTipText(cardTooltip(canDraw, unavailable));
    if (unavailable) {
      label.setText(
          "<html><center>" + UiSupport.displayName(cardColor) + "<br>Locked</center></html>");
      label.setBackground(label.getBackground().darker());
    }
    label.addMouseListener(new FaceUpCardClickListener(activePlayerId, index, canDraw));
    return label;
  }

  private void configureBlindDeckButton() {
    blindDeckButton.setName("blindDeckButton");
    blindDeckButton.setFocusable(false);
    blindDeckButton.setOpaque(true);
    blindDeckButton.setPreferredSize(new Dimension(112, 68));
    blindDeckButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    blindDeckButton.setBackground(new Color(73, 40, 117));
    blindDeckButton.setForeground(Color.WHITE);
    blindDeckButton.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    blindDeckButton.addActionListener(
        event -> {
          if (blindDeckButton.isEnabled()) {
            blindDrawHandler.accept(activePlayerId);
          }
        });
  }

  private void updateBlindDeckButton(GameSnapshot snapshot) {
    int drawableCards = snapshot.transportDrawPileSize() + snapshot.transportDiscardPileSize();
    boolean canDraw = snapshot.acceptsPlayerActions() && drawableCards > 0;
    blindDeckButton.setText(
        "<html><center>Blind Deck<br>"
            + snapshot.transportDrawPileSize()
            + " draw<br>"
            + snapshot.transportDiscardPileSize()
            + " discard</center></html>");
    blindDeckButton.setEnabled(canDraw);
    blindDeckButton.setToolTipText(
        canDraw
            ? "Blind draw from the transportation deck"
            : "No blind transportation cards are available");
  }

  private boolean isUnavailableForSecondDraw(
      GameSnapshot snapshot, CardColor cardColor, int index) {
    return snapshot.transportDrawActionActive()
        && snapshot.transportDrawsTaken() == 1
        && (cardColor == CardColor.BUS || snapshot.lockedFaceUpIndex() == index);
  }

  private String cardTooltip(boolean canDraw, boolean unavailableForSecondDraw) {
    if (canDraw) {
      return "Draw this face-up card";
    }
    if (unavailableForSecondDraw) {
      return "Unavailable as the second draw";
    }
    return "Game is not accepting player actions";
  }

  private final class FaceUpCardClickListener extends MouseAdapter {
    private final String playerId;
    private final int index;
    private final boolean canDraw;

    private FaceUpCardClickListener(String playerId, int index, boolean canDraw) {
      this.playerId = playerId;
      this.index = index;
      this.canDraw = canDraw;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
      if (canDraw) {
        faceUpDrawHandler.accept(playerId, index);
      }
    }
  }
}
