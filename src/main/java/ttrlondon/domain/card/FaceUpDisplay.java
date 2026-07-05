package ttrlondon.domain.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Five-slot face-up transportation card display.
 */
public final class FaceUpDisplay {
  public static final int MAX_VISIBLE_CARDS = 5;

  private final List<CardColor> visibleCards;

  /**
   * Creates a face-up display.
   *
   * @param visibleCards initial visible cards, up to five
   */
  public FaceUpDisplay(List<CardColor> visibleCards) {
    Objects.requireNonNull(visibleCards, "visibleCards");
    if (visibleCards.size() > MAX_VISIBLE_CARDS) {
      throw new IllegalArgumentException("face-up display cannot contain more than five cards");
    }
    this.visibleCards = new ArrayList<>(visibleCards);
  }

  /** Returns visible cards by slot index. */
  public List<CardColor> visibleCards() {
    return Collections.unmodifiableList(visibleCards);
  }

  /** Takes the card at the supplied slot without applying Phase 2 refill rules. */
  public CardColor take(int index) {
    return visibleCards.remove(index);
  }

  /**
   * Refills a display slot from the deck.
   *
   * @param index slot to refill
   * @param deck transportation card deck
   * @return replacement card when the deck could supply one
   */
  public Optional<CardColor> refillSlot(int index, TransportCardDeck deck) {
    Objects.requireNonNull(deck, "deck");
    if (index < 0 || index > visibleCards.size()) {
      throw new IndexOutOfBoundsException("face-up slot index out of range: " + index);
    }
    Optional<CardColor> replacement = deck.draw();
    replacement.ifPresent(card -> visibleCards.add(index, card));
    enforceBusFlush(deck);
    return replacement;
  }

  /**
   * Applies the three-Bus-card flush rule.
   *
   * @param deck transportation card deck
   */
  public void enforceBusFlush(TransportCardDeck deck) {
    Objects.requireNonNull(deck, "deck");
    Set<SupplyState> seenStates = new HashSet<>();
    while (visibleCards.size() == MAX_VISIBLE_CARDS && busCount() >= 3) {
      SupplyState state = SupplyState.from(visibleCards, deck);
      if (!seenStates.add(state)) {
        break;
      }
      List<CardColor> flushedCards = new ArrayList<>(visibleCards);
      visibleCards.clear();
      deck.discard(flushedCards);
      refillVisibleCards(deck);
    }
  }

  /** Returns the number of visible Bus cards. */
  public int busCount() {
    int count = 0;
    for (CardColor card : visibleCards) {
      if (card == CardColor.BUS) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns whether the visible card at the supplied slot is a Bus card.
   *
   * @param index slot index to inspect
   * @return {@code true} when the slot exists and holds a Bus card
   */
  public boolean isBusAt(int index) {
    return index >= 0 && index < visibleCards.size() && visibleCards.get(index) == CardColor.BUS;
  }

  /**
   * Returns whether any visible slot other than the excluded one holds a non-Bus card.
   *
   * <p>Used to decide whether a second face-up draw is still possible when a replacement Bus card
   * has locked one slot.
   *
   * @param excludedIndex slot to ignore, or {@code -1} to consider every slot
   * @return {@code true} when a drawable non-Bus face-up card remains
   */
  public boolean hasNonBusCardOutsideSlot(int excludedIndex) {
    for (int index = 0; index < visibleCards.size(); index++) {
      if (index == excludedIndex) {
        continue;
      }
      if (visibleCards.get(index) != CardColor.BUS) {
        return true;
      }
    }
    return false;
  }

  /**
   * Restores visible face-up cards from a trusted game memento.
   *
   * @param restoredVisibleCards visible cards by slot index
   */
  public void restoreState(List<CardColor> restoredVisibleCards) {
    Objects.requireNonNull(restoredVisibleCards, "restoredVisibleCards");
    if (restoredVisibleCards.size() > MAX_VISIBLE_CARDS) {
      throw new IllegalArgumentException("face-up display cannot contain more than five cards");
    }
    visibleCards.clear();
    visibleCards.addAll(List.copyOf(restoredVisibleCards));
  }

  private void refillVisibleCards(TransportCardDeck deck) {
    while (visibleCards.size() < MAX_VISIBLE_CARDS) {
      Optional<CardColor> replacement = deck.draw();
      if (replacement.isEmpty()) {
        return;
      }
      visibleCards.add(replacement.get());
    }
  }

  private record SupplyState(
      List<CardColor> visibleCards, List<CardColor> drawPile, List<CardColor> discardPile) {
    private static SupplyState from(List<CardColor> visibleCards, TransportCardDeck deck) {
      return new SupplyState(
          List.copyOf(visibleCards), deck.drawPileSnapshot(), deck.discardPileSnapshot());
    }
  }
}
