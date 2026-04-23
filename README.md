# edtf-java

[![Maven Central](https://img.shields.io/maven-central/v/io.github.openhistoricalmap/edtf?logo=apachemaven&label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.openhistoricalmap/edtf)
[![Javadoc](https://javadoc.io/badge2/io.github.openhistoricalmap/edtf/javadoc.svg)](https://javadoc.io/doc/io.github.openhistoricalmap/edtf)
[![CI](https://github.com/OpenHistoricalMap/edtf-java/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/OpenHistoricalMap/edtf-java/actions/workflows/ci.yml)
[![License: BSD-2-Clause](https://img.shields.io/badge/License-BSD%202--Clause-blue.svg)](LICENSE)

A Java library for parsing, comparing, and rendering [Extended Date/Time
Format (EDTF)][edtf] strings, per **ISO 8601-2:2019** (with Amendment 1,
2025) and the Library of Congress EDTF specification.

This is a port of the JavaScript library [edtf.js][edtf-js] to the JVM,
targeting Java 17+ with zero runtime dependencies.

> **Status**: First public release (0.2.0). Available on Maven
> Central.

## Quick start

```java
import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfTemporal;

EdtfTemporal t = Edtf.parse("2020-XX");
System.out.println(t.toEdtfString()); // "2020-XX"
System.out.println(t.min());          // epoch ms for 2020-01-01T00:00:00Z
System.out.println(t.max());          // epoch ms for 2020-12-31T23:59:59.999Z
```

## Dependency

Available on Maven Central as of `0.2.0`.

### Maven

```xml
<dependency>
    <groupId>io.github.openhistoricalmap</groupId>
    <artifactId>edtf</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.openhistoricalmap:edtf:0.2.0'
```

### Ant + Ivy (JOSM plugins)

```xml
<dependency org="io.github.openhistoricalmap" name="edtf" rev="0.2.0"/>
```

## EDTF coverage

| Level | Description                                        | Status                 |
|-------|----------------------------------------------------|------------------------|
| 0     | ISO 8601-1 dates, datetimes, centuries             | v0.1 &mdash; done      |
| 1     | Uncertain / approximate / unspecified, Y-notation, | v0.1 &mdash; done      |
|       | seasons, open / unknown intervals                  |                        |
| 2     | Sets, lists, consecutive ranges, partial masking,  | v0.2 &mdash; done      |
|       | extended seasons (25-41), decades, Y exponential,  |                        |
|       | significant-digits, positional UA markers          |                        |
| 3     | Season-to-season intervals (via L1/L2 endpoints);  | v0.2 &mdash; partial   |
|       | further L3-specific forms                          | (planned v0.3)         |

**Locale-aware formatting** ships in v0.3.0 via
`io.github.openhistoricalmap.edtf.format.EdtfFormatter`:

```java
import io.github.openhistoricalmap.edtf.format.EdtfFormatter;
import java.util.Locale;

EdtfFormatter f = EdtfFormatter.forLocale(Locale.US);
f.format(Edtf.parse("2020-05"));     // "May 2020"
f.format(Edtf.parse("199"));         // "the 1990s"
f.format(Edtf.parse("2020/2021"));   // "2020 to 2021"
f.format(Edtf.parse("2020-21"));     // "Q1 2020"
```

Translations are managed via [Transifex](https://app.transifex.com/);
contributors don't hand-edit `messages_<locale>.properties` files.

Comparison (`compareTo`, `covers`) and epoch-millisecond bounds
(`min` / `max`) are implemented for every supported type. Canonical
string rendering matches edtf.js for parity; see
[`CHANGELOG.md`](CHANGELOG.md) for the four documented deviations.

## Building

Requires JDK 17 or newer and Maven 3.9+.

```bash
mvn -B verify              # compile + run tests
mvn -B verify -DskipTests  # compile only
mvn -B package             # build jar
```

The `-Prelease` profile adds source + Javadoc jars, GPG signing, and
Maven Central publishing. Reserved for release automation.

## License

BSD 2-Clause. See [`LICENSE`](LICENSE). Portions derive from
[edtf.js][edtf-js] (BSD 2-Clause, © 2016–2025 EDTF.js Authors and
Contributors). See [`ATTRIBUTION.md`](ATTRIBUTION.md) for details and
[`LICENSE-edtf.js.txt`](LICENSE-edtf.js.txt) for the upstream license.

## AI-assisted development

This project was developed with substantial assistance from Claude
(Anthropic). See [`ATTRIBUTION.md`](ATTRIBUTION.md) for scope and
rationale. Commits with AI involvement carry a `Co-Authored-By: Claude`
trailer; contributors are asked to disclose AI involvement in their own
contributions ([`CONTRIBUTING.md`](CONTRIBUTING.md)).

## References

- [ISO 8601-2:2019][iso] — the authoritative standard
- [Library of Congress EDTF specification][edtf] — free reference
- [edtf.js][edtf-js] — the upstream JS library this ports
- [`REFERENCES.md`](REFERENCES.md) — pinned versions and local spec paths

[edtf]: https://www.loc.gov/standards/datetime/
[edtf-js]: https://github.com/inukshuk/edtf.js
[iso]: https://www.iso.org/standard/70908.html
