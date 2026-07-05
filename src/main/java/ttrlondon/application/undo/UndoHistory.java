package ttrlondon.application.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * Bounded stack of game mementos used for turn-level undo.
 */
public final class UndoHistory {
  private static final int MAX_ENTRIES = 2;

  private final Deque<GameMemento> mementos = new ArrayDeque<>();

  /** Adds a memento, discarding the oldest entry when the history is full. */
  public void push(GameMemento memento) {
    Objects.requireNonNull(memento, "memento");
    if (mementos.size() == MAX_ENTRIES) {
      mementos.removeFirst();
    }
    mementos.addLast(memento);
  }

  /** Removes and returns the most recent memento when present. */
  public Optional<GameMemento> pop() {
    if (mementos.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(mementos.removeLast());
  }

  /** Returns whether an undo operation can currently restore a completed turn. */
  public boolean canUndo() {
    return !mementos.isEmpty();
  }
}

