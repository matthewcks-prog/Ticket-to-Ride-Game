# ADR-007: Graph Traversal for Ticket Completion

## Status
Accepted

## Context
Final scoring needs to determine whether each player's claimed routes connect the two endpoints on
each Destination Ticket. The same claimed-route graph is also used for the Longest Continuous Path
bonus, where route weights matter and route edges may not be reused.

This logic must remain independent from Swing and from the application command layer. UI code should
only display final scoring results; it must not infer ticket completion or longest-path winners.

## Decision
Use graph traversal in the domain scoring package:

- `TicketCompletionChecker` builds an undirected graph from one player's claimed routes and uses BFS
  reachability to decide whether a ticket is completed.
- `LongestPathCalculator` builds a weighted undirected multigraph and runs DFS from every location,
  tracking used route IDs so vertices may repeat but printed routes cannot be reused.
- `ScoreCalculator` composes those collaborators with `RouteScoreTable`, then produces an immutable
  `FinalScore` containing route points, ticket results, longest-path lengths, bonuses, totals, and
  winners.

Route points are recomputed from claimed routes during final scoring instead of trusting the running
player score. This makes final scoring auditable and keeps `RouteScoreTable` as the single source of
truth for route length points.

## Alternatives Considered
1. Store ticket completion state on `DestinationTicket` - rejected because completion depends on a
   specific player's claimed-route graph and changes over the course of the game.
2. Calculate connectivity in Swing when showing the final screen - rejected because it violates the
   rule that game logic must live outside UI classes.
3. Use Union-Find for ticket completion - rejected for now because BFS is simpler, expressive, and
   fast enough for London's small board while still keeping the graph boundary clear.
4. Use shortest-path algorithms for longest path - rejected because the rule asks for a maximum
   weighted trail, not a shortest or simple path.

## Consequences
Positive: Scoring rules remain testable without Swing.
Positive: Ticket completion and longest-path logic are separated into cohesive collaborators.
Positive: Recomputing route points at final scoring catches drift between claimed routes and running
player scores.

Negative: Longest path DFS is exponential in the number of claimed routes, though the London board is
small enough for this to remain practical.
Negative: `FinalScore` is a richer value object, so callers must choose the breakdown field they need
rather than reading only a total.
