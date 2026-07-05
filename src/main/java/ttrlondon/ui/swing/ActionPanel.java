package ttrlondon.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import ttrlondon.application.commands.ClaimRouteCommand;
import ttrlondon.application.commands.ClaimRoutePayment;
import ttrlondon.application.commands.DrawDestinationTicketsCommand;
import ttrlondon.application.dto.CommandResult;
import ttrlondon.application.dto.DestinationTicketDrawPreview;
import ttrlondon.application.dto.DestinationTicketSnapshot;
import ttrlondon.application.dto.GameSnapshot;
import ttrlondon.application.dto.PlayerSnapshot;
import ttrlondon.application.dto.RouteSnapshot;
import ttrlondon.application.service.GameApplicationService;
import ttrlondon.application.service.GameStateListener;
import ttrlondon.domain.card.CardColor;
import ttrlondon.domain.card.CardPayment;
import ttrlondon.domain.rushhour.RushHourPhase;

/**
 * Holds action controls and translates user intentions into application commands.
 */
public final class ActionPanel extends JPanel implements GameStateListener {
  private final JLabel activePlayerLabel;
  private final JLabel selectedRouteLabel;
  private final JButton endDrawButton;
  private final JButton undoButton;
  private final JButton claimRouteButton;
  private final JButton drawTicketsButton;
  private final GameApplicationService applicationService;
  private final Consumer<CommandResult> resultHandler;
  private GameSnapshot snapshot;
  private String selectedRouteId;

  /**
   * Creates action controls for the three legal turn actions.
   */
  public ActionPanel() {
    this(null, result -> {});
  }

  /**
   * Creates action controls wired to the application service.
   *
   * @param applicationService application entry point for executing commands
   * @param resultHandler callback for displaying command results
   */
  public ActionPanel(
      GameApplicationService applicationService, Consumer<CommandResult> resultHandler) {
    super(new GridBagLayout());
    this.applicationService = applicationService;
    this.resultHandler = Objects.requireNonNull(resultHandler, "resultHandler");
    setBorder(UiSupport.titledBorder("Actions"));
    activePlayerLabel = new JLabel();
    selectedRouteLabel = new JLabel("No route selected");
    selectedRouteLabel.setToolTipText(selectedRouteLabel.getText());
    endDrawButton = createButton("End Draw");
    undoButton = createButton("Undo");
    claimRouteButton = createButton("Claim Route");
    drawTicketsButton = createButton("Draw Tickets");
    add(activePlayerLabel, actionConstraints(0, 0, 0.0));
    add(selectedRouteLabel, actionConstraints(1, 0, 1.0));
    add(createButtonPanel(), actionButtonConstraints());
    wireActions();
  }

  /**
   * Refreshes button enabled state from the latest snapshot.
   *
   * @param snapshot updated game state
   */
  @Override
  public void onGameStateChanged(GameSnapshot snapshot) {
    this.snapshot = snapshot;
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    activePlayerLabel.setText(activePlayer.name() + ":");
    endDrawButton.setEnabled(
        applicationService != null
            && snapshot.transportDrawActionActive()
            && snapshot.transportDrawsTaken() > 0);
    undoButton.setEnabled(applicationService != null && snapshot.canUndo());
    refreshClaimRouteButton(snapshot);
    drawTicketsButton.setEnabled(
        canStartTurnAction(snapshot) && snapshot.destinationTicketDeckSize() > 0);
    updateSelectedRouteLabel(snapshot);
  }

  /**
   * Records the route selected on the board for a later route-claim command.
   *
   * @param routeId selected route identifier
   */
  public void selectRoute(String routeId) {
    selectedRouteId = Objects.requireNonNull(routeId, "routeId");
    if (snapshot != null) {
      updateSelectedRouteLabel(snapshot);
      refreshClaimRouteButton(snapshot);
    }
  }

  private boolean canStartTurnAction(GameSnapshot snapshot) {
    return applicationService != null
        && snapshot.acceptsPlayerActions()
        && !snapshot.transportDrawActionActive();
  }

