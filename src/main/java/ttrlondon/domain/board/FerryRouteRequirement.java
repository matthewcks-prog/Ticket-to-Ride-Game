package ttrlondon.domain.board;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;

/**
 * Requirement for a ferry route with mandatory Bus-symbol payments.
 */
public final class FerryRouteRequirement implements RouteRequirement {
  private static final int SUBSTITUTE_CARDS_PER_BUS_SYMBOL = 3;
  private static final Map<RouteColor, CardColor> CARD_COLORS_BY_ROUTE_COLOR =
      createCardColorMap();

  private final RouteColor routeColor;
  private final int length;
  private final int requiredBusSymbols;

  /**
   * Creates a ferry route requirement.
   *
   * @param routeColor printed route colour
   * @param length printed route length
   * @param requiredBusSymbols number of required Bus symbols
   */
  public FerryRouteRequirement(RouteColor routeColor, int length, int requiredBusSymbols) {
    this.routeColor = Objects.requireNonNull(routeColor, "routeColor");
    this.length = requirePositive(length, "length");
    this.requiredBusSymbols = requirePositive(requiredBusSymbols, "requiredBusSymbols");
    if (requiredBusSymbols > length) {
      throw new IllegalArgumentException("requiredBusSymbols cannot exceed route length");
    }
  }

  /** Returns the printed route colour. */
  public RouteColor routeColor() {
    return routeColor;
  }

  /** Returns the printed route length. */
  public int length() {
    return length;
  }

  /** Returns the number of required Bus symbols. */
  public int requiredBusSymbols() {
    return requiredBusSymbols;
  }

  @Override
  public boolean isSatisfiedBy(CardPayment payment) {
    Objects.requireNonNull(payment, "payment");
    int remainingRouteSpaces = length - requiredBusSymbols;
    int maximumDirectBusSymbols = Math.min(requiredBusSymbols, payment.count(CardColor.BUS));
    for (int directBusSymbols = maximumDirectBusSymbols; directBusSymbols >= 0; directBusSymbols--) {
      int substituteSymbols = requiredBusSymbols - directBusSymbols;
      int substituteCardCount = substituteSymbols * SUBSTITUTE_CARDS_PER_BUS_SYMBOL;
      int expectedPaymentSize = directBusSymbols + substituteCardCount + remainingRouteSpaces;
      if (payment.size() != expectedPaymentSize) {
        continue;
      }
      List<CardColor> afterDirectBusSymbols = payment.copyCards();
      if (!removeCards(afterDirectBusSymbols, CardColor.BUS, directBusSymbols)) {
        continue;
      }
      if (containsValidRemainder(afterDirectBusSymbols, remainingRouteSpaces)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsValidRemainder(List<CardColor> cards, int remainingRouteSpaces) {
    if (remainingRouteSpaces == 0) {
      return true;
    }
    return containsValidRemainder(cards, remainingRouteSpaces, 0, new ArrayList<>());
  }

  private boolean containsValidRemainder(
      List<CardColor> cards, int remainingRouteSpaces, int startIndex, List<CardColor> chosen) {
    if (chosen.size() == remainingRouteSpaces) {
      return normalRouteSpacesSatisfiedBy(new CardPayment(chosen));
    }
    for (int index = startIndex; index < cards.size(); index++) {
      chosen.add(cards.get(index));
      if (containsValidRemainder(cards, remainingRouteSpaces, index + 1, chosen)) {
        return true;
      }
      chosen.remove(chosen.size() - 1);
    }
    return false;
  }

  private boolean normalRouteSpacesSatisfiedBy(CardPayment payment) {
    if (payment.size() != length - requiredBusSymbols || !payment.hasSingleNonBusColor()) {
      return false;
    }
    if (routeColor == RouteColor.GREY) {
      return true;
    }
    return payment.nonBusCardsMatch(CARD_COLORS_BY_ROUTE_COLOR.get(routeColor));
  }

  private static boolean removeCards(List<CardColor> cards, CardColor cardColor, int count) {
    for (int removed = 0; removed < count; removed++) {
      if (!cards.remove(cardColor)) {
        return false;
      }
    }
    return true;
  }

  private static int requirePositive(int value, String fieldName) {
    if (value <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive");
    }
    return value;
  }

  private static Map<RouteColor, CardColor> createCardColorMap() {
    Map<RouteColor, CardColor> colors = new EnumMap<>(RouteColor.class);
    colors.put(RouteColor.BLUE, CardColor.BLUE);
    colors.put(RouteColor.GREEN, CardColor.GREEN);
    colors.put(RouteColor.BLACK, CardColor.BLACK);
    colors.put(RouteColor.PINK, CardColor.PINK);
    colors.put(RouteColor.YELLOW, CardColor.YELLOW);
    colors.put(RouteColor.ORANGE, CardColor.ORANGE);
    return colors;
  }
}
