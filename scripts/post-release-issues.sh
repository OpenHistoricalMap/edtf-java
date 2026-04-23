#!/usr/bin/env bash
# One-shot script to open the three follow-up issues for v0.2.0
# deferred work and create the v0.2.0 GitHub Release. Run from the
# repo root on a machine where `gh auth status` is logged in:
#
#   ./scripts/post-release-issues.sh
#
# After running, this script can be deleted (or kept around as a
# template for future post-release housekeeping).

set -euo pipefail

REPO="OpenHistoricalMap/edtf-java"

echo "Creating issue: localized formatting..."
gh issue create -R "$REPO" \
    --title "Add locale-aware formatting (ResourceBundle / Transifex)" \
    --label "enhancement" \
    --body "$(cat <<'EOF'
The v0.2.0 release ships canonical EDTF rendering only via
`toEdtfString()`. Locale-aware pretty-printing was deferred.

## Scope

- An `EdtfFormatter` (or method on `EdtfTemporal`) that produces a
  human-readable rendering for a given `Locale`, e.g. "May 2020"
  for `EdtfDate.ofYearMonth(2020, 5)` in `Locale.US`.
- Source strings managed via `ResourceBundle`, with the canonical
  English file at
  `src/main/resources/io/github/openhistoricalmap/edtf/format/messages.properties`
  (already present as a placeholder).
- Translations driven by Transifex per the existing `.tx/config`.
  Contributors don't hand-edit `messages_<locale>.properties`;
  they pull from Transifex.
- Reasonable defaults for the qualifier and mask cases:
  uncertain, approximate, mixed (`%`), partially-unspecified
  dates, intervals, sets, lists.

## Out of scope

- Calendar-system conversions (Hijri, Hebrew, etc.).
- Time-of-day localisation beyond what `java.time.format` already
  provides.

## References

- README's "Still deferred" section
- `CONTRIBUTING.md` Localization section
- Upstream pattern: `edtf.js/src/format.js` and `edtf.js/locale-data/`
EOF
)"

echo "Creating issue: Ant/Ivy smoke test..."
gh issue create -R "$REPO" \
    --title "Add Ant/Ivy consumption smoke test against a JOSM plugin" \
    --label "enhancement,testing" \
    --body "$(cat <<'EOF'
The v0.2.0 release publishes to Maven Central but the end-to-end
consumption path from a real Ant + Ivy build (the JOSM plugin
scenario the library was originally written for) has not been
verified.

## Scope

A small Ant + Ivy project (could live in a sibling folder or a
separate repo) that:

1. Declares a dependency on `io.github.openhistoricalmap:edtf:0.2.0`
   in its `ivy.xml`.
2. Resolves it via `ivy:retrieve` from Maven Central.
3. Compiles a one-line Java program that calls
   `Edtf.parse("2020-XX")`, runs it, and asserts the output.

## Acceptance

Smoke-test build green on at least one CI run (could be the JOSM
plugin's own CI when it adopts the dependency, or a dedicated
`smoke/` directory in this repo with its own CI workflow).

## References

- README "Still deferred" section.
- The JOSM plugin that consumes this library lives separately.
EOF
)"

echo "Creating issue: spec test vector files..."
gh issue create -R "$REPO" \
    --title "Add LoC spec, ISO 8601-2, and edge-case test vector TSVs" \
    --label "enhancement,testing" \
    --body "$(cat <<'EOF'
The current generated parity vectors
(`src/test/resources/vectors/level0,1,2.tsv`) cover edtf.js
behaviour but not the authoritative specifications directly. Three
hand-curated TSVs are planned to close that gap:

## 1. `loc-spec.tsv`

Rows from the Library of Congress EDTF specification examples at
<https://www.loc.gov/standards/datetime/>. Same TSV shape as the
generated files (`input \\t type \\t level \\t min \\t max \\t edtf`).

## 2. `iso8601-2.tsv`

Rows from ISO 8601-2:2019 (and Amendment 1, 2025) clause examples.
The PDFs live locally at `iso/` (gitignored, copyrighted); cite
clause numbers in TSV comments so reviewers can cross-reference
without redistributing the standard.

