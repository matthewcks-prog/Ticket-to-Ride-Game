package ttrlondon.ui.viewmodel;

import java.util.Objects;
import ttrlondon.domain.common.Text;

/**
 * Immutable rendering read model for a London board location.
 */
public final class BoardLocationViewModel {
  private final String id;
  private final String displayName;
  private final int district;
  private final NormalizedPoint position;
  private final NormalizedOffset labelOffset;

  /**
   * Creates a location rendering view model.
   *
   * @param id location identifier
   * @param displayName location label
   * @param district district number displayed inside the station marker
   * @param position normalized station position
   * @param labelOffset label offset relative to the station position
   */
  public BoardLocationViewModel(
      String id,
      String displayName,
      int district,
      NormalizedPoint position,
      NormalizedOffset labelOffset) {
    this.id = Text.requireNonBlank(id, "id");
    this.displayName = Text.requireNonBlank(displayName, "displayName");
    this.district = district;
    this.position = Objects.requireNonNull(position, "position");
    this.labelOffset = Objects.requireNonNull(labelOffset, "labelOffset");
  }

  /** Returns the location identifier. */
  public String id() {
    return id;
  }

  /** Returns the label text. */
  public String displayName() {
    return displayName;
  }

  /** Returns the London district number. */
  public int district() {
    return district;
  }

  /** Returns the normalized station position. */
  public NormalizedPoint position() {
    return position;
  }

  /** Returns the normalized label offset. */
  public NormalizedOffset labelOffset() {
    return labelOffset;
  }
}
