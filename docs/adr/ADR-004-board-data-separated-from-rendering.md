# ADR-004: Board Data Separated from Rendering

## Status
Accepted

## Context
The board domain model must remain independent from Swing and from visual map coordinates. The
London board needs approximate station positions, label placement, route lane offsets, and curve
hints for rendering, but those values are presentation concerns. Mixing them into `Board`,
`Location`, `Route`, or Swing paint methods would either pollute the domain with UI data or make
the drawing code hard to inspect and adapt for a future board configuration.

## Decision
Introduce `BoardViewModel` in the UI layer as the adapter between immutable application snapshots
and board rendering. It maps location identifiers to normalized coordinates from
`london_board_layout.md`, route identifiers to lane offsets and curve hints, and claimed routes to
the claiming player's display colour.

`BoardPanel` receives `GameSnapshot`, builds a `BoardViewModel`, and delegates drawing to
`BoardRenderer`, `RouteRenderer`, and `LocationRenderer`. The renderers draw from the view model and
return route hit shapes for hover detection. They do not validate route claims, mutate game state, or
inspect domain objects directly.

## Alternatives Considered
1. Store coordinates on domain `Location` and route layout data on domain `Route` - rejected because
   the domain model would start depending on one Swing presentation of the London board.
2. Hard-code coordinates directly in `BoardPanel.paintComponent` - rejected because the panel would
   become a difficult-to-test rendering God Class and future board/layout changes would require
   editing imperative painting code.
3. Load route geometry directly from Markdown at runtime - rejected for this phase because the
   authoritative layout is stable, tests should not depend on file I/O, and a later data-source
   adapter can replace the in-code geometry table without changing domain rules.

## Consequences
Positive:
- Domain and application layers remain independent of Swing and rendering coordinates.
- Board rendering is data-driven from a dedicated UI model rather than scattered through paint code.
- Hover detection reuses renderer-generated shapes, keeping route input translation in the UI layer.
- Normalized coordinates scale with the panel size and support future board-specific geometry.

Negative:
- Route geometry is still encoded in Java constants, so adding a new board requires another geometry
  source or adapter.
- Fine-tuning visual placement requires editing view-model geometry data and checking the rendered
  Swing board.
