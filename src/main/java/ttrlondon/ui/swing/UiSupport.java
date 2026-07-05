package ttrlondon.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.player.PlayerColor;

final class UiSupport {
  private UiSupport() {}

  static Border titledBorder(String title) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(title), BorderFactory.createEmptyBorder(8, 8, 8, 8));
  }

  /**
   * Shows a destination-ticket selection dialog and returns the checked ticket identifiers.
   *
   * <p>Re-prompts until at least one ticket is kept, mirroring the destination-ticket keep rule.
   *
   * @param parent parent component for the modal dialogs
   * @param form pre-built panel containing the ticket check boxes
   * @param dialogTitle confirm-dialog title
   * @param warningTitle title for the "keep at least one" warning dialog
   * @param checkBoxesById ticket identifier to its check box, in display order
   * @return kept ticket identifiers, or null if the player cancels
   */
  static List<String> chooseKeptTickets(
      Component parent,
      JComponent form,
      String dialogTitle,
      String warningTitle,
      Map<String, JCheckBox> checkBoxesById) {
    while (true) {
      int option = FormConfirmDialog.showCompactConfirmDialog(parent, dialogTitle, fixedForm(form));
      if (option != JOptionPane.OK_OPTION) {
        return null;
      }
      List<String> keptIds = new ArrayList<>();
      for (Map.Entry<String, JCheckBox> entry : checkBoxesById.entrySet()) {
        if (entry.getValue().isSelected()) {
          keptIds.add(entry.getKey());
        }
      }
      if (!keptIds.isEmpty()) {
        return keptIds;
      }
      JOptionPane.showMessageDialog(
          parent,
          "Keep at least one destination ticket.",
          warningTitle,
          JOptionPane.WARNING_MESSAGE);
    }
  }

  static FormConfirmDialog.ResizableForm fixedForm(JComponent component) {
    return new FormConfirmDialog.ResizableForm() {
      @Override
      public JComponent component() {
        return component;
      }

      @Override
      public void onLayoutChange(Runnable repackDialog) {
        // Fixed panels do not need dynamic re-packing.
      }
    };
  }

  static PlayerSnapshot activePlayer(GameSnapshot snapshot) {
    return snapshot.players().stream()
        .filter(player -> player.id().equals(snapshot.currentPlayerId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("snapshot has no active player"));
  }

  static String displayName(CardColor cardColor) {
    String lowerCase = cardColor.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
  }

  static Color cardColor(CardColor cardColor) {
    return switch (cardColor) {
      case BLUE -> new Color(35, 95, 168);
      case GREEN -> new Color(35, 124, 73);
      case BLACK -> new Color(46, 46, 46);
      case PINK -> new Color(203, 65, 139);
      case YELLOW -> new Color(236, 198, 82);
      case ORANGE -> new Color(225, 119, 42);
      case BUS -> new Color(118, 74, 162);
    };
  }

  static Color playerColor(PlayerColor playerColor) {
    return switch (playerColor) {
      case RED -> new Color(185, 42, 42);
      case WHITE -> new Color(92, 92, 92);
      case BLUE -> new Color(38, 88, 176);
      case YELLOW -> new Color(165, 130, 28);
    };
  }

  static Color textColorFor(Color background) {
    double luminance =
        (0.299 * background.getRed()
                + 0.587 * background.getGreen()
                + 0.114 * background.getBlue())
            / 255.0;
    return luminance > 0.58 ? Color.BLACK : Color.WHITE;
  }
}
