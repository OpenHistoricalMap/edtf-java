# Changelog

All notable changes to this project are documented here. The format is
loosely based on [Keep a Changelog](https://keepachangelog.com/) and
this project follows [Semantic Versioning](https://semver.org/).

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
