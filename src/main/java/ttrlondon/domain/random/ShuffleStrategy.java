package ttrlondon.domain.random;

import java.util.List;

/**
 * Strategy interface for deterministic or random shuffling.
 */
public interface ShuffleStrategy {
  /**
   * Returns a shuffled copy of the supplied items.
   *
   * @param items items to shuffle
   * @param <T> item type
   * @return shuffled copy
   */
  <T> List<T> shuffle(List<T> items);
}
