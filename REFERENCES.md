# References

Authoritative specs and upstream sources this project depends on. Files
marked "local-only" are present on the maintainer's machine but are not
committed to the repository.

## Specifications

### ISO 8601-2:2019 — Representations for information interchange — Part 2: Extensions

The official international standard for EDTF. Authoritative.

- Purchase: <https://www.iso.org/standard/70908.html>
- **Local copy (not committed, ISO copyright)**: `iso/ISO_8601-2_2019(en).pdf`

### ISO 8601-2:2019 / Amd 1:2025

Amendment 1, 2025. Contains changes not present in edtf.js v4.11.0.

- Purchase: <https://www.iso.org/standard/> (search "8601-2 amendment")
- **Local copy (not committed, ISO copyright)**: `iso/ISO_8601-2_2019_Amd_1_2025(en).pdf`

### Library of Congress EDTF specification

The free public reference. Predates the formal ISO standardization;
edtf.js was written against this. Useful for citation and linking.

- <https://www.loc.gov/standards/datetime/>

### Priority when sources disagree

ISO 8601-2:2019 + Amd 1:2025 > LoC drafts > edtf.js behavior. Deviations
from ISO behavior are documented in Javadoc on the affected types.

## Upstream library

### edtf.js

The JavaScript library this project ports to Java.

- Repository: <https://github.com/inukshuk/edtf.js>
- License: BSD 2-Clause (see `LICENSE-edtf.js.txt`)
- Pinned version for this port: **4.11.0**
- Pinned commit: `042e298a2ce15d145a6516ee36187c536d8584de`

To clone the upstream source locally for comparison during development,
run `./scripts/fetch-reference.sh`. The clone lands in `reference/` and is
gitignored. The JOSM plugin that consumes this library lives separately;
its build uses ivy.xml to pull this library from Maven Central.
