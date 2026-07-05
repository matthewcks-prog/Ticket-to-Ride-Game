package ttrlondon.ui.swing;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.RushHourEventSnapshot;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.domain.rushhour.RushHourPhase;

/**
 * Displays Rush Hour event state from immutable snapshots.
 */
public final class RushHourPanel extends JPanel implements GameStateListener {
  private final JLabel phaseLabel;
  private final JLabel eventLabel;
  private final JLabel detailLabel;
  private final JLabel legendLabel;

  /** Creates a Rush Hour status panel. */
  public RushHourPanel() {
    super(new GridLayout(0, 1, 4, 4));
    setBorder(UiSupport.titledBorder("Rush Hour"));
    phaseLabel = new JLabel();
    eventLabel = new JLabel();
    detailLabel = new JLabel();
    legendLabel = new JLabel("Red-bordered routes are affected by Rush Hour.");
    add(phaseLabel);
    add(eventLabel);
    add(detailLabel);
    add(legendLabel);
  }

  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    phaseLabel.setText(snapshot.rushHourPhase() + " - " + snapshot.rushHourTurnsRemaining() + " turns");
    RushHourEventSnapshot event =
        snapshot.rushHourPhase() == RushHourPhase.PEAK
            ? snapshot.activeRushHourEvent().orElse(null)
            : snapshot.forecastRushHourEvent().orElse(null);
    if (event == null) {
      eventLabel.setText("No event");
      detailLabel.setText("Affected routes: 0");
      return;
    }
    eventLabel.setText(event.title());
    detailLabel.setText(
        "Affected routes: "
            + snapshot.rushHourAffectedRouteIds().size()
            + ", detour +"
            + event.extraCardCost()
            + ", bonus +"
            + event.bonusPoints());
  }
}
