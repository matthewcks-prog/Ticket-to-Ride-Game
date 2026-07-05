package ttrlondon.application.dto;

import ttrlondon.domain.common.Text;

/**
 * Immutable outcome returned by application commands.
 */
public final class CommandResult {
  private final boolean success;
  private final String message;

  private CommandResult(boolean success, String message) {
    this.success = success;
    this.message = Text.requireNonBlank(message, "message");
  }

  /**
   * Creates a successful command result.
   *
   * @param message user-facing success message
   * @return success result
   */
  public static CommandResult success(String message) {
    return new CommandResult(true, message);
  }

  /**
   * Creates a failed command result.
   *
   * @param message user-facing failure message
   * @return failure result
   */
  public static CommandResult failure(String message) {
    return new CommandResult(false, message);
  }

  /** Returns whether the command succeeded. */
  public boolean isSuccess() {
    return success;
  }

  /** Returns whether the command failed. */
  public boolean isFailure() {
    return !success;
  }

  /** Returns the descriptive command message. */
  public String message() {
    return message;
  }
}