## 3. `edge-cases.tsv`

Worst-case inputs that aren't naturally covered by the generated
or spec vectors:

- Year `0000` (= 1 BCE), `-0001`, `-9999`.
- Leap-year Feb 29 at century boundaries (`1900-02-29` invalid,
  `2000-02-29` valid).
- Unicode minus sign `\u2212` vs ASCII hyphen.
- Timezones at `\u00b114:00`, `Z` vs `+00:00`, `24:00:00`.
- Y-notation extremes including overflow of `long` epoch ms.
- Both-sided UA markers, deeply qualified dates.
- Deliberately invalid: `2020-13-01`, `2020-02-30`, mismatched
  brackets.

Add VALID and INVALID rows, with the test harness asserting that
INVALID rows throw `EdtfParseException`.

## Acceptance

Each TSV in `src/test/resources/vectors/`, with a matching
`@TestFactory` method in
`src/test/java/.../vectors/GeneratedVectorsTest.java`. All rows
green on `mvn -B verify`.

## References

- `CONTRIBUTING.md` Testing strategy section.
- Existing `GeneratedVectorsTest` for the TSV format.
EOF
)"

echo "Creating GitHub Release v0.2.0..."
gh release create v0.2.0 -R "$REPO" \
    --title "v0.2.0 — L0 + L1 + L2 EDTF parsing" \
    --notes "$(cat <<'EOF'
First public release on Maven Central. Covers EDTF Levels 0, 1, and
most of Level 2.

**Maven Central**: <https://central.sonatype.com/artifact/io.github.openhistoricalmap/edtf/0.2.0>
**Javadoc**: <https://javadoc.io/doc/io.github.openhistoricalmap/edtf/0.2.0>

## What's included

### Level 0
ISO 8601-1 dates, datetimes (minute, second, and millisecond
precision), centuries.

### Level 1
Uncertain (`?`), approximate (`~`), and combined (`%`) markers;
`Y`-notation for five-or-more-digit years; seasons (codes 21-24);
intervals with bounded, open (`..`), and unknown endpoints.

### Level 2
Non-progressive partial X-mask patterns in any YYYY/MM/DD position;
sets (`[2020,2021]`) and lists (`{2019..2021}`) including
consecutive `start..end` members and `earlier`/`later` markers;
extended season codes 25-41 (hemispheric variants, quadrimesters,
half-year divisions); three-digit decade notation (`199`, `199?`);
`Y`-notation exponential (`Y1E5`) and significant-digits
(`Y12345S3`) forms; positional UA markers on individual date
components (`2020?-05~`, `?2020-%05`).

### Comparison and bounds
`EdtfTemporal#compareTo`, `EdtfTemporal#covers`, and
`long`-millisecond `min` / `max` for every supported type.

### Build & test
247 unit tests including a generated parity-vector harness
comparing against `edtf.js` v4.11.0. Targets Java 17 bytecode,
zero runtime dependencies.

## Documented divergences from edtf.js

1. **Datetime canonical form** is normalised to UTC and full
   millisecond precision; the original timezone offset is preserved
   on the in-memory value via `EdtfDate#timeZone()` but not emitted
   by `toEdtfString()`.
2. **Datetime atomicity**: minute, second, and millisecond
   precision values report `min == max`.
3. **Season codes 21-24** use calendar-quarter bounds
   (Q1 = Jan-Mar, etc.) rather than meteorological seasons.
4. **Consecutive list members** report bounds spanning
   `start.min..end.max`. edtf.js reports `start.max` for the upper
   bound, which we consider an upstream bug and diverge from.

## Deferred to a future release

- Locale-aware formatting via `ResourceBundle`.
- Ant / Ivy consumption smoke test against a real JOSM plugin.

See `CHANGELOG.md` for the full record.
EOF
)"

echo ""
echo "Done. View at:"
echo "  https://github.com/${REPO}/issues"
echo "  https://github.com/${REPO}/releases/tag/v0.2.0"
