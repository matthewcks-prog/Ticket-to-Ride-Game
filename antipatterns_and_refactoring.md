# Design Anti-Patterns and Applied Refactoring Techniques

This is the Sprint 3 Object-Oriented Design deliverable: *"Identified design antipatterns from
Sprint 2 and applied refactoring techniques"* (`sprint_3_specifications.md`, Section 2.1).

The primary focus is **Part A**: anti-patterns that were *still present in the Sprint 2 codebase
after the MVP was completed*. These were identified during the Sprint 3 design review and
**refactored now**, before the Sprint 3 extensions are built, with before/after code from the live
codebase.

**Part B** is kept for completeness: anti-patterns that were identified and refactored *during*
Sprint 2 development, with git-backed evidence.

Every refactoring is behaviour-preserving (no game rule changed). After all Part A changes the full
suite (**57 tests**) passes with **0 Checkstyle violations** (`mvn verify`; see
`docs/progress_tracker.md`). The architecture boundary test still passes, confirming no
`javax.swing`/`java.awt` leaked into `domain`/`application`/`infrastructure` while rules were moved
into the domain. The evidence format follows `docs/design/refactoring-antipatterns-guide.md`.

---

# Part A — Anti-Patterns Still Present After the Sprint 2 MVP (Refactored Now)

## A1 — Feature Envy: the application layer reaching into the card subsystem

| Field | Detail |
|---|---|
| **Anti-pattern** | Feature Envy (and cross-class Duplicated Code): application code repeatedly asked the card classes for their raw data, then made decisions that belong to those classes. |
| **Location** | `GameApplicationService.isFaceUpBus(int)` and `canDrawAnotherTransportationCard()` looped over `game.faceUpDisplay().visibleCards()` and inspected slots; `GameApplicationService` and `DrawTransportCardCommand` *both* re-derived "a card can be drawn" as `transportCardDeck().drawPileSize() > 0 \|\| transportCardDeck().discardPileSize() > 0`. |
| **Refactoring applied** | Move Method to the information experts (`TransportCardDeck.canDraw()`, `FaceUpDisplay.isBusAt(int)`, `FaceUpDisplay.hasNonBusCardOutsideSlot(int)`), then Inline Method on the now-trivial private wrapper. |
| **Proof** | Code below; new unit tests in `CardDeckAndFaceUpDisplayTest` (`canDrawReflectsDrawAndDiscardPileContents`, `isBusAtChecksSlotContentsAndBounds`, `hasNonBusCardOutsideSlotIgnoresExcludedSlotAndBusCards`). |

### Before

```java
// application/service/GameApplicationService.java (before)
private boolean canDrawAnotherTransportationCard() {
  if (!drawActionState.isActive() || drawActionState.drawsTaken() >= 2) {
    return false;
  }
  if (game.transportCardDeck().drawPileSize() > 0
      || game.transportCardDeck().discardPileSize() > 0) {     // duplicated deck rule
    return true;
  }
  for (int index = 0; index < game.faceUpDisplay().visibleCards().size(); index++) {
    if (index == drawActionState.lockedFaceUpIndex()) {        // envies FaceUpDisplay internals
      continue;
    }
    if (game.faceUpDisplay().visibleCards().get(index) != CardColor.BUS) {
      return true;
    }
  }
  return false;
}

private boolean isFaceUpBus(int index) {
  List<CardColor> visibleCards = game.faceUpDisplay().visibleCards();
  return index >= 0 && index < visibleCards.size() && visibleCards.get(index) == CardColor.BUS;
}

// application/commands/DrawTransportCardCommand.java (before) — the same deck rule, duplicated
private static boolean canDrawBlind(Game game) {
  return game.transportCardDeck().drawPileSize() > 0
      || game.transportCardDeck().discardPileSize() > 0;
}
```

### Why it is problematic in our design