  private void refreshClaimRouteButton(GameSnapshot snapshot) {
    claimRouteButton.setEnabled(canStartTurnAction(snapshot) && selectedRouteId != null);
  }

  private JButton createButton(String text) {
    JButton button = new JButton(text);
    button.setEnabled(false);
    button.setFocusable(false);
    return button;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    buttonPanel.add(endDrawButton);
    buttonPanel.add(undoButton);
    buttonPanel.add(claimRouteButton);
    buttonPanel.add(drawTicketsButton);
    return buttonPanel;
  }

  private void updateSelectedRouteLabel(GameSnapshot snapshot) {
    String routeText = selectedRouteText(snapshot);
    selectedRouteLabel.setText(routeText);
    selectedRouteLabel.setToolTipText(routeText);
  }

  private void wireActions() {
    endDrawButton.addActionListener(event -> endDrawAction());
    undoButton.addActionListener(event -> undoLastTurn());
    claimRouteButton.addActionListener(event -> claimSelectedRoute());
    drawTicketsButton.addActionListener(event -> drawDestinationTickets());
  }

  private void endDrawAction() {
    if (applicationService == null) {
      return;
    }
    resultHandler.accept(applicationService.endTransportCardDrawAction());
  }

  private void undoLastTurn() {
    if (applicationService == null) {
      return;
    }
    selectedRouteId = null;
    resultHandler.accept(applicationService.undoLastTurn());
  }

  private void claimSelectedRoute() {
    if (applicationService == null || snapshot == null || selectedRouteId == null) {
      return;
    }
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    List<CardColor> paymentCards = promptForPayment(activePlayer, "Select Route Payment Cards");
    if (paymentCards == null) {
      return;
    }
    List<CardColor> detourCards = promptForDetourPaymentIfNeeded(activePlayer);
    if (detourCards == null) {
      return;
    }
    CommandResult result =
        applicationService.executeCommand(
            new ClaimRouteCommand(
                activePlayer.id(),
                selectedRouteId,
                new ClaimRoutePayment(
                    new CardPayment(paymentCards), new CardPayment(detourCards))));
    if (result.isSuccess()) {
      selectedRouteId = null;
    }
    resultHandler.accept(result);
  }

