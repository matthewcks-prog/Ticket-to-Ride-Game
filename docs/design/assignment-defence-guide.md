# Assignment Defence Guide

Use this file when writing design rationale, ADRs, report sections, or viva answers. Keep every
claim tied to evidence in the implementation, tests, or docs.

## Architecture Position

The project uses a layered modular monolith:

```text
ui -> application -> domain
infrastructure -> domain/application setup seams
```

The domain layer owns Ticket to Ride: London rules and invariants. The application layer exposes
commands, snapshots, and use-case coordination. Swing displays snapshots and forwards user
intentions. Infrastructure creates London board/deck data and provides shuffle strategies.

This is better suited than client-server or microservices for a local Java/Swing assignment because
distributed deployment would add latency, failure modes, and operational complexity without solving
a project problem. The useful lecture idea retained from those architectures is clear module
boundaries and independently testable responsibilities.

## Pattern Defence

| Pattern or Principle | Why It Was Used | Evidence |
|---|---|---|
| Layered architecture | Separates UI, use cases, rules, and setup data. | Package layout, architecture guide, boundary test. |
| MVC-style separation | Domain is model, Swing is view, application commands/adapters coordinate input. | Swing panels render `GameSnapshot` and submit commands. |
| Command | Player actions are explicit write requests with validation and result handling. | `GameCommand` and concrete command classes. |
| Observer | Multiple UI panels refresh without domain depending on Swing. | `GameStateListener` and snapshot publication. |
| Strategy | Behaviour varies for route requirements and shuffle determinism. | `RouteRequirement` and `ShuffleStrategy`. |
| Factory | London board/deck setup is data-heavy and must stay out of UI. | `BoardFactory`, `DeckFactory`, and `GameFactory`. |
| DTO/Snapshot | UI receives immutable read models, not mutable domain objects. | `GameSnapshot`, player/route/ticket/final-score snapshots. |
| Constructor dependency injection | Tests and setup pass dependencies explicitly. | Shuffle/scoring collaborators and factories. |
| ADRs | Significant choices record alternatives and consequences. | `docs/adr/`. |

## Strong Answers To Likely Questions

Why are rules outside Swing?

Swing is a presentation technology. If button handlers decide route legality or scoring, the rules
cannot be tested without the GUI and a second UI could bypass them. This project therefore sends
commands to the application layer and lets domain objects enforce the rules.

Why use commands instead of calling domain methods directly from panels?

Commands make user intention explicit and testable. They also give a consistent result model for
success/failure messages and keep Swing from knowing the domain mutation sequence.

Why use Strategy?

There is real variation. Grey and coloured routes validate payments differently, and shuffling must
be random in production but deterministic in tests. Strategy removes repeated type conditionals and
makes tests stable.

Why avoid Singleton?

Singleton would hide global state, make deterministic tests harder, and prevent multiple isolated
game instances. Constructor injection makes dependencies visible.

What trade-off did the architecture accept?

Layers, commands, snapshots, and ADRs add some boilerplate. The trade-off is worthwhile because the
rules are independently testable, UI changes are safer, and Sprint 3 extensions have clear places to
land.

Why not implement every lecture pattern?

Patterns are only used where they solve a concrete problem. Microservices, service mesh, IPS, and
full client-server deployment solve distributed-system problems that this local game does not have.
Adding them would be speculative generality.

## Quality Evidence To Cite

- `ArchitectureBoundaryTest` verifies no forbidden Swing/AWT imports outside UI.
- Domain/application tests cover core rule behaviour without rendering Swing.
- `FixedOrderShuffleStrategy` makes random card behaviour deterministic in tests.
- Factories centralise the 17 locations, 43 printed routes, 44 transportation cards, and 20
  destination tickets.
- ADRs explain pattern choices and rejected alternatives.
- `docs/progress_tracker.md` records verification status and known remaining work.
- `docs/known_defects.md` records residual defects or risks.

## Refactoring Discussion Template

Use this shape for the future anti-pattern/refactoring section:

```text
In Sprint 2, <anti-pattern> occurred in <class/method>. It was problematic because <specific design
harm in this codebase>. I applied <refactoring technique> by moving/extracting <responsibility> into
<new owner>. This improved <cohesion/coupling/readability/extensibility/testability> because
<concrete before/after>. The behaviour is protected by <tests/checks>.
```

## Final Pre-Submission Questions

- Can I point to where route-claiming validation lives?
- Can I prove domain rules run without Swing?
- Can I explain each pattern with the concrete problem it solves?
- Can I name at least one rejected pattern and why?
- Can I show how the class diagram and implementation correspond?
- Can I show tests for graph scoring, destination tickets, turn flow, and invalid commands?
- Can I explain a trade-off honestly rather than claiming the design is free?