- **Misplaced responsibility.** Whether a card can be drawn is `TransportCardDeck`'s rule (it owns
  the draw and discard piles and the reshuffle logic). Whether a slot holds a Bus card, or whether a
  drawable non-Bus card remains, is `FaceUpDisplay`'s rule. The application service was making these
  decisions *for* them by pulling their raw lists across the boundary.
- **Broken encapsulation / high coupling.** `FaceUpDisplay.visibleCards()` returns an unmodifiable
  list, but the service depended on its *indexed structure* and re-implemented Bus detection, so the
  card classes could not change their internal representation without breaking the service.
- **Duplicated rule.** The deck-affordance expression existed verbatim in two layers; a change to the
  reshuffle rule (relevant once the undo extension rewinds the deck) would need two edits and could
  drift.

### After

The rules now live with the data:

```46:48:src/main/java/ttrlondon/domain/card/TransportCardDeck.java
  public boolean canDraw() {
    return !drawPile.isEmpty() || !discardPile.isEmpty();
  }
```

```97:120:src/main/java/ttrlondon/domain/card/FaceUpDisplay.java
  public boolean isBusAt(int index) {
    return index >= 0 && index < visibleCards.size() && visibleCards.get(index) == CardColor.BUS;
  }

  // ...

  public boolean hasNonBusCardOutsideSlot(int excludedIndex) {
    for (int index = 0; index < visibleCards.size(); index++) {
      if (index == excludedIndex) {
        continue;
      }
      if (visibleCards.get(index) != CardColor.BUS) {
        return true;
      }
    }
    return false;
  }
```

The service now composes those queries; the Bus-slot wrapper was inlined:

```234:240:src/main/java/ttrlondon/application/service/GameApplicationService.java
  private boolean canDrawAnotherTransportationCard() {
    if (!drawActionState.isActive() || drawActionState.drawsTaken() >= 2) {
      return false;
    }
    return game.transportCardDeck().canDraw()
        || game.faceUpDisplay().hasNonBusCardOutsideSlot(drawActionState.lockedFaceUpIndex());
  }
```

`DrawTransportCardCommand` now calls `game.transportCardDeck().canDraw()` instead of its own copy.

### Improvement
Cohesion rises (card-supply rules live once, on the classes that own the cards); coupling drops (the
application layer no longer iterates the face-up list or re-derives the deck rule); readability rises
(`canDrawAnotherTransportationCard()` reads as an intention); and the rules are now unit-tested
directly on the domain classes.

## A2 — Long Parameter List + Data Clump at the snapshot boundary

| Field | Detail |
|---|---|
| **Anti-pattern** | Long Parameter List with an embedded Data Clump: four draw-action values always travelled together. |
| **Location** | `GameSnapshot` had a **17-parameter** constructor; the values `transportDrawActionActive`, `transportDrawActionPlayerId`, `transportDrawsTaken`, and `lockedFaceUpIndex` were passed individually from `GameApplicationService.getSnapshot()` through `GameSnapshot.from(game, boolean, String, int, int)`. |
| **Refactoring applied** | Introduce Parameter Object (`application/dto/TransportDrawProgress`). |
| **Proof** | Code below; the public draw-state getters are unchanged, so no UI/test call site needed editing. |

### Before

```java
// application/dto/GameSnapshot.java (before) — 17-parameter constructor
public GameSnapshot(
    GamePhase phase, String currentPlayerId, List<String> playerOrder,
    List<PlayerSnapshot> players, List<LocationSnapshot> locations, List<RouteSnapshot> routes,
    List<CardColor> faceUpCards, int transportDrawPileSize, int transportDiscardPileSize,
    int destinationTicketDeckSize, boolean finalRoundActive, String triggeringPlayerId,
    int finalTurnsRemaining,
    boolean transportDrawActionActive, String transportDrawActionPlayerId,
    int transportDrawsTaken, int lockedFaceUpIndex) { /* ... */ }

// application/service/GameApplicationService.java (before)
public GameSnapshot getSnapshot() {
  return GameSnapshot.from(
      game,
      drawActionState.isActive(),
      drawActionState.playerId(),
      drawActionState.drawsTaken(),
      drawActionState.lockedFaceUpIndex());
}
```

