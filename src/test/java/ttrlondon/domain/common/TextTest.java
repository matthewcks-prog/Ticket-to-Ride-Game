package ttrlondon.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.LocationSnapshot;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.player.Player;
import ttrlondon.domain.player.PlayerColor;

/** Tests the shared text-validation contract used across domain, application, and UI layers. */
final class TextTest {
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "  "})
  void requireNonBlankRejectsNullOrBlank(String value) {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> Text.requireNonBlank(value, "id"));
    assertEquals("id must not be blank", exception.getMessage());
  }

  @Test
  void requireNonBlankReturnsValueWhenPresent() {
    assertEquals("P1", Text.requireNonBlank("P1", "id"));
  }

  @Test
  void normalizeOptionalTreatsNullOrBlankAsAbsent() {
    assertNull(Text.normalizeOptional(null));
    assertNull(Text.normalizeOptional(""));
    assertNull(Text.normalizeOptional(" "));
  }

  @Test
  void normalizeOptionalReturnsNonBlankValue() {
    assertEquals("claimed", Text.normalizeOptional("claimed"));
  }

  @Test
  void domainEntitiesRejectNullIdentifiersWithIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new Player(null, "Name", PlayerColor.RED));
  }

  @Test
  void applicationDtosRejectNullIdentifiersWithIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> CommandResult.success(null));
    assertThrows(
        IllegalArgumentException.class, () -> new LocationSnapshot(null, "London", 1));
  }

  @Test
  void commandsRejectNullIdentifiersWithIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ClaimRouteCommand(null, "R1", new CardPayment(List.of())));
  }
}
