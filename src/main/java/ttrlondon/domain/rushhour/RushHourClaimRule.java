package ttrlondon.domain.rushhour;

import java.util.Objects;
import ttrlondon.domain.board.Route;
import ttrlondon.domain.card.CardPayment;

/**
 * Rush Hour-specific route-claim modifier.
 */
public final class RushHourClaimRule {
  private final RushHourManager rushHourManager;

  /**
   * Creates a claim rule backed by the supplied manager.
   *
   * @param rushHourManager event manager
   */
  public RushHourClaimRule(RushHourManager rushHourManager) {
    this.rushHourManager = Objects.requireNonNull(rushHourManager, "rushHourManager");
  }

  /** Returns whether Rush Hour currently affects this route claim. */
  public boolean isAffected(Route route) {
    return rushHourManager.affectsDuringPeak(route);
  }

  /** Returns the required detour card count for the route claim. */
  public int requiredDetourCards(Route route) {
    return rushHourManager.activeEvent().filter(event -> isAffected(route)).map(RushHourEvent::extraCardCost).orElse(0);
  }

  /** Returns bonus points awarded for a successful route claim. */
  public int bonusPoints(Route route) {
    return rushHourManager.activeEvent().filter(event -> isAffected(route)).map(RushHourEvent::bonusPoints).orElse(0);
  }

  /** Returns whether the supplied detour payment satisfies Rush Hour requirements. */
  public boolean isDetourSatisfied(Route route, CardPayment detourPayment) {
    Objects.requireNonNull(detourPayment, "detourPayment");
    return detourPayment.size() == requiredDetourCards(route);
  }
}