### Why it is problematic in our design

- **The four draw-action values are one concept.** They are produced together, consumed together,
  and meaningless apart ("locked slot" only matters when an action is "active"). Passing them as
  loose positional parameters is a Data Clump.
- **Long Parameter List is error-prone.** A 17-parameter constructor with adjacent
  `boolean`/`String`/`int` arguments is easy to mis-order, and the compiler cannot catch a swapped
  `int`.
- **Extensibility friction.** The Sprint 3 Rush Hour extension and undo mechanism will add more
  per-action state to the snapshot; growing an already-overlong parameter list compounds the smell.

### After

A focused, immutable value object groups the clump (the four values, the shared `NO_LOCKED_SLOT`
sentinel, and an `inactive()` factory):

```12:19:src/main/java/ttrlondon/application/dto/TransportDrawProgress.java
public final class TransportDrawProgress {
  /** Sentinel value meaning no face-up slot is locked. */
  public static final int NO_LOCKED_SLOT = -1;

  private final boolean active;
  private final String playerId;
  private final int drawsTaken;
  private final int lockedFaceUpIndex;
```

`GameSnapshot` now takes one parameter instead of four (17 → 14) and delegates its getters, so the
UI read-model API is unchanged:

```106:106:src/main/java/ttrlondon/application/dto/GameSnapshot.java
  public static GameSnapshot from(Game game, TransportDrawProgress drawProgress) {
```

```96:98:src/main/java/ttrlondon/application/service/GameApplicationService.java
  public GameSnapshot getSnapshot() {
    return GameSnapshot.from(game, drawActionState.toProgress());
  }
```

### Improvement
Readability and safety rise (the snapshot's draw state is one named, type-safe argument that cannot
be mis-ordered); cohesion rises (the four values, their `inactive()` default, and the
`NO_LOCKED_SLOT` sentinel live in one place — the service's `DrawActionState.NO_LOCKED_SLOT` now
references the shared constant); and Rush Hour/undo can extend `TransportDrawProgress` without
touching the `GameSnapshot` constructor or any caller. The change is backwards compatible: the
public getters still exist and delegate, so no Swing panel or test changed.

## A3 — Duplicated Code / Divergent-Change risk in `ActionPanel`

| Field | Detail |
|---|---|
| **Anti-pattern** | Duplicated Code: the "can the active player start a turn action / claim the selected route" predicate was computed in two methods, inviting Divergent Change. |
| **Location** | `ui/swing/ActionPanel.onGameStateChanged(...)` and `ActionPanel.selectRoute(...)` each rebuilt `applicationService != null && snapshot.acceptsPlayerActions() && !snapshot.transportDrawActionActive()` and set `claimRouteButton`'s enabled state. |
| **Refactoring applied** | Extract Method (`canStartTurnAction(snapshot)` and `refreshClaimRouteButton(snapshot)`). |

### Before

```java
// onGameStateChanged(...)
boolean canStartTurnAction =
    applicationService != null
        && snapshot.acceptsPlayerActions()
        && !snapshot.transportDrawActionActive();
claimRouteButton.setEnabled(canStartTurnAction && selectedRouteId != null);

// selectRoute(...)
claimRouteButton.setEnabled(
    applicationService != null
        && snapshot.acceptsPlayerActions()
        && !snapshot.transportDrawActionActive());   // had already begun to diverge (no id guard)
```

### Why it is problematic in our design
The two copies had already begun to diverge (`selectRoute` omitted the `selectedRouteId != null`
guard). Any future rule change — for example, also disabling claims during a Rush Hour peak gate —
would have to be made consistently in both places, which is exactly the Divergent Change that
duplication causes.

### After

