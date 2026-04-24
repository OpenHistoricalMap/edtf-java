#!/usr/bin/env bash
# One-shot script to open the v0.3.1 follow-up issues. Idempotent:
# safe to re-run. Skips issues whose title already exists, creates
# missing labels first, and continues past individual failures.
#
# Run from a host where `gh auth status` is logged in:
#
#     ./scripts/open-followup-issues.sh
#
# After running, this script can be deleted (or kept as a template
# for future post-release housekeeping).

set -uo pipefail   # NOT -e: keep going past individual failures.

REPO="OpenHistoricalMap/edtf-java"

ensure_label() {
    local name="$1" desc="$2" color="$3"
    if ! gh label list -R "$REPO" --search "$name" --json name --jq '.[].name' \
            | grep -Fxq "$name"; then
        echo "Creating label: $name"
        gh label create "$name" -R "$REPO" --description "$desc" --color "$color"
    else
        echo "Label exists: $name"
    fi
}

# Find an existing issue (open or closed) by exact title match.
# Prints its number, or empty if none.
issue_exists() {
    local title="$1"
    gh issue list -R "$REPO" --state all --search "in:title \"$title\"" \
        --json number,title \
        --jq ".[] | select(.title == \"$title\") | .number" | head -1
}

create_or_skip() {
    local title="$1" labels="$2" body="$3"
    local existing
    existing=$(issue_exists "$title")
    if [[ -n "$existing" ]]; then
        echo "SKIP (already exists, #$existing): $title"
        return
    fi
    echo "Creating: $title"
    gh issue create -R "$REPO" --title "$title" --label "$labels" --body "$body"
}

# 1. Labels.
ensure_label enhancement  "New feature or request"                a2eeef
ensure_label testing      "Tests, vectors, smoke checks"          0e8a16
ensure_label localization "Translation, ResourceBundle, Transifex" c5def5

# 2. Six follow-up issues.

create_or_skip \
    "Formatter: render time-precision datetimes (currently falls back to canonical)" \
    "enhancement" \
    "EdtfFormatter currently falls back to toEdtfString() output for datetime-precision values (MINUTE / SECOND / MILLISECOND on EdtfDate). Add locale-aware human rendering, e.g. 'May 1, 2020 at 10:30 AM UTC' or whatever the locale's DateFormat.LONG produces.

Test coverage: extend EdtfFormatterTest with cases for each datetime precision in en-US and at least one non-English locale."

create_or_skip \
    "Formatter: render partial X-masks (currently falls back to canonical)" \
    "enhancement" \
    "EdtfFormatter currently emits the canonical EDTF string for any value with a non-zero unspecified bitmask. Add locale-aware rendering for the common cases:

- Full year mask: '2020-XX' -> 'sometime in 2020'
- Full month-and-day mask: '2020-XX-XX' -> 'sometime in 2020' (same as above semantically)
- Day-only mask: '2020-05-XX' -> 'sometime in May 2020'
- Year tens digit unspecified: '20XX' -> 'sometime in the 21st century' (or per locale)

Use Bitmask.qualified() positions to decide rendering strategy. Test in EdtfFormatterTest."

create_or_skip \
    "Formatter: render exponential Y-notation (currently falls back to canonical)" \
    "enhancement" \
    "EdtfFormatter falls back to canonical for EdtfYear values that overflow long epoch-ms (Y100000+, Y1E5, etc.). For human display these would be more useful as 'year 100,000', 'year 1,000,000', or scientific-notation phrasings.

Bonus: BCE rendering for very-large-magnitude negative years (e.g., 'Y-100000' -> '100,000 BCE')."

create_or_skip \
    "Optional edtf-jackson companion module for JSON serialization" \
    "enhancement" \
    "Ship a separate Maven module (io.github.openhistoricalmap:edtf-jackson) providing a Jackson Module that serializes EdtfTemporal values as their toEdtfString() form and deserializes via Edtf.parse. Keeps the core jar zero-dep while letting downstream JSON-using consumers add one extra dependency to get round-trip serialization for free.

Skeleton sketch is in the project plan / ATTRIBUTION.md."

create_or_skip \
    "Add jqwik property-based tests for Bitmask and parse-format round-trips" \
    "enhancement,testing" \
    "jqwik is a property-based test library for JUnit. Two valuable property suites:

1. Bitmask: for any random bit pattern, the min() and max() bounds for a given pattern stay within expected month / day ranges, leap-year correction holds, etc.

2. Round-trip: for any EDTF string accepted by Edtf.parse(), Edtf.parse(value.toEdtfString()).equals(value). This would catch any canonical-form regression that the curated TSVs miss."

create_or_skip \
    "Wire Transifex sync workflow once locale bundles have real translations" \
    "enhancement,localization" \
    "v0.3.1 seeds five locale bundles (de, es, fr, it, ja) with bootstrap translations. Once the Transifex project is set up and translators have started contributing real translations, add a GitHub Actions workflow that runs 'tx pull' on a schedule (e.g., weekly) and opens a PR with refreshed messages_<locale>.properties files. CONTRIBUTING.md already documents the contributor flow; this issue is about the automation."

echo ""
echo "Done. Verify at https://github.com/${REPO}/issues"
