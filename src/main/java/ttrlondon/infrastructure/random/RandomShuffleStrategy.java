package ttrlondon.infrastructure.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import ttrlondon.domain.random.ShuffleStrategy;

/**
 * Shuffle strategy backed by {@link Random}.
 */
public final class RandomShuffleStrategy implements ShuffleStrategy {
  private final Random random;

  /** Creates a strategy with a new random source. */
  public RandomShuffleStrategy() {
    this(new Random());
  }

  /**
   * Creates a strategy with an injected random source.
   *
   * @param random random source
   */
  public RandomShuffleStrategy(Random random) {
    this.random = Objects.requireNonNull(random, "random");
  }

  @Override
  public <T> List<T> shuffle(List<T> items) {
    List<T> shuffled = new ArrayList<>(items);
    Collections.shuffle(shuffled, random);
    return shuffled;
  }
}