```113:121:src/main/java/ttrlondon/ui/swing/ActionPanel.java
  private boolean canStartTurnAction(GameSnapshot snapshot) {
    return applicationService != null
        && snapshot.acceptsPlayerActions()
        && !snapshot.transportDrawActionActive();
  }

  private void refreshClaimRouteButton(GameSnapshot snapshot) {
    claimRouteButton.setEnabled(canStartTurnAction(snapshot) && selectedRouteId != null);
  }
```

### Improvement
The enable rule has one definition; the two call sites can no longer drift. Intent is named; and
`selectRoute` now also honours the `selectedRouteId != null` guard, removing the latent
inconsistency for free while preserving observable behaviour.

## A4 — Duplicated Code across Swing: the destination-ticket chooser dialog

| Field | Detail |
|---|---|
| **Anti-pattern** | Duplicated Code across two unrelated Swing classes. |
| **Location** | `ActionPanel.promptForKeptTickets(...)` (in-game ticket draw) and `GameSetupDialog.promptForPlayerTickets(...)` (initial setup) both built a checkbox map keyed by ticket id, ran an identical "re-prompt until at least one ticket is kept" confirm loop, and each declared its own identical `selectedTicketIds(Map<String, JCheckBox>)` helper. |
| **Refactoring applied** | Extract Method into the shared `UiSupport.chooseKeptTickets(...)` helper. |

### Before (both classes contained this loop, plus a duplicate `selectedTicketIds`)

```java
while (true) {
  int option = JOptionPane.showConfirmDialog(
      parent, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
  if (option != JOptionPane.OK_OPTION) {
    return null;
  }
  List<String> keptIds = selectedTicketIds(checkBoxes);
  if (!keptIds.isEmpty()) {
    return keptIds;
  }
  JOptionPane.showMessageDialog(parent, "Keep at least one destination ticket.", title,
      JOptionPane.WARNING_MESSAGE);
}
```

### Why it is problematic in our design
The destination-ticket "keep at least one" interaction is a single rule presented in two places. With
two copies, a change (e.g. the Sprint 3 undo flow re-using ticket selection, or a wording change)
must be applied twice and can diverge. It also bloats two already-large Swing classes with the same
control-flow.

### After

The loop lives once in `UiSupport`:

```38:66:src/main/java/ttrlondon/ui/swing/UiSupport.java
  static List<String> chooseKeptTickets(
      Component parent,
      Component form,
      String dialogTitle,
      String warningTitle,
      Map<String, JCheckBox> checkBoxesById) {
    while (true) {
      int option =
          JOptionPane.showConfirmDialog(
              parent, form, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
```

Both callers now build only their checkbox panel and delegate the loop, for example
`ui/swing/ActionPanel.java` line 250 and `ui/swing/GameSetupDialog.java` line 106.

### Improvement
The selection rule has one home; both Swing classes shrank and lost their duplicate
`selectedTicketIds`; and the keep-at-least-one behaviour is guaranteed identical in both flows.

## A5 — Duplicated Code: a validation helper copied into 15+ classes

| Field | Detail |
|---|---|
| **Anti-pattern** | Duplicated Code (the most widespread instance in the codebase): the private helper `requireText(value, fieldName)` was copy-pasted into ~15 classes across the `domain`, `application`, and `ui` layers, and `normalizeOptionalText(value)` into four more. |
| **Location** | `Player`, `Route`, `Location`, `DestinationTicket`, `GameSnapshot`, `CommandResult`, `LocationSnapshot`, `RouteSnapshot`, `PlayerSnapshot`, `DestinationTicketSnapshot`, `FinalScore`, `FinalScoreSnapshot`, `ClaimRouteCommand`, `DrawTransportCardCommand`, `DrawDestinationTicketsCommand`, `BoardLocationViewModel`, `BoardRouteViewModel`. |
| **Refactoring applied** | Extract Class — a single shared utility `ttrlondon.domain.common.Text` with `requireNonBlank(...)` and `normalizeOptional(...)`. |

