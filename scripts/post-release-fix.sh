#!/usr/bin/env bash
# Recovery script for the v0.2.0 post-release housekeeping. The
# original scripts/post-release-issues.sh halted partway through
# because gh rejected an unknown label. This version is robust:
#
#   - Creates the missing labels first (idempotent).
#   - Skips issues whose title already exists.
#   - Continues past individual failures.
#   - Closes issues immediately after creation since the underlying
#     work has been merged on main as of v0.3.0-SNAPSHOT.
#   - Creates the v0.2.0 GitHub Release if missing.
#
# Run from a host where `gh auth status` is logged in:
#
#     ./scripts/post-release-fix.sh
#
# After it succeeds, this script can be deleted.

set -uo pipefail   # NOT -e: continue past individual failures.

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

# Find an open issue by exact title; print its number, or empty.
issue_number_by_title() {
    local title="$1"
    gh issue list -R "$REPO" --state all --search "in:title \"$title\"" \
        --json number,title \
        --jq ".[] | select(.title == \"$title\") | .number" | head -1
}

create_or_skip() {
    local title="$1" labels="$2" body="$3"
    local existing
    existing=$(issue_number_by_title "$title")
    if [[ -n "$existing" ]]; then
        echo "Issue exists: #$existing $title — skipping create"
        echo "$existing"
        return
    fi
    echo "Creating issue: $title"
    local url
    url=$(gh issue create -R "$REPO" --title "$title" --label "$labels" --body "$body")
    # Extract the issue number from the URL.
    echo "${url##*/}"
}

close_with_comment() {
    local number="$1" comment="$2"
    if [[ -z "$number" ]]; then return; fi
    gh issue close "$number" -R "$REPO" \
        --reason completed --comment "$comment" || true
}

# 1. Labels.
ensure_label enhancement "New feature or request" a2eeef
ensure_label testing     "Tests, vectors, smoke checks" 0e8a16

# 2. The three follow-up issues. All three are now complete on main,
#    so each is created (or matched) and then closed with a pointer
#    to the merging commit.

n1=$(create_or_skip \
    "Add locale-aware formatting (ResourceBundle / Transifex)" \
    "enhancement" \
    "Closed by 0a283cc: EdtfFormatter in io.github.openhistoricalmap.edtf.format with English ResourceBundle and 21 unit tests. Default locale + per-locale variants supported. See CHANGELOG.md v0.3.0 entry.")

n2=$(create_or_skip \
    "Add Ant/Ivy consumption smoke test against a JOSM plugin" \
    "enhancement,testing" \
    "Closed by 1d79c23 + 5d52c16: smoke/ project resolves the published artefact via Ivy from Maven Central, compiles src/Smoke.java against it, and asserts on stdout. .github/workflows/smoke.yml runs it daily. See smoke/README.md.")

n3=$(create_or_skip \
    "Add LoC spec, ISO 8601-2, and edge-case test vector TSVs" \
    "enhancement,testing" \
    "Closed by e742539: loc-spec.tsv, iso8601-2.tsv, and edge-cases.tsv added under src/test/resources/vectors/, with three new @TestFactory methods in GeneratedVectorsTest.")

close_with_comment "$n1" "Closed by 0a283cc; shipped in v0.3.0."
close_with_comment "$n2" "Closed by 1d79c23 + 5d52c16; shipped in v0.3.0."
close_with_comment "$n3" "Closed by e742539; shipped in v0.3.0."

# 3. The v0.2.0 GitHub Release.
if gh release view v0.2.0 -R "$REPO" >/dev/null 2>&1; then
    echo "Release v0.2.0 already exists — skipping."
else
    echo "Creating release v0.2.0"
    gh release create v0.2.0 -R "$REPO" \
        --title "v0.2.0 — L0 + L1 + L2 EDTF parsing" \
        --notes "$(cat <<'EOF'
First public release on Maven Central. Covers EDTF Levels 0, 1, and
most of Level 2.

**Maven Central**: <https://central.sonatype.com/artifact/io.github.openhistoricalmap/edtf/0.2.0>
**Javadoc**: <https://javadoc.io/doc/io.github.openhistoricalmap/edtf/0.2.0>

See [CHANGELOG.md](https://github.com/OpenHistoricalMap/edtf-java/blob/main/CHANGELOG.md)
for the full record, including the four documented divergences from
edtf.js.

## What's included

- **Level 0**: ISO 8601-1 dates, datetimes, centuries.
- **Level 1**: uncertain / approximate / mixed markers, Y-notation,
  seasons (codes 21-24), open / unknown intervals.
- **Level 2**: non-progressive partial X-masks, sets and lists with
  consecutive `start..end` members, extended seasons (25-41),
  three-digit decade notation, Y exponential and significant-digits
  forms, positional UA markers on individual date components.
- 247 unit tests, including a generated parity-vector harness
  against `edtf.js` v4.11.0.
- Java 17 bytecode target, zero runtime dependencies, JPMS module
  descriptor.

## Documented divergences from edtf.js

1. Datetime canonical form normalised to UTC.
2. Datetime atomicity: minute/second/millisecond precision report `min == max`.
3. Season codes 21-24 use calendar-quarter bounds.
4. Consecutive list members report `start.min..end.max`.
EOF
)"
fi

echo ""
echo "Done."
echo "  Issues:   https://github.com/${REPO}/issues?q=is%3Aissue"
echo "  Releases: https://github.com/${REPO}/releases"