  private List<CardColor> promptForPayment(PlayerSnapshot activePlayer, String title) {
    JPanel form = new JPanel(new GridBagLayout());
    Map<CardColor, JSpinner> spinners = new EnumMap<>(CardColor.class);
    Map<CardColor, Integer> counts = handCounts(activePlayer);
    int row = 0;
    RouteSnapshot selectedRoute = selectedRoute(snapshot);
    if (selectedRoute != null && selectedRoute.isFerry()) {
      form.add(
          new JLabel("Ferry: " + selectedRoute.requiredBusSymbols() + " Bus symbol(s)"),
          constraints(0, row, 2));
      row++;
    }
    for (CardColor cardColor : CardColor.values()) {
      GridBagConstraints labelConstraints = constraints(0, row);
      form.add(new JLabel(UiSupport.displayName(cardColor)), labelConstraints);

      int available = counts.getOrDefault(cardColor, 0);
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, available, 1));
      spinners.put(cardColor, spinner);
      form.add(spinner, constraints(1, row));
      row++;
    }

    int option =
        FormConfirmDialog.showCompactConfirmDialog(
            this, title, UiSupport.fixedForm(form));
    if (option != JOptionPane.OK_OPTION) {
      return null;
    }

    List<CardColor> cards = new ArrayList<>();
    for (CardColor cardColor : CardColor.values()) {
      int count = (Integer) spinners.get(cardColor).getValue();
      for (int index = 0; index < count; index++) {
        cards.add(cardColor);
      }
    }
    return cards;
  }

  private List<CardColor> promptForDetourPaymentIfNeeded(PlayerSnapshot activePlayer) {
    if (snapshot.rushHourPhase() != RushHourPhase.PEAK
        || !snapshot.rushHourAffectedRouteIds().contains(selectedRouteId)) {
      return List.of();
    }
    return promptForPayment(activePlayer, "Select 1 Rush Hour Detour Card");
  }

  private void drawDestinationTickets() {
    if (applicationService == null || snapshot == null) {
      return;
    }
    PlayerSnapshot activePlayer = UiSupport.activePlayer(snapshot);
    DestinationTicketDrawPreview preview =
        applicationService.previewDestinationTickets(activePlayer.id());
    if (preview.commandResult().isFailure()) {
      resultHandler.accept(preview.commandResult());
      return;
    }
    List<String> keptTicketIds = promptForKeptTickets(preview.tickets());
    if (keptTicketIds == null) {
      return;
    }
    CommandResult result =
        applicationService.executeCommand(
            new DrawDestinationTicketsCommand(activePlayer.id(), keptTicketIds));
    resultHandler.accept(result);
  }

  private List<String> promptForKeptTickets(List<DestinationTicketSnapshot> tickets) {
    JPanel form = new JPanel(new GridBagLayout());
    form.setOpaque(false);
    Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    for (int index = 0; index < tickets.size(); index++) {
      DestinationTicketSnapshot ticket = tickets.get(index);
      JCheckBox checkBox = new JCheckBox(destinationTicketLabel(ticket), true);
      checkBox.setOpaque(false);
      checkBoxes.put(ticket.id(), checkBox);
      form.add(checkBox, ticketChoiceConstraints(index));
    }

    return UiSupport.chooseKeptTickets(
        this, form, "Choose Destination Tickets", "Destination Tickets", checkBoxes);
  }

  private String destinationTicketLabel(DestinationTicketSnapshot ticket) {
    return "<html><b>"
        + ticket.points()
        + " points</b>: "
        + ticket.locationADisplayName()
        + " to "
        + ticket.locationBDisplayName()
        + "</html>";
  }

  private String selectedRouteText(GameSnapshot snapshot) {
    if (selectedRouteId == null) {
      return "No route selected";
    }
    RouteSnapshot selectedRoute = selectedRoute(snapshot);
    if (selectedRoute == null) {
      return "Selected route: " + selectedRouteId;
    }
    String claimState = selectedRoute.claimedBy().isPresent() ? "claimed" : "open";
    String ferryText =
        selectedRoute.isFerry() ? ", ferry " + selectedRoute.requiredBusSymbols() + " Bus" : "";
    return "Selected: "
        + selectedRoute.id()
        + " ("
        + selectedRoute.length()
        + ", "
        + selectedRoute.color()
        + ferryText
        + ", "
        + claimState
        + ")";
  }

  private RouteSnapshot selectedRoute(GameSnapshot snapshot) {
    return snapshot.routes().stream()
        .filter(route -> route.id().equals(selectedRouteId))
        .findFirst()
        .orElse(null);
  }

  private Map<CardColor, Integer> handCounts(PlayerSnapshot activePlayer) {
    Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
    for (CardColor cardColor : activePlayer.handCards()) {
      counts.merge(cardColor, 1, Integer::sum);
    }
    return counts;
  }

  private GridBagConstraints constraints(int x, int y) {
    return constraints(x, y, 1);
  }

  private GridBagConstraints constraints(int x, int y, int gridWidth) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = gridWidth;
    constraints.insets = new Insets(4, 4, 4, 4);
    constraints.anchor = GridBagConstraints.WEST;
    return constraints;
  }

  private GridBagConstraints actionConstraints(int x, int y, double weightx) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.insets = new Insets(4, 4, 4, 4);
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = weightx;
    return constraints;
  }

  private GridBagConstraints actionButtonConstraints() {
    GridBagConstraints constraints = actionConstraints(0, 1, 1.0);
    constraints.gridwidth = 2;
    return constraints;
  }

  private GridBagConstraints ticketChoiceConstraints(int row) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(4, 0, 4, 0);
    return constraints;
  }
}