### Before (repeated verbatim, with two slightly inconsistent variants)

```java
// Variant in the rich domain entities (Player, Route, Location, DestinationTicket)
private static String requireText(String value, String fieldName) {
  if (value == null || value.isBlank()) {
    throw new IllegalArgumentException(fieldName + " must not be blank");
  }
  return value;
}

// Variant in DTOs/commands (CommandResult, snapshots, commands) — differs for null handling
private static String requireText(String value, String fieldName) {
  Objects.requireNonNull(value, fieldName);
  if (value.isBlank()) {
    throw new IllegalArgumentException(fieldName + " must not be blank");
  }
  return value;
}
```

### Why it is problematic in our design

- **Pure copy-paste duplication at scale.** Nineteen identical (or near-identical) private methods
  encode one decision: "this identifier/label must not be blank." This is the textbook Duplicated
  Code smell and the kind of repetition the Sprint 2 review specifically looks for.
- **Inconsistency had already crept in.** Two variants existed — one threw `IllegalArgumentException`
  for a null value, the other threw `NullPointerException` — so the *same* invariant behaved
  differently depending on which class you constructed.
- **Change amplification.** Improving the message, trimming input, or logging a validation failure
  would require touching 15+ files.

### After

One small, well-documented helper owns the rule:

```20:25:src/main/java/ttrlondon/domain/common/Text.java
  public static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
```

Every former call site now reads `Text.requireNonBlank(id, "id")` /
`Text.normalizeOptional(claimedBy)`, and the 19 private helpers were deleted. It lives in
`domain.common` so domain, application, and UI may all depend on it without violating the
`ui -> application -> domain` direction.

### Improvement
- **Duplication eliminated:** one definition replaces ~19 copies.
- **Consistency:** null-or-blank identifiers now fail uniformly with `IllegalArgumentException`
  everywhere (a deliberate unification of the two prior variants; no test distinguished them and the
  full suite still passes).
- **Maintainability:** the validation rule has a single point of change, and each class shrank to its
  real responsibility.

---

# Part B — Anti-Patterns Already Refactored During Sprint 2 (kept for completeness)

## B1 — UI-Driven Business Logic + Shotgun Surgery (a duplicated lifecycle rule)

| Field | Detail |
|---|---|
| **Anti-pattern** | UI-Driven Business Logic, expressed as Duplicated Code / Shotgun Surgery across layers. |
| **Location** | The rule *"the game accepts turn actions only in `RUNNING` or `FINAL_ROUND`"* was copied as a private `acceptsPlayerActions(GamePhase)` method in three command classes **and** `ui/swing/ActionPanel`. |
| **Refactoring applied** | Move Method to `Game`, plus Introduce Read-Model Query (`GameSnapshot.acceptsPlayerActions()`). |
| **Proof** | Commit `d4c16df`; `docs/adr/ADR-011-aggregate-owned-player-action-eligibility.md`. |

### After

```90:93:src/main/java/ttrlondon/domain/game/Game.java
  /** Returns whether the game is currently accepting player turn actions. */
  public boolean acceptsPlayerActions() {
    return phase == GamePhase.RUNNING || phase == GamePhase.FINAL_ROUND;
  }
```

Improvement: coupling drops (the UI no longer imports `domain.game.GamePhase`), the rule has a single
source of truth, and a new action-accepting phase is a one-line change — de-risking the Sprint 3
undo and Rush Hour work.

## B2 — Feature Envy / Duplicated Player Lookup

| Field | Detail |
|---|---|
| **Anti-pattern** | Feature Envy expressed as duplicated player-lookup loops and null-driven control flow. |
| **Location** | `findPlayerOrNull(Game, String)` iterated `game.players()` inside three command classes. |
| **Refactoring applied** | Move Method (`Game.findPlayer`) + Replace Null with `Optional`. |
| **Proof** | Commit `d4c16df`; ADR-011. |

