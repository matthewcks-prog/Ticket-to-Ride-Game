package ttrlondon.application.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ttrlondon.domain.card.CardPayment;

/**
 * Immutable route-claim payment split into normal route payment and Rush Hour detour payment.
 */
public final class ClaimRoutePayment {
  private final CardPayment routePayment;
  private final CardPayment rushHourDetourPayment;

  /**
   * Creates a split route-claim payment.
   *
   * @param routePayment normal route or Ferry payment
   * @param rushHourDetourPayment extra Rush Hour detour cards
   */
  public ClaimRoutePayment(CardPayment routePayment, CardPayment rushHourDetourPayment) {
    this.routePayment = Objects.requireNonNull(routePayment, "routePayment");
    this.rushHourDetourPayment =
        Objects.requireNonNull(rushHourDetourPayment, "rushHourDetourPayment");
  }

  /** Creates a claim payment with no Rush Hour detour cards. */
  public static ClaimRoutePayment routeOnly(CardPayment routePayment) {
    return new ClaimRoutePayment(routePayment, new CardPayment(List.of()));
  }

  /** Returns normal route or Ferry payment cards. */
  public CardPayment routePayment() {
    return routePayment;
  }

  /** Returns Rush Hour detour payment cards. */
  public CardPayment rushHourDetourPayment() {
    return rushHourDetourPayment;
  }

  /** Returns all cards paid for this route claim. */
  public CardPayment combinedPayment() {
    List<ttrlondon.domain.card.CardColor> combined = new ArrayList<>(routePayment.cards());
    combined.addAll(rushHourDetourPayment.cards());
    return new CardPayment(combined);
  }
}
