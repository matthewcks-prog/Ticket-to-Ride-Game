package ttrlondon.domain.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable value object representing cards offered to claim a route.
 */
public final class CardPayment {
  private final List<CardColor> cards;

  /**
   * Creates a card payment.
   *
   * @param cards cards offered by the player
   */
  public CardPayment(List<CardColor> cards) {
    Objects.requireNonNull(cards, "cards");
    if (cards.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("payment cards must not contain null");
    }
    this.cards = List.copyOf(cards);
  }

  /** Returns the cards in this payment. */
  public List<CardColor> cards() {
    return Collections.unmodifiableList(cards);
  }

  /** Returns the total number of cards offered. */
  public int size() {
    return cards.size();
  }

  /** Returns how many cards of a colour are present. */
  public int count(CardColor color) {
    int count = 0;
    for (CardColor card : cards) {
      if (card == color) {
        count++;
      }
    }
    return count;
  }

  /** Returns card counts grouped by colour. */
  public Map<CardColor, Integer> countsByColor() {
    Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
    for (CardColor card : cards) {
      counts.merge(card, 1, Integer::sum);
    }
    return Collections.unmodifiableMap(counts);
  }

  /** Returns whether all non-Bus cards in this payment are the same colour. */
  public boolean hasSingleNonBusColor() {
    CardColor firstColor = null;
    for (CardColor card : cards) {
      if (card == CardColor.BUS) {
        continue;
      }
      if (firstColor == null) {
        firstColor = card;
      } else if (firstColor != card) {
        return false;
      }
    }
    return true;
  }

  /** Returns whether every non-Bus card has the supplied colour. */
  public boolean nonBusCardsMatch(CardColor requiredColor) {
    Objects.requireNonNull(requiredColor, "requiredColor");
    for (CardColor card : cards) {
      if (card != CardColor.BUS && card != requiredColor) {
        return false;
      }
    }
    return true;
  }

  /** Returns a mutable copy of this payment's card list for internal consumers. */
  public List<CardColor> copyCards() {
    return new ArrayList<>(cards);
  }
}
