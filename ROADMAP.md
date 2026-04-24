# Roadmap

Forward-looking notes on where this library is headed, plus the
maintenance reminders that don't have a natural home in
[CHANGELOG.md](CHANGELOG.md) (which is backward-looking) or
GitHub Issues (which track concrete work).

This document is a living overview, not a contract. Plans change
when reality demands.

## Near-term &mdash; v0.4.x

Concrete work tracked as
[GitHub Issues](https://github.com/OpenHistoricalMap/edtf-java/issues).
At time of writing, the open enhancements are:

- **`EdtfFormatter` coverage gaps**: time-precision datetimes,
  partial X-masks, and exponential Y-notation currently fall
  back to the canonical EDTF string. Three separate issues, each
  scoped to one form.
- **Optional `edtf-jackson` module** for JSON serialisation,
  shipped as a sibling Maven module so the core jar stays
  zero-dep.
- **`jqwik` property-based tests** for `Bitmask` and for
  `parse(format(x)).equals(x)` round-trips.
- **Transifex sync workflow** once the seeded locale bundles
  (de, es, fr, it, ja) have real translator-supplied content.

If you want to pick one up, see
[CONTRIBUTING.md](CONTRIBUTING.md) for the development flow.

## Medium-term &mdash; toward 1.0

`1.0.0` is when we commit to API stability under SemVer. Before
tagging it:

1. The `EdtfFormatter` coverage gaps above should be closed (or
   explicitly marked as "stays as fallback by design" with
   Javadoc).
2. The four documented divergences from edtf.js
   (CHANGELOG.md "Known deviations from edtf.js") should be
   reconfirmed against the latest spec and either kept or
   resolved.
3. Public Javadoc should be reviewed end-to-end &mdash;
   `mvn -B javadoc:javadoc` warnings down to zero.
4. The `loc-spec.tsv` and `iso8601-2.tsv` vector files should be
   expanded to cover every example in the corresponding
   spec section, not the curated subset shipped today.
5. At least one external consumer (the OHM JOSM plugin counts)
   should have integrated and given feedback.

## Long-term ideas

Not committed, just thinking out loud:

- **Streaming / iterator API** for very long intervals
  (`EdtfTemporal.until(other)`, `between(other)`) so callers can
  walk a date range without materialising every member.
- **More locale bundles** beyond the seeded five.
- **Dedicated parser modes** for spec-strict vs lenient input.

## Maintenance reminders

These have to live somewhere outside `git log`. Update the dates
as you do each rotation.

| Item | Last action | Next action by | Notes |
|------|-------------|----------------|-------|
| Sonatype Central Portal user token | 2026-04-22 (created at 6-month expiry) | **2026-10-22** | Regenerate at <https://central.sonatype.com/account>. Rotate `CENTRAL_USERNAME` / `CENTRAL_PASSWORD` GitHub secrets. |
| GPG signing key | 2026-04-22 (created) | When you generated it &mdash; check `gpg --list-secret-keys --keyid-format=long` | If set with no expiry, no rotation needed but still review annually. |
| JOSM Java baseline | Verified 2026-04-22 (Java 11 bytecode target) | Recheck before any release whose smoke / consumer is on JOSM | If JOSM's baseline rises above 17, this library's `<release>17</release>` becomes a non-issue; if it drops (unlikely), reconsider. |
| `central-publishing-maven-plugin` | 0.10.0 in v0.3.1 | When `0.7 -> 0.10` style minor jump appears in dependabot | Same "no-op release" pattern (see CHANGELOG v0.3.1) keeps the upgrade isolated. |
| `dependabot.yml` ignore rules | None | Review if false-positive PRs become noisy | Currently allows all updates; tighten if a particular dep churns. |

## Done at the v0.3.1 cut

For completeness:

- v0.2.0, v0.3.0, v0.3.1 all live on Maven Central.
- Full L0 + L1 coverage; L2 covers decade, set, list with
  consecutive ranges, partial X-masks, extended seasons 25-41,
  Y-notation exponential and significant-digits, positional UA
  markers; partial L3 via L1/L2-endpoint intervals.
- `EdtfFormatter` with English plus five seeded locales.
- 367 unit tests including TSV parity vectors against
  `edtf.js` v4.11.0, the LoC spec, the ISO 8601-2 spec, and
  hand-curated edge cases.
- Daily smoke workflow exercising the latest-published
  artefact via Ant + Ivy.
- All GitHub Actions on Node 24 native; all Maven plugins on
  latest stable.

See [CHANGELOG.md](CHANGELOG.md) for the version-by-version
record.
