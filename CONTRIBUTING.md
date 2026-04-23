# Contributing to edtf-java

Thank you for your interest in contributing! This project is a Java port
of [edtf.js][edtf-js] built to support [OpenHistoricalMap][ohm] tooling
(including a JOSM plugin) and the broader JVM ecosystem.

## Getting started

### Prerequisites

- JDK 17 or newer (tested on 17 and 21)
- Maven 3.9+
- Git

### Build and test

```bash
git clone https://github.com/OpenHistoricalMap/edtf-java.git
cd edtf-java
git config commit.template .gitmessage   # one-time setup
mvn -B verify                            # compile + test
```

### Running against the upstream reference

To compare against [edtf.js][edtf-js] during development:

```bash
./scripts/fetch-reference.sh
```

This clones the pinned upstream commit into `reference/edtf.js/` (which
is gitignored).

## How we work

### Coding style

- Target Java 17 bytecode (`<release>17</release>`).
- Zero runtime dependencies in the core library. Test-only deps (JUnit 5,
  AssertJ) are allowed.
- Prefer sealed interfaces, records, and pattern matching over `instanceof`
  chains.
- Public API lives under `io.github.openhistoricalmap.edtf.*`. Anything
  under `.parser` or `.internal` is implementation detail and not exported
  from the JPMS module.
- Javadoc is required for public classes and methods. Follow existing
  wording for consistency.

### Specification priority

When behavior is in dispute, the priority order is:

1. **ISO 8601-2:2019** (and Amendment 1, 2025)
2. Library of Congress EDTF specification
3. `edtf.js` behavior

Deviations from edtf.js are acceptable when ISO or LoC disagree; document
them in Javadoc on the affected type.

### Commit messages

- Use imperative mood: "Add Bitmask min()" not "Added Bitmask min()".
- Reference the issue or plan phase when relevant.
- Every commit should carry the `Co-Authored-By: Claude
  <noreply@anthropic.com>` trailer *when AI assistance was used*. The
  repository ships a `.gitmessage` template that pre-populates this; wire
  it in once with `git config commit.template .gitmessage`. If your
  contribution was made without AI assistance, remove the trailer before
  committing.

### Pull requests

Every PR should:

- Explain the motivation briefly.
- Include tests for new behavior (unit tests under `src/test/java/`, vector
  rows under `src/test/resources/vectors/` where applicable).
- Keep `mvn -B verify` green.
- Use the provided PR template (`.github/PULL_REQUEST_TEMPLATE.md`) which
  includes an AI-disclosure checkbox.

## AI-assisted contributions

This project was developed with substantial AI assistance, documented in
[`ATTRIBUTION.md`](ATTRIBUTION.md). We ask contributors to follow the
same transparency:

- If an AI assistant (Claude, Copilot, Cursor, etc.) materially contributed
  to your PR, check the "AI assistance" box in the PR template and add a
  `Co-Authored-By:` trailer in your commit(s).
- Review every line you submit. The presence of AI assistance does not
  transfer responsibility for correctness to the tool.
- Follow the specification priority order above. AI tools are imperfect
  at cross-referencing ISO 8601-2 specifically; verify edge cases against
  the LoC examples at minimum.

## Localization

User-facing strings (format templates, error messages) live in Java
`ResourceBundle` files under
`src/main/resources/io/github/openhistoricalmap/edtf/format/`. The
canonical English source is `messages.properties`; translations are
named `messages_<locale>.properties`.

Translations are managed via [Transifex](https://app.transifex.com/):

```bash
tx pull -a         # pull all translations from Transifex
tx push -s         # push source strings to Transifex
```

Do not commit translations by hand; let Transifex own the flow. If you
spot a mistranslation, file an issue or contact the locale team on
Transifex rather than patching the file directly.

## Testing strategy

- **Unit tests** per class (e.g., `EdtfDateTest`,
  `L0ParserTest`, `L1ParserTest`, `L2ParserTest`,
  `EdtfFormatterTest`).
- **Generated parity vectors** under
  `src/test/resources/vectors/{level0,level1,level2}.tsv`,
  produced by `scripts/generate-vectors.mjs` from the upstream
  edtf.js. `GeneratedVectorsTest` reads them and asserts that
  `Edtf.parse(...)` agrees with edtf.js on type, level, min, max,
  and round-trip rendering.
- **Spec vectors**: `loc-spec.tsv` (Library of Congress EDTF
  examples) and `iso8601-2.tsv` (ISO 8601-2:2019 standard
  examples) under the same path, also driven by
  `GeneratedVectorsTest` `@TestFactory` methods.
- **Edge cases**: `edge-cases.tsv` &mdash; worst-case inputs with
  a 7th `valid` column. INVALID rows assert
  `EdtfParseException`; VALID rows round-trip. Add new
  pathological inputs here as you find them; the harness picks
  them up automatically on the next test run.

## Security

Please report security issues per [`SECURITY.md`](SECURITY.md), not in
public issues.

## License

By contributing, you agree that your contributions will be licensed
under the [BSD 2-Clause License](LICENSE) that covers this project.

[edtf-js]: https://github.com/inukshuk/edtf.js
[ohm]: https://www.openhistoricalmap.org/
