# Attribution

## edtf.js

This project is based on and inspired by the JavaScript library **edtf.js**.

* Repository: https://github.com/inukshuk/edtf.js
* Authors: EDTF.js Authors and Contributors
* License: BSD 2-Clause License

This Java implementation reuses concepts, parsing behavior, and overall design patterns from edtf.js. Portions of the logic and test cases may be derived from or informed by that project.

We are grateful to the edtf.js contributors for their work in implementing the Extended Date Time Format (EDTF) specification.

The original edtf.js license is included in this repository as required by its terms.

---

## Notes on Derivation

This project is not a direct line-by-line port of edtf.js. It is a Java implementation that:

* Adapts the EDTF parsing model to Java idioms
* Reinterprets structures where appropriate for the JVM ecosystem
* May differ in internal architecture while maintaining behavioral compatibility

Any errors or deviations from the specification are the responsibility of this project, not the edtf.js authors.

---

## Extended Date Time Format (EDTF)

This project implements the Extended Date Time Format (EDTF) specification as defined by the Library of Congress:

https://www.loc.gov/standards/datetime/

---

## Original Work

All Java-specific implementation in this repository, unless otherwise noted, is:

Copyright (c) 2026 OpenHistoricalMap.org 

---

## AI-assisted development

This project was developed with substantial assistance from Claude
(Anthropic's AI assistant), via both the Claude web interface and
Claude Code.

### What Claude contributed

- **Research and analysis.** Read through the upstream `edtf.js` library
  (grammar, types, bitmask, parser, tests) and summarized its public API,
  data model, and notable complexity for porting to Java. Checked JOSM's
  current Java baseline (JRE 11+, compiled as Java 11 bytecode) to inform
  compatibility decisions.
- **Implementation plan.** Drafted the phased plan at
  `/home/agent/.claude/plans/` including architectural decisions —
  Java 17 target, hand-written level-ordered recursive-descent parser,
  sealed-interface-based `EdtfTemporal` hierarchy, `Endpoint` sealed
  type for interval bounds, `BigInteger` for `Year` values that exceed
  `long` epoch range — and the reasoning behind each. Flagged
  specification-priority rules (ISO 8601-2:2019 + Amd 1:2025 > LoC
  drafts > edtf.js), grammar ambiguity, and BCE/leap-year edge cases.
- **Project scaffolding (Phase 1).** Wrote the initial `pom.xml` with
  Maven Central publishing configuration, `module-info.java`,
  `package-info.java`, `README.md`, `CONTRIBUTING.md`,
  `CODE_OF_CONDUCT.md`, `SECURITY.md`, `REFERENCES.md`,
  `LICENSE-edtf.js.txt`, GitHub Actions workflows (CI + release), issue
  and PR templates, Dependabot config, `.tx/config` for Transifex, a
  minimal `Edtf` façade stub and `PlaceholderTest`, and the
  `scripts/fetch-reference.sh` helper. Proposed the `.gitignore`
  additions for `iso/`, `reference/`, and `target/`.
- **Subsequent phases** (Bitmask, types, L0–L3 parser, formatting,
  release hardening) will be co-authored by Claude under the same
  review discipline and carry the same `Co-Authored-By` trailer. This
  section will be updated as substantive new contributions land.

### What the human author contributed

- Project scope and direction
- Review and testing
- Final approval on all code, messages, and behavior

### Reviewing AI contributions

All code in this repository has been reviewed and tested by the
author. Bugs, design flaws, and behavior issues are the author's
responsibility; the presence of AI assistance in development does
not transfer authorship or liability.

Commits made directly by Claude Code carry a `Co-Authored-By: Claude
<noreply@anthropic.com>` trailer in the commit message, making AI
involvement traceable at the commit level.

### Why this notice exists

There is no established convention yet for disclosing AI assistance
in open-source projects. This notice exists because:

1. This plugin encodes opinions about OpenHistoricalMap tagging
   conventions that affect other mappers' work. Readers deserve to
   know that those opinions were developed in conversation with an
   AI, even though the author made the final calls.
2. Transparency about development process is consistent with the
   OSM/OHM community's values around data provenance and open
   contribution.
3. If you are considering using this code in a context where AI
   provenance matters to you, you should know.

### Questions or concerns

If you have concerns about AI-assisted code in this project, please
open an issue on the project's repository. Contributions, bug
reports, and pull requests are welcome regardless of whether they
involve AI tooling; please disclose AI involvement in your own
contributions if it was substantial.
