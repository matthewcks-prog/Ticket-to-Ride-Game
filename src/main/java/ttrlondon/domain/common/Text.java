package ttrlondon.domain.common;

/**
 * Small shared text-validation helpers used by domain entities, application DTOs, and view models.
 *
 * <p>These replace the {@code requireText}/{@code normalizeOptionalText} helpers that were
 * previously copy-pasted into every class holding a string identifier or optional label.
 */
public final class Text {
  private Text() {}

  /**
   * Returns the supplied value when it is neither null nor blank.
   *
   * @param value value to validate
   * @param fieldName field name used in the error message
   * @return the validated value
   * @throws IllegalArgumentException when the value is null or blank (never {@link
   *     NullPointerException}; this is the canonical contract for identifiers across all layers)
   */
  public static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  /**
   * Normalises an optional text value, treating null or blank input as absent.
   *
   * @param value value to normalise
   * @return the trimmed-of-meaning value, or null when null or blank
   */
  public static String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value;
  }
}
