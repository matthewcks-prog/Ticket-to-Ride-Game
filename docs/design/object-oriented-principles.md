# Object-Oriented Principles Applied to Ticket to Ride: London

This note is supporting design material. The authoritative architecture rules remain in
`architecture_guide.md`.

## Responsibility Assignment

Each rule belongs to the object with the information needed to enforce it.

| Responsibility | Owner | Reason |
|---|---|---|
| Track hand, tickets, score, and buses | `Player` | The player owns those resources and can protect invariants. |
| Validate route payment shape | `RouteRequirement` | The requirement knows whether the route is coloured or grey. |
| Enforce route claim status and double-route constraints | `Route` with `Board.routesInDoubleGroup()` | The route owns claim state; the board can find related printed routes. |
| Manage turn order and final-round countdown | `TurnManager` | Turn progression is cohesive and independent of UI. |
| Draw, discard, and reshuffle transportation cards | `TransportCardDeck` | Deck mechanics should not leak into commands or Swing panels. |
| Enforce face-up display flushes | `FaceUpDisplay` | The display owns the visible-card invariant. |
| Check ticket completion | `TicketCompletionChecker` | Connectivity is a scoring rule, not a UI concern. |
| Calculate final score and winners | `ScoreCalculator` | Final scoring composes route, ticket, and bonus rules. |

## Encapsulation

- Domain fields remain private.
- Mutable collections are copied or exposed as unmodifiable views.
- Route ownership changes through `route.claim(playerId)`, not a public setter.
- Card spending happens through `Player.spendCards(payment)`, not by external hand mutation.
- Swing receives `GameSnapshot` and command results, never live mutable domain objects.

## Composition Over Inheritance

The implementation avoids deep inheritance hierarchies. Important examples:

- `Route` has a `RouteRequirement` strategy rather than subclasses for coloured and grey routes.
- `ScoreCalculator` has scoring collaborators rather than embedding every algorithm directly.
- `GameApplicationService` has a `ScoreCalculator`, publishes to `GameStateListener`s, and delegates
  transportation-card draw sequencing to `TransportDrawCoordinator`.

This keeps extension points narrow and testable.

## Polymorphism Over Type Checking

The strongest current example is route payment validation:

```java
route.requirement().isSatisfiedBy(payment);
```

The caller does not need to branch on grey versus coloured routes. That decision is hidden behind
the `RouteRequirement` interface.

## Coupling and Cohesion

- `domain` has no Swing dependencies.
- `ui` renders state and forwards intent, but does not decide rules.
- `application` coordinates use cases and owns temporary interaction state such as the two-card draw
  sequence.
- `infrastructure` creates London board/deck data and injects randomness.

These boundaries let domain and application tests run without the UI package.

## Command-Query Separation

Commands mutate state: `claim`, `spendCards`, `useBuses`, and `endCurrentTurn`.

Queries report state: `findRoute`, `currentPlayer`, `canAfford`, `visibleCards`, and snapshots.

Keeping these separate makes rule tests easier to reason about and avoids hidden mutation during UI
rendering.

## Design Risks to Keep Watching

- `GameApplicationService` can grow if more multi-step actions are added. The transportation-card
  draw workflow is already isolated in `TransportDrawCoordinator`; future multi-step actions should
  receive similarly cohesive coordinators instead of expanding the service.
- New route types should extend `RouteRequirement` or a similarly narrow rule abstraction rather
  than adding Swing-side checks.
- Undo or save/load should work through application/domain state snapshots and commands, not through
  direct UI component state.
- New scoring rules should be composed into scoring collaborators and documented with ADRs.
