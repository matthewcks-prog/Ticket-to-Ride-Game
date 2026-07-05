package ttrlondon.domain.board;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;

/**
 * Requirement for a route that must be paid with one specific colour plus optional Bus cards.
 */
public final class ColouredRouteRequirement implements RouteRequirement {
  private static final Map<RouteColor, CardColor> CARD_COLORS_BY_ROUTE_COLOR =
      createCardColorMap();

  private final RouteColor requiredColor;
  private final int length;

  /**
   * Creates a coloured route requirement.
   *
   * @param requiredColor non-grey route colour required for payment
   * @param length number of cards required
   */
  public ColouredRouteRequirement(RouteColor requiredColor, int length) {
    if (requiredColor == RouteColor.GREY) {
      throw new IllegalArgumentException("Coloured route requirement cannot require grey");
    }
    this.requiredColor = Objects.requireNonNull(requiredColor, "requiredColor");
    this.length = requirePositive(length);
  }

  /** Returns the required route colour. */
  public RouteColor requiredColor() {
    return requiredColor;
  }

  /** Returns the required payment length. */
  public int length() {
    return length;
  }

  @Override
  public boolean isSatisfiedBy(CardPayment payment) {
    Objects.requireNonNull(payment, "payment");
    CardColor requiredCardColor = CARD_COLORS_BY_ROUTE_COLOR.get(requiredColor);
    return payment.size() == length
        && payment.hasSingleNonBusColor()
        && payment.nonBusCardsMatch(requiredCardColor);
  }

  private static int requirePositive(int value) {
    if (value <= 0) {
      throw new IllegalArgumentException("length must be positive");
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