### After

```128:136:src/main/java/ttrlondon/domain/game/Game.java
  public Optional<Player> findPlayer(String playerId) {
    Objects.requireNonNull(playerId, "playerId");
    for (Player player : players) {
      if (player.id().equals(playerId)) {
        return Optional.of(player);
      }
    }
    return Optional.empty();
  }
```

Improvement: cohesion and encapsulation improve (`Game` may change its storage freely), and
`Optional` removes the duplicated null checks.

## B3 — Long Method with a Cryptic, Low-Diagnosability Conditional Cascade

| Field | Detail |
|---|---|
| **Anti-pattern** | Long Method tending toward an Obscured Result: one validation method mixed rule evaluation with message construction, collapsing distinct failures into one vague string. |
| **Location** | `ClaimRouteCommand.validate(Game)` — payment and double-route branches. |
| **Refactoring applied** | Extract Method (`paymentFailureMessage`, `doubleRouteFailureMessage`). |
| **Proof** | Commit `e779588`. |

### After

```126:144:src/main/java/ttrlondon/application/commands/ClaimRouteCommand.java
  private static String paymentFailureMessage(Route route, CardPayment payment) {
    if (payment.size() != route.length()) {
      return "Payment must contain exactly "
          + route.length()
          + " cards for this route, but "
          + payment.size()
          + " were selected.";
    }
    if (!payment.hasSingleNonBusColor()) {
      return "Payment must use one colour set; Bus cards may substitute, but mixed non-Bus "
          + "colours are not allowed.";
    }
    if (route.color() != RouteColor.GREY) {
      return "Payment must match the route colour "
          + route.color()
          + ", using Bus cards only as wild substitutions.";
    }
    return "Payment does not satisfy this route's card requirement.";
  }
```

Improvement: `validate(...)` reads as flat guard clauses; each failure family is cohesive and gives a
precise, testable message — exactly what the Sprint 3 "clear rejection messages" direction needs.

---

# Summary

| # | Anti-pattern | Location | Refactoring technique | Status | Quality improved |
|---|---|---|---|---|---|
| A1 | Feature Envy on card subsystem | `GameApplicationService` + `DrawTransportCardCommand` | Move Method + Inline Method | **Present after Sprint 2 → refactored now** | Cohesion, coupling, testability |
| A2 | Long Parameter List / Data Clump | `GameSnapshot` 17-arg constructor | Introduce Parameter Object | **Present after Sprint 2 → refactored now** | Readability, safety, extensibility |
| A3 | Duplicated Code / Divergent Change | `ActionPanel` enable logic | Extract Method | **Present after Sprint 2 → refactored now** | Maintainability, consistency |
| A4 | Duplicated Code across Swing | `ActionPanel` + `GameSetupDialog` ticket chooser | Extract Method (shared helper) | **Present after Sprint 2 → refactored now** | Maintainability, reuse |
| A5 | Duplicated Code (15+ copies) | `requireText` across all layers | Extract Class (`Text`) | **Present after Sprint 2 → refactored now** | Duplication removed, consistency |
| B1 | UI-Driven Business Logic / Shotgun Surgery | phase rule in 3 commands + `ActionPanel` | Move Method + Snapshot query | Sprint 2 (`d4c16df`, ADR-011) | Coupling, single source of truth |
| B2 | Feature Envy / duplicated lookup | `findPlayerOrNull` in 3 commands | Move Method + `Optional` | Sprint 2 (`d4c16df`) | Cohesion, encapsulation |
| B3 | Long Method / cryptic result | `ClaimRouteCommand.validate` | Extract Method | Sprint 2 (`e779588`) | Readability, diagnosability |

All Part A refactorings were validated with `mvn verify`: **57 tests pass, 0 Checkstyle violations**,
and `ArchitectureBoundaryTest` still confirms the layer boundaries hold.
