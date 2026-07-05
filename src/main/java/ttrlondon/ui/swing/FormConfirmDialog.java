package ttrlondon.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Modal OK/Cancel dialog that re-packs when its form content changes size.
 *
 * <p>{@link JOptionPane} caches dialog dimensions on first display, which breaks forms whose
 * height changes while open (for example when a player-count spinner reveals more rows). This
 * helper keeps dialog sizing in sync with the form's preferred size.
 */
final class FormConfirmDialog {
  private FormConfirmDialog() {}

  /**
   * Shows a resizable confirm dialog.
   *
   * @param parent parent component used for dialog ownership and placement
   * @param title dialog title
   * @param form form content that may change size while the dialog is visible
   * @return {@link JOptionPane#OK_OPTION} or {@link JOptionPane#CANCEL_OPTION}
   */
  static int showConfirmDialog(Component parent, String title, ResizableForm form) {
    return showConfirmDialog(parent, title, form, DialogSizing.APP_SHELL);
  }

  /**
   * Shows a compact resizable confirm dialog for in-game prompts.
   *
   * @param parent parent component used for dialog ownership and placement
   * @param title dialog title
   * @param form form content that may change size while the dialog is visible
   * @return {@link JOptionPane#OK_OPTION} or {@link JOptionPane#CANCEL_OPTION}
   */
  static int showCompactConfirmDialog(Component parent, String title, ResizableForm form) {
    return showConfirmDialog(parent, title, form, DialogSizing.CONTENT);
  }

  private static int showConfirmDialog(
      Component parent, String title, ResizableForm form, DialogSizing sizing) {
    Frame owner = resolveOwnerFrame(parent);
    JDialog dialog = new JDialog(owner, title, true);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    AtomicBoolean confirmed = new AtomicBoolean(false);
    JPanel content = new JPanel(new GridBagLayout());
    content.setBackground(AppWindowStyle.APP_BACKGROUND);
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    JPanel formShell = new JPanel(new BorderLayout(0, 18));
    formShell.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new java.awt.Color(190, 170, 145)),
        BorderFactory.createEmptyBorder(22, 28, 22, 28)));
    formShell.setBackground(AppWindowStyle.PANEL_BACKGROUND);
    formShell.add(form.component(), BorderLayout.CENTER);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttons.setOpaque(false);
    JButton okButton = new JButton("OK");
    okButton.addActionListener(event -> {
      confirmed.set(true);
      dialog.dispose();
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(event -> dialog.dispose());
    buttons.add(okButton);
    buttons.add(cancelButton);
    formShell.add(buttons, BorderLayout.SOUTH);
    content.add(formShell, centeredConstraints());
    dialog.setContentPane(content);
    sizing.applyInitialSize(dialog);

    Runnable repackDialog =
        () -> {
          dialog.pack();
          sizing.applyPackedSize(dialog);
          dialog.setLocationRelativeTo(parent);
        };
    form.onLayoutChange(repackDialog);
    repackDialog.run();

    dialog.setVisible(true);
    return confirmed.get() ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;
  }

  private enum DialogSizing {
    APP_SHELL {
      @Override
      void applyInitialSize(JDialog dialog) {
        dialog.setMinimumSize(AppWindowStyle.MINIMUM_APP_WINDOW_SIZE);
        dialog.setPreferredSize(AppWindowStyle.APP_WINDOW_SIZE);
      }

      @Override
      void applyPackedSize(JDialog dialog) {
        dialog.setSize(AppWindowStyle.APP_WINDOW_SIZE);
      }
    },
    CONTENT {
      @Override
      void applyInitialSize(JDialog dialog) {
        dialog.setMinimumSize(new Dimension(360, 220));
      }

      @Override
      void applyPackedSize(JDialog dialog) {
        Dimension preferredSize = dialog.getPreferredSize();
        dialog.setSize(
            Math.max(preferredSize.width, 360),
            Math.max(preferredSize.height, 220));
      }
    };

    abstract void applyInitialSize(JDialog dialog);

    abstract void applyPackedSize(JDialog dialog);
  }

  private static GridBagConstraints centeredConstraints() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.CENTER;
    return constraints;
  }

  private static Frame resolveOwnerFrame(Component parent) {
    if (parent instanceof Frame frame) {
      return frame;
    }
    if (parent == null) {
      return null;
    }
    Component ancestor = SwingUtilities.getWindowAncestor(parent);
    if (ancestor instanceof Frame frame) {
      return frame;
    }
    return null;
  }

  /** Form whose preferred size may change while a hosting dialog is open. */
  interface ResizableForm {
    /** Returns the root component displayed in the dialog body. */
    JComponent component();

    /**
     * Registers a callback invoked when the form needs the hosting dialog to re-pack.
     *
     * @param repackDialog action that packs and recentres the dialog
     */
    void onLayoutChange(Runnable repackDialog);
  }
}
