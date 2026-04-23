# Changelog

All notable changes to this project are documented here. The format is
loosely based on [Keep a Changelog](https://keepachangelog.com/) and
this project follows [Semantic Versioning](https://semver.org/).

## [0.3.1] &mdash; unreleased

Dependency-and-tooling release. The five seeded locale bundles
(German, French, Spanish, Italian, Japanese) are the only
user-visible behaviour change. The publishing-plugin upgrade is
the headline reason this release exists separately rather than
being rolled into the next feature release: it isolates the
plugin upgrade so any release-time regression has a single
suspect.

### Added

- **Five locale bundles** under
  `src/main/resources/io/github/openhistoricalmap/edtf/format/`:
  `messages_de`, `messages_es`, `messages_fr`, `messages_it`,
  `messages_ja`. Bootstrap translations seeded for the next
  Transifex sync; future updates flow through `tx pull` /
  `tx push` rather than direct file edits.
- README's Ant + Ivy section reframed as the canonical reference
  for any downstream Ant consumer (not JOSM-specific), with a
  pointer at `smoke/` as a complete worked example.

### Changed

- **`actions/checkout` 4 &rarr; 6** and **`actions/setup-java` 4
  &rarr; 5** in all three GitHub Actions workflows; both now
  natively use Node.js 24 so the redundant
  `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24` env var was removed.
- **`junit-bom` 5.11.4 &rarr; 6.0.3.** All 367 tests still pass
  unchanged (verified locally before the merge).
- **`central-publishing-maven-plugin` 0.7.0 &rarr; 0.10.0.** The
  primary motivation for cutting this release as a standalone
  patch &mdash; it isolates the publish-time plugin upgrade from
  any future feature work.
- **`maven-source-plugin` 3.3.1 &rarr; 3.4.0**,
  **`maven-surefire-plugin` 3.5.3 &rarr; 3.5.5**,
  **`maven-gpg-plugin` 3.2.7 &rarr; 3.2.8**.
- README dependency snippets updated from `0.2.0` to `0.3.0`.

### Fixed

- **Smoke test** at `smoke/src/Smoke.java` was calling
  `i.upper()` on an `EdtfTemporal`-typed variable, which doesn't
  compile against the published artefact (`upper()` is on the
  permitted subtype `EdtfInterval`, not on the sealed interface).
  Replaced with `i.type()` so the smoke source stays on the
  stable-since-0.2.0 API surface and works against any future
  release without modification.

[0.3.1]: https://github.com/OpenHistoricalMap/edtf-java/releases/tag/v0.3.1

## [0.3.0] &mdash; 2026-04-23

Closes the three v0.2.0 follow-up issues. Adds locale-aware
formatting, a real Ant/Ivy consumption smoke test, and three
hand-curated test-vector TSVs.

### Added

- **`EdtfFormatter`** in the new
  `io.github.openhistoricalmap.edtf.format` package &mdash; a
  locale-aware human-readable formatter for every supported
  `EdtfTemporal` subtype. Default English bundle ships at
  `messages.properties`; translations are managed via Transifex.
  Examples:
  - `EdtfFormatter.forLocale(Locale.US).format(Edtf.parse("2020-05"))`
    &rarr; `"May 2020"`
  - `format(Edtf.parse("199"))` &rarr; `"the 1990s"`
  - `format(Edtf.parse("2020/2021"))` &rarr; `"2020 to 2021"`
  - `format(Edtf.parse("2020-21"))` &rarr; `"Q1 2020"`
- **Three new test-vector TSVs** under `src/test/resources/vectors/`:
  - `loc-spec.tsv` &mdash; curated from the Library of Congress
    EDTF specification page (Levels 0-2).
  - `iso8601-2.tsv` &mdash; curated from the ISO 8601-2:2019
    standard's example callouts, restricted to the EDTF/LoC
    implicit-form profile.
  - `edge-cases.tsv` &mdash; hand-curated worst-case inputs with
    a 7th `valid` column, including INVALID rows that assert
    `EdtfParseException`.
- **Ant/Ivy consumption smoke test** at `smoke/` &mdash; a
  self-contained Ant + Ivy project that resolves the published
  artefact from Maven Central, compiles a small program that
  calls `Edtf.parse` and `EdtfFormatter`, and asserts on its
  output. Daily `smoke.yml` GitHub Actions workflow runs it
  against the latest release.
- **Module export**: `io.github.openhistoricalmap.edtf.format` is
  now exported from the JPMS module descriptor.
- 358 unit tests (up from 247 in 0.2.0).

### Documented divergences

No new divergences from edtf.js. The four documented in 0.2.0 still
apply.

[0.3.0]: https://github.com/OpenHistoricalMap/edtf-java/releases/tag/v0.3.0

## [0.2.0] &mdash; 2026-04-23

First public release on Maven Central. Covers EDTF Levels 0, 1, and most
of Level 2.

### Added

- **Level 0** parsing: ISO 8601-1 dates, datetimes (minute, second, and
  millisecond precision), centuries.
- **Level 1** parsing: uncertain (`?`), approximate (`~`), and combined
  (`%`) markers; `Y`-notation for five-or-more-digit years; seasons
  (codes 21&ndash;24); intervals with bounded, open (`..`), and unknown
  endpoints; `EdtfYear`, `EdtfSeason`, `Endpoint` sealed type, and
  `EdtfInterval` types.
- **Level 2** parsing: non-progressive partial X-mask patterns in any
  YYYY/MM/DD position; sets (`[2020,2021]`) and lists
  (`{2019..2021}`) including consecutive `start..end` members and
  `earlier`/`later` markers; extended season codes 25&ndash;41
  (hemispheric variants, quadrimesters, half-year divisions);
  three-digit decade notation (`199`, `199?`); `Y`-notation
  exponential (`Y1E5`) and significant-digits (`Y12345S3`) forms;
  positional UA markers on individual date components
  (`2020?-05~`, `?2020-%05`).
- **Comparison and bounds**: `EdtfTemporal#compareTo`,
  `EdtfTemporal#covers`, and `long`-millisecond `min` / `max` for every
  supported type.
- **Canonical serialization**: every type implements `toEdtfString()`
  in a form that round-trips with edtf.js for the supported subset.
- **Build and CI**: Maven build targeting Java 17 bytecode, JPMS
  module descriptor, GitHub Actions CI matrix (Java 17 + 21), release
  workflow that signs with GPG and publishes via Sonatype Central
  Portal, Dependabot, issue and PR templates, Contributor Covenant
  code of conduct, AI-assisted-development disclosure in
  `ATTRIBUTION.md`.
- **Test coverage**: 247 unit tests including a generated
  parity-vector harness comparing against `edtf.js` v4.11.0 output.

### Known deviations from edtf.js

Documented in code on the affected types. Summary:

- **Datetime canonical form** is normalised to UTC and full
  millisecond precision (matching edtf.js's `toISOString` output);
  the original timezone offset is preserved on the in-memory value
  via `EdtfDate#timeZone()` but not emitted by `toEdtfString()`.
- **Datetime atomicity**: minute, second, and millisecond precision
  values report `min == max` (atomic instants), matching edtf.js.
- **Season codes 21&ndash;24** use calendar-quarter bounds
  (Q1=Jan&ndash;Mar, etc.) rather than meteorological seasons,
  matching the upstream reference.
- **Consecutive list members** report bounds spanning
  `start.min..end.max` (semantically the full range). edtf.js
  reports `start.max` for the upper bound, which we consider an
  upstream bug and diverge from.

### Deferred to a future release

- Locale-aware formatting via `ResourceBundle` (canonical EDTF
  rendering only at this release).
- Ant/Ivy consumption smoke test against a real JOSM plugin
  scaffold.

[0.2.0]: https://github.com/OpenHistoricalMap/edtf-java/releases/tag/v0.2.0
