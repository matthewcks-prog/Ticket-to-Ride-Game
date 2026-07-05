package ttrlondon.infrastructure.random;

import java.util.List;
import ttrlondon.domain.random.ShuffleStrategy;

/**
 * Deterministic shuffle strategy that preserves item order for tests.
 */
public final class FixedOrderShuffleStrategy implements ShuffleStrategy {
  @Override
  public <T> List<T> shuffle(List<T> items) {
    return List.copyOf(items);
  }
}
