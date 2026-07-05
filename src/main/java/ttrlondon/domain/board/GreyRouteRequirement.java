package ttrlondon.domain.board;

import java.util.Objects;
import ttrlondon.domain.card.CardPayment;

/**
 * Requirement for a grey route that accepts any single colour set plus optional Bus cards.
 */
public final class GreyRouteRequirement implements RouteRequirement {
  private final int length;

  /**
   * Creates a grey route requirement.
   *
   * @param length number of cards required
   */
  public GreyRouteRequirement(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("length must be positive");
    }
    this.length = length;
  }

  /** Returns the required payment length. */
  public int length() {
    return length;
  }

  @Override
  public boolean isSatisfiedBy(CardPayment payment) {
    Objects.requireNonNull(payment, "payment");
    return payment.size() == length && payment.hasSingleNonBusColor();
  }
}
