# Ticket to Ride: London — Implementation-Level UML Class Diagram

## 0. Current Audit Status

Audited against `src/main/java` on 2026-06-01. The diagram below has been updated to reflect the current codebase structure and Sprint 3 architecture:

- Ferry routes are represented through `RouteKind`, `Route.requiredBusSymbols`, and `FerryRouteRequirement`.
- Rush Hour events are represented through `RushHourManager`, `RushHourEvent`, `RushHourClaimRule`, `RouteSelector`, `RouteSelectors`, `RushHourPhase`, `RushHourEventFactory`, `RushHourPanel`, and snapshot fields on `GameSnapshot`.
- Undo is represented through application-layer Memento classes: `GameMemento`, `GameMementoFactory`, and `UndoHistory`.
- Transportation-card draw sequencing is represented by `TransportDrawCoordinator`; its private `DrawActionState` no longer lives inside `GameApplicationService`.
- `ShuffleStrategy` is a domain port (`ttrlondon.domain.random`) implemented by infrastructure classes.

## 1. Scope and Omissions

### Included

All architecturally significant classes, interfaces, and enums across the four layers of the implemented system:

- **Domain layer** (`ttrlondon.domain.*`): All core entities, value objects, enums, strategy interfaces, and scoring collaborators. These classes encode the game rules and are the heart of the architecture.
- **Application layer** (`ttrlondon.application.*`): The `GameCommand` interface and its three concrete commands, `ClaimRoutePayment`, the `GameApplicationService` coordinator, `TransportDrawCoordinator`, Undo/Memento support, the `GameStateListener` observer interface, and the key DTOs (`CommandResult`, `GameSnapshot`, `TransportDrawProgress`, `RushHourEventSnapshot`).
- **Infrastructure layer** (`ttrlondon.infrastructure.*`): The factory classes (`GameFactory`, `BoardFactory`, `DeckFactory`, `RushHourEventFactory`) and the random/deterministic implementations of the domain `ShuffleStrategy` port.
- **UI layer** (`ttrlondon.ui.*`): Represented at the boundary only — `MainFrame` as the top-level frame, `BoardPanel` for route selection, `CardMarketPanel` for face-up and blind transportation draws, plus `BoardViewModel` and `BoardRenderer` to show the rendering pipeline. Other Swing panels are not exhaustively modelled because they share the same architectural role (observe snapshots, render state, forward user intent).

### Excluded

Note: the current representative UI boundary also includes `RushHourPanel`, because Rush Hour is an active Sprint 3 feature. The rest of the Swing panels remain intentionally omitted from the diagram for readability.

- **Test classes** (`*Test.java`, `TestGameFactory`): Not part of the production architecture.
- **`package-info.java` files**: Documentation-only, no architectural significance.
- **Trivial getters/setters**: Accessors are omitted unless they reveal meaningful domain behaviour (e.g. `Player.canAfford()` is included; `Player.id()` is omitted).
- **DTO internals**: Snapshot DTOs (`PlayerSnapshot`, `RouteSnapshot`, `LocationSnapshot`, etc.) are noted as dependencies of `GameSnapshot` but not fully expanded — they are thin immutable data carriers with no behaviour.
- **Private nested types**: `DrawActionState` inside `TransportDrawCoordinator`, `SupplyState` inside `FaceUpDisplay`, and `Edge` inside `LongestPathCalculator` are implementation details omitted for clarity.
- **Most UI classes**: `ActionPanel`, `PlayerPanel`, `DestinationTicketPanel`, `GameStatusPanel`, `FinalScoreDialog`, `GameSetupDialog`, `UiSupport`, `BoardPalette`, `RouteRenderer`, `LocationRenderer`, and viewmodel support types (`NormalizedPoint`, `NormalizedOffset`, `BoardLocationViewModel`, `BoardRouteViewModel`) are excluded to keep the diagram focused on architectural structure. They all follow the same Observer-driven pattern shown by `BoardPanel`, `CardMarketPanel`, and `MainFrame`.
- **`Main.java`**: Entry point that wires dependencies; not architecturally significant beyond construction.
- **`GameSetupDraft`**: Infrastructure helper used during game creation; not a persistent domain concept.
- **`DrawTransportCardResult`**, **`DestinationTicketDrawPreview`**, **`FinalScoreSnapshot`**: Application-layer DTOs with no domain behaviour.

### Representation Choices

- The codebase uses traditional Java OOP classes throughout, so standard UML class notation applies directly.
- Multiplicities are inferred from field types (e.g. `List<Player>` → `1..*` or `2..4`), constructor contracts, and domain rules documented in `game_rules.md`.
- Where the code stores a reference as a field, the relationship is modelled as an association or composition. Where a type appears only as a method parameter or return value, the relationship is modelled as a dependency.

