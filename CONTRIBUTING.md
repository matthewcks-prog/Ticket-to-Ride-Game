# Contributing — Ticket to Ride: London

## Coding Standard

All Java source code must conform to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Key points:

- **Indentation:** 2 spaces (no tabs).
- **Column limit:** 100 characters.
- **Braces:** K&R style (opening brace on same line).
- **Naming:** `UpperCamelCase` for classes, `lowerCamelCase` for methods and variables, `UPPER_SNAKE_CASE` for constants.
- **Javadoc:** Required on all public classes and public methods. Use `@param`, `@return`, and `@throws` tags.
- **Imports:** No wildcard imports. Organise imports: static imports first, then regular imports, both alphabetically.
- **One top-level class per file.**

## Branching Strategy

- `main` — stable, passing code only.
- `feature/<name>` — new features (e.g., `feature/claim-route`, `feature/board-rendering`).
- `feature/sprint3/<name>` — Sprint 3 extensions (e.g.,
  `feature/sprint3/required-extension-introducing-ferries`).
- `fix/<name>` — bug fixes.
- `refactor/<name>` — refactoring without feature changes.

Merge feature branches into `main` with `git merge --no-ff` after `mvn verify`
passes, so merge commits preserve branch boundaries in history. Record merge
details in `docs/git-branching-evidence.md`.

## Commit Style

Use descriptive commit messages that explain what work is being committed.

Format:
```
<type>: <short summary>

<optional body explaining why>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`.

Examples:
```
feat: implement route claiming with coloured and grey requirements
test: add unit tests for double-route restrictions in 2-player games
docs: add ADR-002 for Command pattern usage
refactor: extract RouteScoreTable from ScoreCalculator
```

## Testing Expectations

- Run `mvn verify` before merging. It compiles, runs tests, packages the jar, and enforces
  Google Java Style through Checkstyle.
- All domain logic must have unit tests (JUnit 5).
- Domain and application rule tests must not depend on Swing, file I/O, or network. Architecture
  boundary tests may read source files when the dependency rule itself is the subject under test.
- Use `FixedOrderShuffleStrategy` for deterministic test behaviour.
- Test both success and failure paths for every game rule.
- Aim for tests covering all five key game functionalities and game setup.

## Architecture Rules

- **No game logic in Swing UI classes.** See `architecture_guide.md`.
- **No God Classes.** Keep classes small and focused.
- **Constructor injection for dependencies.** No hidden globals.
- **Document design decisions in ADRs** when introducing or changing patterns.

## Design Decision Documentation

When making a significant design choice, create an Architecture Decision Record (ADR) in the `docs/adr/` folder using the template in `architecture_guide.md` Section 13.
