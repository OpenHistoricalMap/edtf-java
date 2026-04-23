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

Through the v0.2.0 release, Claude's contributions cover the full
arc of the port:

- **Research and analysis.** Read through the upstream `edtf.js`
  library (grammar, types, bitmask, parser, tests) and summarised
  its public API, data model, and notable complexity. Checked JOSM's
  current Java baseline (Java 11 bytecode) to inform compatibility
  decisions.
- **Implementation plan.** Drafted a phased plan covering the
  architectural decisions: Java 17 target, hand-written level-ordered
  recursive-descent parser, sealed-interface-based `EdtfTemporal`
  hierarchy with eight permitted concrete types, `Endpoint` sealed
  type for interval bounds, `BigInteger` for `Year` values that
  exceed `long` epoch range, `Bitmask` ported bit-for-bit from the
  upstream JS, specification-priority rules (ISO 8601-2:2019 +
  Amd 1:2025 > LoC drafts > edtf.js), and the BCE / leap-year /
  grammar-ambiguity edge cases they imply.
- **Project scaffolding.** `pom.xml` with Maven Central publishing
  configuration, JPMS module descriptor, `package-info.java`,
  README, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`,
  `REFERENCES.md`, `CHANGELOG.md`, `LICENSE-edtf.js.txt`, GitHub
  Actions CI and release workflows, issue and PR templates,
  Dependabot, `.tx/config` for Transifex, the `scripts/`
  helpers (`fetch-reference.sh`, `generate-vectors.mjs`), and the
  `.gitignore` additions for `iso/`, `reference/`, and `target/`.
- **`Bitmask` port.** A faithful Java port of the 306-line
  `bitmask.js` including the leap-February correction, the DX / XD /
  MX / XM partial-mask branches, and the qualified-position table.
  Validated bit-for-bit by a hand-translated JUnit 5 suite.
- **Type hierarchy.** All eight `EdtfTemporal` concrete types
  (`EdtfDate`, `EdtfYear` with `BigInteger` and exponential /
  significant-digits, `EdtfDecade`, `EdtfCentury`, `EdtfSeason` with
  L1 quarter and L2 extended codes 25-41, `EdtfInterval` with the
  `Endpoint` sealed type, `EdtfList`, `EdtfSet` with `ListMember`
  including consecutive ranges).
- **Parsers.** `Cursor` utility plus `L0Parser`, `L1Parser`, and
  `L2Parser` with positional UA markers, partial X-masks, and the
  `Edtf.parse` lowest-level-wins facade. Around 247 unit tests
  including a generated parity-vector harness against edtf.js.
- **Documentation and release.** Drafted every commit message;
  identified and documented the four deliberate divergences from
  edtf.js; walked the human author through the Sonatype Central
  Portal namespace claim, GPG keypair generation, and tag-driven
  release path.

Future substantive contributions to this section will be appended
as they land. The full per-commit history is available at
`git log --oneline`.

### What the human author contributed

- Project scope, direction, and the OpenHistoricalMap-specific use
  case that drives the design.
- Architectural decisions where Claude offered alternatives:
  Maven Central group ID (`io.github.openhistoricalmap`), Java 17
  bytecode target, hand-written parser over ANTLR.
- The Sonatype Central Portal account, the GPG key generation, the
  GitHub repository creation and namespace claim, and the manual
  Publish click for the v0.2.0 release.
- Review of every code change, every commit message, every
  documentation file, and every behavioural decision before it
  landed in `main`.

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

1. This library encodes opinions about how EDTF strings (which
   parameterise OpenHistoricalMap tagging conventions) are parsed
   and compared. Readers deserve to know that those opinions were
   developed in conversation with an AI, even though the author
   made the final calls.
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
