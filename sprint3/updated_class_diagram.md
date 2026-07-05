# Sprint 3 Updated UML Class Diagram

## Rubric Coverage

This file is the Sprint 3 hand-in wrapper for the updated UML class diagram. It makes the
Sprint 3 changes from the Sprint 2 diagram explicit and links the final diagram assets.

| Rubric requirement | Evidence |
|---|---|
| Clear and neat class diagram | `sprint3/class-diagrams/class-diagram-final.drawio.png` and `sprint3/class-diagrams/class-diagram-final.drawio` |
| Covers required functionality | Root implementation-level diagram in `class_diagram.md`, audited against `src/main/java` on 2026-06-01 |
| Sprint 2 changes clearly visible | Side-by-side assets and change log below |
| Source code corresponds to class diagram | `class_diagram.md` Section 4 cross-checks implementation classes against `domain_model.md` and `game_rules.md` |

## Diagram Files

- Sprint 2 baseline diagram used for comparison:
  `sprint3/class-diagrams/class-diagram-used-in-sprint2.drawio.png`
- Sprint 3 intermediate diagram:
  `sprint3/class-diagrams/class-diagram-sprint3-v1.drawio.png`
- Sprint 3 final diagram:
  `sprint3/class-diagrams/class-diagram-final.drawio.png`
- Editable final source:
  `sprint3/class-diagrams/class-diagram-final.drawio`
- Markdown implementation-level UML source:
  `class_diagram.md`

## Sprint 3 Changes Compared With Sprint 2

| Change area | Sprint 2 structure | Sprint 3 final structure | Why the change matters |
|---|---|---|---|
| Ferry routes | Routes were standard coloured/grey routes only | `RouteKind`, `Route.requiredBusSymbols`, and `FerryRouteRequirement` add Ferry payment rules | Required Sprint 3 extension is modelled as a `RouteRequirement` Strategy instead of UI logic |
| Undo | No turn-history model | `GameMemento`, `GameMementoFactory`, and `UndoHistory` restore the last two completed turns | Pick-from-the-list extension uses Memento without exposing mutable internals to Swing |
| Rush Hour | No event system | `RushHourManager`, `RushHourEvent`, `RushHourClaimRule`, `RouteSelector`, `RushHourPhase`, and `RushHourEventFactory` | Self-defined extension adds new gameplay while preserving domain/application/UI separation |
| Transport draw orchestration | Draw progress lived inside broader service coordination | `TransportDrawCoordinator` owns two-card draw progress and Bus restrictions | Removes application-service bloat and keeps multi-step action state cohesive |
| UI event display | Core panels observed game snapshots | `RushHourPanel` also implements `GameStateListener` | New extension state is rendered through the existing Observer pattern |
| Snapshot data | Snapshot covered base-game state | `GameSnapshot` also exposes undo availability, draw progress, and Rush Hour event data | Swing remains a read-only consumer of immutable application DTOs |
| Factory setup | Factories created board/decks/game | `RushHourEventFactory` and updated `GameFactory` wire Sprint 3 extension state | London setup data stays centralised and testable |

## Class Diagram Scope

The final diagram intentionally shows architecturally significant classes rather than every Swing
helper or DTO field. It includes:

- Domain entities and value objects: `Game`, `Board`, `Route`, `Player`, tickets, decks, scoring,
  turn state, Ferry route rules, and Rush Hour rules.
- Application classes: commands, `GameApplicationService`, `TransportDrawCoordinator`, immutable
  snapshots, Observer listener, and Memento undo classes.
- Infrastructure factories and shuffle implementations.
- Representative UI boundary classes that prove Swing observes snapshots and submits commands.

The omitted classes are listed in `class_diagram.md` Section 1. They are omitted only where they are
thin DTOs, rendering helpers, tests, or repeated Swing panels that follow the same Observer-driven
structure already shown.

## Pattern Visibility In The Diagram

| Pattern | Diagram evidence |
|---|---|
| Command | `GameCommand` implemented by `ClaimRouteCommand`, `DrawTransportCardCommand`, and `DrawDestinationTicketsCommand` |
| Strategy | `RouteRequirement`, `ShuffleStrategy`, and `RouteSelector` implementation relationships |
| Observer | `GameStateListener` implemented by `MainFrame`, `BoardPanel`, `CardMarketPanel`, and `RushHourPanel` |
| Factory | `GameFactory`, `BoardFactory`, `DeckFactory`, and `RushHourEventFactory` creation dependencies |
| Memento | `GameMemento`, `GameMementoFactory`, and bounded `UndoHistory` |

## Cardinality Highlights

- One `Game` composes exactly one `Board`, one `TurnManager`, one `RushHourManager`, one
  `TransportCardDeck`, one `FaceUpDisplay`, one `DestinationTicketDeck`, and two to four players.
- One `Board` contains 17 `Location` objects and 43 printed `Route` objects for London.
- One `Route` connects exactly two `Location` endpoints and has exactly one `RouteRequirement`.
- One `Player` owns zero or more transport cards, one or more destination tickets after setup, and
  17 starting buses represented as a count.
- One `GameApplicationService` has zero or more `GameStateListener` observers and one bounded
  `UndoHistory` containing zero to two `GameMemento` entries.