---

## 2. UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% ════════════════════════════════════════════
    %% ENUMS AND VALUE OBJECTS
    %% ════════════════════════════════════════════

    class CardColor {
        <<enumeration>>
        BLUE
        GREEN
        BLACK
        PINK
        YELLOW
        ORANGE
        BUS
    }

    class RouteColor {
        <<enumeration>>
        BLUE
        GREEN
        BLACK
        PINK
        YELLOW
        ORANGE
        GREY
    }

    class PlayerColor {
        <<enumeration>>
        RED
        WHITE
        BLUE
        YELLOW
    }

    class GamePhase {
        <<enumeration>>
        SETUP
        RUNNING
        FINAL_ROUND
        SCORING
        GAME_OVER
    }

    class DrawSource {
        <<enumeration>>
        BLIND
        FACE_UP
    }

    class RouteKind {
        <<enumeration>>
        STANDARD
        FERRY
    }

    class RushHourPhase {
        <<enumeration>>
        INACTIVE
        FORECAST
        PEAK
    }

    class CardPayment {
        <<value object>>
        -cards : List~CardColor~
        +size() int
        +count(color : CardColor) int
        +countsByColor() Map~CardColor, Integer~
        +hasSingleNonBusColor() boolean
        +nonBusCardsMatch(requiredColor : CardColor) boolean
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — BOARD
    %% ════════════════════════════════════════════

    class Board {
        <<entity>>
        -locationsById : Map~String, Location~
        -routesById : Map~String, Route~
        +locations() List~Location~
        +routes() List~Route~
        +findLocation(locationId : String) Optional~Location~
        +findRoute(routeId : String) Optional~Route~
        +routesInDoubleGroup(doubleGroupId : String) List~Route~
    }

    class Location {
        <<value object>>
        -id : String
        -displayName : String
        -district : int
    }

    class Route {
        <<entity>>
        -id : String
        -locationA : Location
        -locationB : Location
        -length : int
        -color : RouteColor
        -kind : RouteKind
        -requiredBusSymbols : int
        -doubleGroupId : String
        -requirement : RouteRequirement
        -claimedBy : String
        +isClaimed() boolean
        +isFerry() boolean
        +canBeClaimed(playerId, playerCount, routesInDoubleGroup, payment, busesRemaining) boolean
        +claim(playerId : String) void
        +restoreClaim(restoredClaimedBy : String) void
    }

    class RouteRequirement {
        <<interface>>
        +isSatisfiedBy(payment : CardPayment) boolean
    }

    class ColouredRouteRequirement {
        -requiredColor : RouteColor
        -length : int
        +isSatisfiedBy(payment : CardPayment) boolean
    }

    class GreyRouteRequirement {
        -length : int
        +isSatisfiedBy(payment : CardPayment) boolean
    }

    class FerryRouteRequirement {
        -routeColor : RouteColor
        -length : int
        -requiredBusSymbols : int
        +isSatisfiedBy(payment : CardPayment) boolean
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — CARD SUPPLY
    %% ════════════════════════════════════════════

    class TransportCardDeck {
        <<entity>>
        -drawPile : Deque~CardColor~
        -discardPile : List~CardColor~
        -shuffleStrategy : ShuffleStrategy
        +draw() Optional~CardColor~
        +reshuffleDiscardsIntoDrawPile() void
        +discard(cards : List~CardColor~) void
        +drawPileSize() int
        +discardPileSize() int
    }

    class FaceUpDisplay {
        <<entity>>
        -visibleCards : List~CardColor~
        +MAX_VISIBLE_CARDS : int = 5$
        +visibleCards() List~CardColor~
        +take(index : int) CardColor
        +refillSlot(index : int, deck : TransportCardDeck) Optional~CardColor~
        +enforceBusFlush(deck : TransportCardDeck) void
        +busCount() int
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — PLAYER
    %% ════════════════════════════════════════════

    class Player {
        <<entity>>
        +STARTING_BUSES : int = 17$
        -id : String
        -name : String
        -color : PlayerColor
        -hand : List~CardColor~
        -tickets : List~DestinationTicket~
        -busesRemaining : int
        -score : int
        +addCards(cards : List~CardColor~) void
        +addTickets(newTickets : List~DestinationTicket~) void
        +canAfford(payment : CardPayment) boolean
        +spendCards(payment : CardPayment) void
        +useBuses(count : int) void
        +addScore(points : int) void
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — TICKET
    %% ════════════════════════════════════════════

    class DestinationTicket {
        <<value object>>
        -id : String
        -locationA : Location
        -locationB : Location
        -points : int
    }

    class DestinationTicketDeck {
        <<entity>>
        -drawPile : Deque~DestinationTicket~
        +draw(count : int) List~DestinationTicket~
        +drawForTurn() List~DestinationTicket~
        +returnUnkeptToBottom(drawnTickets, keptTickets) void
        +returnToBottom(tickets : List~DestinationTicket~) void
        +size() int
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — TURN
    %% ════════════════════════════════════════════

    class TurnManager {
        <<entity>>
        -playerOrder : List~String~
        -currentIndex : int
        -finalRoundActive : boolean
        -triggeringPlayerId : String
        -finalTurnsRemaining : int
        +currentPlayerId() String
        +isCurrentPlayer(playerId : String) boolean
        +requireCurrentPlayer(playerId : String) void
        +advanceTurn() void
        +endTurn(currentPlayerBusesRemaining : int) void
        +isFinalRoundActive() boolean
        +isFinalRoundComplete() boolean
    }

    class RushHourManager {
        <<entity>>
        -eventDeck : List~String~
        -eventDiscard : List~String~
        -phase : RushHourPhase
        -forecastEventId : String
        -activeEventId : String
        -turnsRemaining : int
        -bonusPointsByPlayerId : Map~String, Integer~
        +start(playerCount : int) void
        +advanceAfterCompletedTurn(playerCount : int) void
        +affectsDuringPeak(route : Route) boolean
        +awardBonus(playerId : String, points : int) void
        +restoreState(...) void
    }

    class RushHourEvent {
        <<value object>>
        -id : String
        -title : String
        -description : String
        -selector : RouteSelector
        -extraCardCost : int
        -bonusPoints : int
        +affects(route : Route) boolean
    }

    class RushHourClaimRule {
        <<service>>
        -rushHourManager : RushHourManager
        +requiredDetourCards(route : Route) int
        +bonusPoints(route : Route) int
        +isDetourSatisfied(route : Route, payment : CardPayment) boolean
    }

    class RouteSelector {
        <<interface>>
        +matches(route : Route) boolean
    }

    class RouteSelectors {
        <<factory>>
        +byKind(kind : RouteKind) RouteSelector$
        +byRouteIds(routeIds : Set~String~) RouteSelector$
        +anyOf(selectors : List~RouteSelector~) RouteSelector$
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — SCORING
    %% ════════════════════════════════════════════

    class ScoreCalculator {
        <<service>>
        -routeScoreTable : RouteScoreTable
        -ticketCompletionChecker : TicketCompletionChecker
        -longestPathCalculator : LongestPathCalculator
        +calculateFinalScore(board : Board, players : List~Player~) FinalScore
    }

    class RouteScoreTable {
        <<value object>>
        +pointsForLength(length : int) int
    }

    class TicketCompletionChecker {
        <<service>>
        +isCompleted(ticket : DestinationTicket, claimedRoutes : List~Route~) boolean
    }

    class LongestPathCalculator {
        <<service>>
        +longestPathLength(claimedRoutes : List~Route~) int
    }

    class FinalScore {
        <<value object>>
        -scoresByPlayerId : Map~String, PlayerFinalScore~
        -winnerIds : List~String~
        +fromPlayerScores(scores, winnerIds) FinalScore$
        +totalsByPlayerId() Map~String, Integer~
        +winnerIds() List~String~
    }

    class PlayerFinalScore {
        <<value object>>
        -playerId : String
        -routePoints : int
        -ticketPoints : int
        -longestPathLength : int
        -longestPathBonus : int
        -ticketResults : List~TicketResult~
        -totalScore : int
        +completedTicketCount() int
        +totalScore() int
    }

    class TicketResult {
        <<value object>>
        -ticketId : String
        -locationAId : String
        -locationBId : String
        -points : int
        -completed : boolean
        +scoreContribution() int
    }

    %% ════════════════════════════════════════════
    %% DOMAIN — GAME AGGREGATE
    %% ════════════════════════════════════════════

    class Game {
        <<entity>>
        -board : Board
        -players : List~Player~
        -transportCardDeck : TransportCardDeck
        -faceUpDisplay : FaceUpDisplay
        -destinationTicketDeck : DestinationTicketDeck
        -turnManager : TurnManager
        -rushHourManager : RushHourManager
        -phase : GamePhase
        +acceptsPlayerActions() boolean
        +transitionTo(nextPhase : GamePhase) void
        +endCurrentTurn() void
        +currentPlayer() Player
        +findPlayer(playerId : String) Optional~Player~
    }

    %% ════════════════════════════════════════════
    %% APPLICATION — COMMANDS
    %% ════════════════════════════════════════════

    class GameCommand {
        <<interface>>
        +execute(game : Game) CommandResult
    }

    class ClaimRouteCommand {
        -playerId : String
        -routeId : String
        -payment : ClaimRoutePayment
        -routeScoreTable : RouteScoreTable
        +execute(game : Game) CommandResult
    }

    class ClaimRoutePayment {
        <<value object>>
        -routePayment : CardPayment
        -rushHourDetourPayment : CardPayment
        +routeOnly(routePayment : CardPayment) ClaimRoutePayment$
        +combinedPayment() CardPayment
    }

    class DrawTransportCardCommand {
        -playerId : String
        -source : DrawSource
        -faceUpIndex : int
        +blind(playerId : String) DrawTransportCardCommand$
        +faceUp(playerId : String, faceUpIndex : int) DrawTransportCardCommand$
        +execute(game : Game) CommandResult
        +executeDraw(game : Game) DrawTransportCardResult
    }

    class DrawDestinationTicketsCommand {
        -playerId : String
        -keptTicketIds : List~String~
        +execute(game : Game) CommandResult
    }

    %% ════════════════════════════════════════════
    %% APPLICATION — SERVICE AND OBSERVER
    %% ════════════════════════════════════════════

    class GameApplicationService {
        <<service>>
        -game : Game
        -scoreCalculator : ScoreCalculator
        -listeners : List~GameStateListener~
        -undoHistory : UndoHistory
        -mementoFactory : GameMementoFactory
        -transportDrawCoordinator : TransportDrawCoordinator
        +executeCommand(command : GameCommand) CommandResult
        +endTransportCardDrawAction() CommandResult
        +getSnapshot() GameSnapshot
        +canUndo() boolean
        +undoLastTurn() CommandResult
        +previewDestinationTickets(playerId : String) DestinationTicketDrawPreview
        +finalScoreSnapshot() FinalScoreSnapshot
        +completeScoring() CommandResult
        +addListener(listener : GameStateListener) void
        +removeListener(listener : GameStateListener) void
    }

    class TransportDrawCoordinator {
        <<service>>
        -game : Game
        -mementoFactory : GameMementoFactory
        -undoHistory : UndoHistory
        -pendingDrawActionMemento : GameMemento
        +execute(command : DrawTransportCardCommand) CommandResult
        +endAction() CommandResult
        +isActive() boolean
        +progress() TransportDrawProgress
        +restoreProgress(progress : TransportDrawProgress) void
    }

    class GameStateListener {
        <<interface>>
        +onGameStateChanged(snapshot : GameSnapshot) void
    }

    %% ════════════════════════════════════════════
    %% APPLICATION — DTOs
    %% ════════════════════════════════════════════

    class CommandResult {
        <<DTO>>
        -success : boolean
        -message : String
        +success(message : String) CommandResult$
        +failure(message : String) CommandResult$
        +isSuccess() boolean
        +isFailure() boolean
    }

    class GameSnapshot {
        <<DTO>>
        -phase : GamePhase
        -currentPlayerId : String
        -players : List~PlayerSnapshot~
        -locations : List~LocationSnapshot~
        -routes : List~RouteSnapshot~
        -faceUpCards : List~CardColor~
        -drawProgress : TransportDrawProgress
        -canUndo : boolean
        -rushHourPhase : RushHourPhase
        -rushHourAffectedRouteIds : List~String~
        -rushHourPointsByPlayerId : Map~String, Integer~
        +from(game : Game, drawProgress : TransportDrawProgress, canUndo : boolean) GameSnapshot$
        +acceptsPlayerActions() boolean
        +canUndo() boolean
    }

    class TransportDrawProgress {
        <<DTO>>
        -active : boolean
        -playerId : String
        -drawsTaken : int
        -lockedFaceUpIndex : int
        +inactive() TransportDrawProgress$
    }

    class RushHourEventSnapshot {
        <<DTO>>
        -id : String
        -title : String
        -description : String
        -extraCardCost : int
        -bonusPoints : int
        +from(event : RushHourEvent) RushHourEventSnapshot$
    }

    class GameMemento {
        <<memento>>
        -phase : GamePhase
        -drawProgress : TransportDrawProgress
        -players : List~PlayerState~
        -routes : List~RouteState~
        -rushHourState : RushHourState
    }

    class GameMementoFactory {
        <<factory>>
        +capture(game : Game, drawProgress : TransportDrawProgress) GameMemento
        +restore(game : Game, memento : GameMemento) void
    }

    class UndoHistory {
        <<mementoStore>>
        -mementos : Deque~GameMemento~
        +push(memento : GameMemento) void
        +pop() Optional~GameMemento~
        +canUndo() boolean
    }

    %% ════════════════════════════════════════════
    %% INFRASTRUCTURE — FACTORIES
    %% ════════════════════════════════════════════

    class GameFactory {
        <<factory>>
        +createNewGame(playerNames : List~String~) Game$
        +createNewGame(playerNames, shuffleStrategy) Game$
        +createSetupDraft(players, shuffleStrategy) GameSetupDraft$
    }

    class BoardFactory {
        <<factory>>
        +createLondonBoard() Board$
    }

    class DeckFactory {
        <<factory>>
        +createTransportDeck(shuffleStrategy : ShuffleStrategy) TransportCardDeck$
        +createDestinationTicketDeck(board : Board, shuffleStrategy : ShuffleStrategy) DestinationTicketDeck$
    }

    class RushHourEventFactory {
        <<factory>>
        +createLondonRushHourManager(shuffleStrategy : ShuffleStrategy) RushHourManager$
        +createLondonEvents() List~RushHourEvent~$
    }

    %% ════════════════════════════════════════════
    %% INFRASTRUCTURE — SHUFFLE STRATEGY
    %% ════════════════════════════════════════════

    class ShuffleStrategy {
        <<interface>>
        +shuffle(items : List~T~) List~T~
    }

    class RandomShuffleStrategy {
        -random : Random
        +shuffle(items : List~T~) List~T~
    }

    class FixedOrderShuffleStrategy {
        +shuffle(items : List~T~) List~T~
    }

    %% ════════════════════════════════════════════
    %% UI — REPRESENTATIVE CLASSES
    %% ════════════════════════════════════════════

    class MainFrame {
        <<view>>
        -service : GameApplicationService
        +onGameStateChanged(snapshot : GameSnapshot) void
    }

    class BoardPanel {
        <<view>>
        -boardRenderer : BoardRenderer
        -boardViewModel : BoardViewModel
        +onGameStateChanged(snapshot : GameSnapshot) void
    }

    class CardMarketPanel {
        <<view>>
        -blindDeckButton : JButton
        -faceUpCardsPanel : JPanel
        +onGameStateChanged(snapshot : GameSnapshot) void
    }

    class RushHourPanel {
        <<view>>
        +onGameStateChanged(snapshot : GameSnapshot) void
    }

    class BoardRenderer {
        -routeRenderer : RouteRenderer
        -locationRenderer : LocationRenderer
        +render(g2d, dimension, viewModel, hoverRouteId, selectedRouteId) Map
    }

    class BoardViewModel {
        <<view model>>
        +from(snapshot : GameSnapshot) BoardViewModel$
    }

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — GAME AGGREGATE COMPOSITION
    %% ════════════════════════════════════════════

    Game "1" *-- "1" Board : board
    Game "1" *-- "2..4" Player : players
    Game "1" *-- "1" TransportCardDeck : transportCardDeck
    Game "1" *-- "1" FaceUpDisplay : faceUpDisplay
    Game "1" *-- "1" DestinationTicketDeck : destinationTicketDeck
    Game "1" *-- "1" TurnManager : turnManager
    Game "1" *-- "1" RushHourManager : rushHourManager
    Game --> GamePhase : phase

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — BOARD COMPOSITION
    %% ════════════════════════════════════════════

    Board "1" *-- "1..*" Location : locationsById
    Board "1" *-- "1..*" Route : routesById

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — ROUTE
    %% ════════════════════════════════════════════

    Route "1..*" --> "2" Location : connects
    Route --> RouteColor : color
    Route --> RouteKind : kind
    Route "1" --> "1" RouteRequirement : requirement

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — STRATEGY: RouteRequirement
    %% ════════════════════════════════════════════

    ColouredRouteRequirement ..|> RouteRequirement
    GreyRouteRequirement ..|> RouteRequirement
    FerryRouteRequirement ..|> RouteRequirement
    ColouredRouteRequirement --> RouteColor : requiredColor
    FerryRouteRequirement --> RouteColor : routeColor

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — CardPayment dependency
    %% ════════════════════════════════════════════

    RouteRequirement ..> CardPayment : validates
    CardPayment o-- "1..*" CardColor : cards

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — PLAYER
    %% ════════════════════════════════════════════

    Player --> PlayerColor : color
    Player "1" o-- "0..*" CardColor : hand
    Player "1" o-- "1..*" DestinationTicket : tickets

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — TICKET
    %% ════════════════════════════════════════════

    DestinationTicket "0..*" --> "2" Location : endpoints
    DestinationTicketDeck "1" o-- "0..*" DestinationTicket : drawPile

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — CARD SUPPLY
    %% ════════════════════════════════════════════

    TransportCardDeck "1" --> "1" ShuffleStrategy : shuffleStrategy
    FaceUpDisplay ..> TransportCardDeck : refills from

    RushHourManager "1" o-- "0..*" RushHourEvent : eventsById
    RushHourManager --> RushHourPhase : phase
    RushHourManager --> ShuffleStrategy : shuffleStrategy
    RushHourEvent --> RouteSelector : selector
    RushHourClaimRule --> RushHourManager : rushHourManager
    RouteSelectors ..> RouteSelector : creates

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — STRATEGY: ShuffleStrategy
    %% ════════════════════════════════════════════

    RandomShuffleStrategy ..|> ShuffleStrategy
    FixedOrderShuffleStrategy ..|> ShuffleStrategy

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — SCORING COMPOSITION
    %% ════════════════════════════════════════════

    ScoreCalculator "1" *-- "1" RouteScoreTable : routeScoreTable
    ScoreCalculator "1" *-- "1" TicketCompletionChecker : ticketCompletionChecker
    ScoreCalculator "1" *-- "1" LongestPathCalculator : longestPathCalculator
    ScoreCalculator ..> Board : reads routes
    ScoreCalculator ..> Player : reads tickets/routes

    FinalScore "1" *-- "1..*" PlayerFinalScore : scoresByPlayerId
    PlayerFinalScore "1" *-- "0..*" TicketResult : ticketResults

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — COMMAND PATTERN
    %% ════════════════════════════════════════════

    ClaimRouteCommand ..|> GameCommand
    DrawTransportCardCommand ..|> GameCommand
    DrawDestinationTicketsCommand ..|> GameCommand
    GameCommand ..> Game : executes against
    GameCommand ..> CommandResult : returns

    ClaimRouteCommand --> ClaimRoutePayment : payment
    ClaimRoutePayment --> CardPayment : route and detour payments
    ClaimRouteCommand --> RouteScoreTable : routeScoreTable
    ClaimRouteCommand ..> RushHourClaimRule : validates detour
    DrawTransportCardCommand --> DrawSource : source

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — APPLICATION SERVICE
    %% ════════════════════════════════════════════

    GameApplicationService "1" --> "1" Game : game
    GameApplicationService "1" --> "1" ScoreCalculator : scoreCalculator
    GameApplicationService "1" --> "0..*" GameStateListener : listeners
    GameApplicationService "1" --> "1" TransportDrawCoordinator : drawCoordinator
    GameApplicationService "1" --> "1" UndoHistory : undoHistory
    GameApplicationService "1" --> "1" GameMementoFactory : mementoFactory
    GameApplicationService ..> GameCommand : executes
    GameApplicationService ..> GameSnapshot : publishes
    GameApplicationService ..> FinalScore : calculates

    TransportDrawCoordinator "1" --> "1" Game : game
    TransportDrawCoordinator "1" --> "1" GameMementoFactory : mementoFactory
    TransportDrawCoordinator "1" --> "1" UndoHistory : undoHistory
    TransportDrawCoordinator ..> TransportDrawProgress : exposes progress
    GameMementoFactory ..> GameMemento : captures/restores
    UndoHistory "1" o-- "0..2" GameMemento : mementos

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — OBSERVER PATTERN (UI)
    %% ════════════════════════════════════════════

    MainFrame ..|> GameStateListener
    BoardPanel ..|> GameStateListener
    CardMarketPanel ..|> GameStateListener
    RushHourPanel ..|> GameStateListener
    MainFrame --> GameApplicationService : service
    GameStateListener ..> GameSnapshot : receives

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — UI RENDERING PIPELINE
    %% ════════════════════════════════════════════

    BoardPanel --> BoardRenderer : boardRenderer
    BoardPanel --> BoardViewModel : boardViewModel
    BoardViewModel ..> GameSnapshot : created from

    %% ════════════════════════════════════════════
    %% RELATIONSHIPS — FACTORY DEPENDENCIES
    %% ════════════════════════════════════════════

    GameFactory ..> Game : creates
    GameFactory ..> BoardFactory : delegates
    GameFactory ..> DeckFactory : delegates
    GameFactory ..> RushHourEventFactory : delegates
    GameFactory ..> ShuffleStrategy : uses
    BoardFactory ..> Board : creates
    DeckFactory ..> TransportCardDeck : creates
    DeckFactory ..> DestinationTicketDeck : creates
    RushHourEventFactory ..> RushHourManager : creates
```

---

## 3. Design Patterns Identified

| Pattern | Where Applied | Justification |
|---------|---------------|---------------|
| **Command** | `GameCommand` interface with `ClaimRouteCommand`, `DrawTransportCardCommand`, `DrawDestinationTicketsCommand` | Decouples player intent from execution. Each action is a first-class object that validates and executes against the `Game` aggregate, making actions testable without Swing. |
| **Strategy** | `RouteRequirement` → `ColouredRouteRequirement` / `GreyRouteRequirement`; `ShuffleStrategy` → `RandomShuffleStrategy` / `FixedOrderShuffleStrategy` | Eliminates type-checking conditionals for route validation. Enables deterministic shuffling in tests via constructor injection. |
| **Observer** | `GameStateListener` interface; `GameApplicationService` publishes `GameSnapshot` to registered listeners; Swing panels implement the interface | Domain and application layers do not know about Swing. UI panels refresh reactively from immutable snapshots. |
| **Factory** | `GameFactory`, `BoardFactory`, `DeckFactory` | Centralises game setup including board data, deck composition, and initial dealing. Isolates London-specific data from generic game mechanics for Sprint 3 extensibility. |

---

## 4. Sanity Check Against Domain Model and Game Rules

### Audit Update

The implementation now includes the Sprint 3 Ferry, Undo, and Rush Hour extensions. The authoritative current structure is:

- Ferry routes: `Route.kind`, `Route.requiredBusSymbols`, `RouteKind`, and `FerryRouteRequirement`.
- Rush Hour: `RushHourManager` is composed by `Game`; `RushHourClaimRule` is used by `ClaimRouteCommand`; `RushHourEventFactory` supplies event data; event state and affected routes are exposed through `GameSnapshot`.
- Undo: `GameApplicationService` owns `UndoHistory` and `GameMementoFactory`; `TransportDrawCoordinator` coordinates draw-progress mementos for multi-step transportation-card actions.
- Transport draw state: `DrawActionState` is a private implementation detail of `TransportDrawCoordinator`, not `GameApplicationService`.
- Current applied patterns include Command, Strategy, Observer, Factory, and Memento.

### Alignment with `domain_model.md`

| Domain Model Concept | Implementation | Notes |
|----------------------|----------------|-------|
| Game | `Game` class (aggregate root) | Matches. Implementation delegates scoring, turns, and card logic to collaborators as prescribed. |
| Player | `Player` class | Matches. Rich behaviour: `canAfford()`, `spendCards()`, `useBuses()`, `addScore()`. Not anemic. |
| Board Map | `Board` class | Matches. Composes `Location` and `Route` via indexed maps. |
| Location | `Location` class | Matches. Immutable with `id`, `displayName`, `district`. |
| Route | `Route` class | Matches. Validates claims via `canBeClaimed()`, delegates payment validation to `RouteRequirement`. |
| Ferry Route | `RouteKind.FERRY`, `Route.requiredBusSymbols`, `FerryRouteRequirement` | Matches Sprint 3 required extension. Ferry payment validation stays in the route requirement strategy, outside Swing. |
| Double Route | Encoded as `doubleGroupId` field on `Route`, queried via `Board.routesInDoubleGroup()` | Domain model has a separate `Double Route` grouping entity. Implementation collapses this into a shared group ID — a pragmatic simplification that preserves the constraint semantics. |
| Transportation Card | Represented as `CardColor` enum values | Domain model has `Transportation Card` and `Bus Card` as a generalisation hierarchy. Implementation treats all cards as `CardColor` enum instances where `BUS` is a distinguished value. This is simpler but sacrifices the explicit subtype relationship. The bus-specific drawing constraints are enforced procedurally in `GameApplicationService` and `FaceUpDisplay`. |
| Bus Card | `CardColor.BUS` enum constant | See above. |
| CardPayment | `CardPayment` value object | Not in the domain model (which has no implementation detail). A well-designed addition for payment validation. |
| Transportation Card Supply | Split into `TransportCardDeck` + `FaceUpDisplay` | Domain model composes Draw Pile, Discard Pile, and Face-Up Display under a single `Transportation Card Supply`. Implementation separates Draw/Discard (in `TransportCardDeck`) from Face-Up Display. Functionally equivalent; the flush rule coordination still works via method parameters. |
| Destination Ticket | `DestinationTicket` class | Matches. Immutable, connects two `Location` endpoints with a point value. |
| Destination Ticket Deck | `DestinationTicketDeck` class | Matches. Bottom-return semantics for unkept tickets. |
| Turn | `TurnManager` class | Domain model has `Turn` as a separate entity. Implementation merges turn tracking and final-round state into `TurnManager`. The concept is preserved; the structural representation differs. |
| Action subtypes | `GameCommand` interface with three implementations | Domain model uses `Action` generalisation with three subtypes. Implementation uses the Command pattern, which serves the same polymorphic purpose with additional benefits (testability, undo potential). |
| Score | `int score` field on `Player` | Domain model has `Score` as a separate entity. Implementation uses a primitive field, which is simpler and sufficient for the current scope. |
| Final Round | `finalRoundActive`/`finalTurnsRemaining` fields in `TurnManager` | Domain model has `Final Round` as a separate entity. Implementation embeds the state within `TurnManager`, which owns turn progression. |
| Longest Continuous Path Bonus | Computed by `LongestPathCalculator`, awarded in `ScoreCalculator` | Domain model has a separate entity. Implementation distributes this across scoring collaborators, which is a reasonable architectural choice. |
| Bus (plastic piece) | `busesRemaining` field on `Player` | Domain model has `Bus` as a separate entity. Implementation uses a counter, which is sufficient since individual bus identity is irrelevant to the rules. |
| Rush Hour Events | `RushHourManager`, `RushHourEvent`, `RushHourClaimRule`, `RouteSelector` | Matches Sprint 3 self-defined extension. Event sequencing and detour/bonus rules live in domain/application collaborators, while Swing only renders snapshot state and submits payments. |
| Undo | `GameMemento`, `GameMementoFactory`, `UndoHistory` | Matches the selected Sprint 3 extension. Mementos live in the application layer and restore through narrow domain restore APIs. |

### Alignment with `game_rules.md`

| Rule | Implementation Status |
|------|----------------------|
| 2–4 players, each with 17 buses | `GameFactory` validates 2–4 players; `Player.STARTING_BUSES = 17` |
| 44 transportation cards (6×6 + 8 Bus) | `DeckFactory.createTransportDeck()` builds the correct composition |
| Three mutually exclusive turn actions | Three `GameCommand` implementations; `GameApplicationService` enforces one-action-per-turn |
| Face-up Bus card limits draw to 1 card | `TransportDrawCoordinator.shouldCompleteAfter()` enforces this |
| Replacement Bus card cannot be second draw | `DrawActionState.lockedFaceUpIndex` tracks and blocks |
| 3-Bus flush rule | `FaceUpDisplay.enforceBusFlush()` with loop protection |
| Route scoring: 1→1, 2→2, 3→4, 4→7 | `RouteScoreTable.pointsForLength()` |
| Grey route accepts any single colour set | `GreyRouteRequirement.isSatisfiedBy()` |
| Double-route restrictions (single-player and 2-player) | `Route.canBeClaimed()` with `playerCount` and `doubleGroupId` checks |
| End-game trigger: 0–2 buses remaining | `TurnManager.endTurn()` triggers at ≤2 buses |
| Final round: each player gets one more turn | `TurnManager.finalTurnsRemaining` counts down |
| Ticket completion via graph connectivity | `TicketCompletionChecker.isCompleted()` using BFS |
| Longest continuous path (+10, ties share) | `LongestPathCalculator` via DFS; `ScoreCalculator` awards +10 to all tied players |
| Tie-breakers: points → completed tickets → longest path | `ScoreCalculator.determineWinners()` applies three-tier filtering |
| Destination tickets: draw 2, keep ≥1, return to bottom | `DestinationTicketDeck.drawForTurn()` + `returnUnkeptToBottom()` |

Additional Sprint 3 rules are also reflected: Ferry routes require Bus symbols through
`FerryRouteRequirement`; Rush Hour forecast/peak sequencing is owned by `RushHourManager`;
Rush Hour detour cost and route-claim bonus handling are applied by `RushHourClaimRule` and
`ClaimRouteCommand`; completed-turn undo is handled by `GameApplicationService`,
`GameMementoFactory`, and `UndoHistory`.

No significant discrepancies between the implementation and the game rules were found after this audit.

---

## 5. Architecture Quality Assessment

### Strengths

- **Clean four-layer separation**: Domain, Application, Infrastructure, and UI layers have one-way dependencies. An `ArchitectureBoundaryTest` enforces no `javax.swing` imports in the domain, application, or infrastructure packages.
- **Rich domain model**: Domain objects own meaningful behaviour. `Player.canAfford()`, `Route.canBeClaimed()`, `Route.claim()`, and `FaceUpDisplay.enforceBusFlush()` keep logic where it belongs.
- **Composition over inheritance**: `Route` *has a* `RouteRequirement`; `ScoreCalculator` *has* three scoring collaborators. No deep inheritance hierarchies.
- **Polymorphism eliminates type-checking**: `RouteRequirement.isSatisfiedBy()` dispatches to the correct strategy without `if/else` on route colour.
- **Constructor dependency injection**: All collaborators are injected via constructors. No hidden globals or service locators.
- **Immutable snapshots**: The UI receives `GameSnapshot` (immutable DTO), protecting the domain from accidental mutation.
- **Testable without GUI**: All domain and application tests run without Swing.

### No Significant Architecture Smells Detected

- No God Class: `Game` delegates to `TurnManager`, `ScoreCalculator`, `Board`, etc.
- No anemic domain model: domain objects have meaningful methods beyond getters.
- No feature envy: external code does not reach into `Player.hand` to count cards.
- No UI-domain coupling: Swing panels create commands and read snapshots only.
