package ttrlondon.domain.board;

import ttrlondon.domain.card.CardPayment;

/**
 * Strategy for validating whether a card payment can satisfy a printed route.
 */
public interface RouteRequirement {
  /**
   * Returns whether the supplied payment satisfies this route requirement.
   *
   * @param payment immutable payment offered by a player
   * @return true when the payment is acceptable
   */
  boolean isSatisfiedBy(CardPayment payment);
}
