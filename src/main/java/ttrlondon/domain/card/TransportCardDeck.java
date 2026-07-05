package ttrlondon.domain.card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ttrlondon.domain.random.ShuffleStrategy;

/**
 * Transportation card draw and discard piles.
 */
public final class TransportCardDeck {
  private final Deque<CardColor> drawPile;
  private final List<CardColor> discardPile;
  private final ShuffleStrategy shuffleStrategy;

  /**
   * Creates a transportation card deck.
   *
   * @param drawPile initial draw pile, with the first element as the top card
   * @param shuffleStrategy injected shuffle strategy
   */
  public TransportCardDeck(List<CardColor> drawPile, ShuffleStrategy shuffleStrategy) {
    Objects.requireNonNull(drawPile, "drawPile");
    this.drawPile = new ArrayDeque<>(drawPile);
    this.discardPile = new ArrayList<>();
    this.shuffleStrategy = Objects.requireNonNull(shuffleStrategy, "shuffleStrategy");
  }

  /** Draws the top card, or returns empty when no card can be drawn. */
  public Optional<CardColor> draw() {
    if (drawPile.isEmpty()) {
      reshuffleDiscardsIntoDrawPile();
    }
    return Optional.ofNullable(drawPile.pollFirst());
  }

  /**
   * Returns whether a card can currently be drawn, accounting for discard reshuffling.
   *
   * @return {@code true} when the draw pile or the discard pile still holds a card
   */
  public boolean canDraw() {
    return !drawPile.isEmpty() || !discardPile.isEmpty();
  }

  /** Reshuffles the discard pile into the draw pile when possible. */
  public void reshuffleDiscardsIntoDrawPile() {
    if (!drawPile.isEmpty() || discardPile.isEmpty()) {
      return;
    }
    List<CardColor> shuffled = shuffleStrategy.shuffle(discardPile);
    discardPile.clear();
    drawPile.addAll(shuffled);
  }

  /** Adds cards to the discard pile. */
  public void discard(List<CardColor> cards) {
    discardPile.addAll(List.copyOf(cards));
  }

  /** Returns the number of cards currently in the draw pile. */
  public int drawPileSize() {
    return drawPile.size();
  }

  /** Returns the number of cards currently in the discard pile. */
  public int discardPileSize() {
    return discardPile.size();
  }

  /** Returns a read-only snapshot of the draw pile. */
  public List<CardColor> drawPileSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(drawPile));
  }

  /** Returns a read-only snapshot of the discard pile. */
  public List<CardColor> discardPileSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(discardPile));
  }

  /** Returns the injected shuffle strategy. */
  public ShuffleStrategy shuffleStrategy() {
    return shuffleStrategy;
  }

  /**
   * Restores draw and discard piles from a trusted game memento.
   *
   * @param restoredDrawPile draw pile in top-to-bottom order
   * @param restoredDiscardPile discard pile in insertion order
   */
  public void restoreState(List<CardColor> restoredDrawPile, List<CardColor> restoredDiscardPile) {
    Objects.requireNonNull(restoredDrawPile, "restoredDrawPile");
    Objects.requireNonNull(restoredDiscardPile, "restoredDiscardPile");
    drawPile.clear();
    drawPile.addAll(List.copyOf(restoredDrawPile));
    discardPile.clear();
    discardPile.addAll(List.copyOf(restoredDiscardPile));
  }
}
