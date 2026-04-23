# edtf-java

A Java library for parsing, comparing, and rendering [Extended Date/Time
Format (EDTF)][edtf] strings, per **ISO 8601-2:2019** (with Amendment 1,
2025) and the Library of Congress EDTF specification.

This is a port of the JavaScript library [edtf.js][edtf-js] to the JVM,
targeting Java 17+ with zero runtime dependencies.

> **Status**: Early development (0.1.0-SNAPSHOT). Not yet published to Maven
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

Once published:

### Maven

```xml
<dependency>
    <groupId>io.github.openhistoricalmap</groupId>
    <artifactId>edtf</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.openhistoricalmap:edtf:0.1.0'
```

### Ant + Ivy (JOSM plugins)

```xml
<dependency org="io.github.openhistoricalmap" name="edtf" rev="0.1.0"/>
```

## EDTF coverage

| Level | Description                                        | Status                 |
|-------|----------------------------------------------------|------------------------|
| 0     | ISO 8601-1 dates, datetimes, centuries             | v0.1 &mdash; done      |
| 1     | Uncertain / approximate / unspecified, Y-notation, | v0.1 &mdash; done      |
|       | seasons, open / unknown intervals                  |                        |
| 2     | Sets, lists, partial masking, decades, significant | Planned v0.2           |
|       | digits, exponential years                          |                        |
| 3     | Experimental L3 season qualifiers, season-on-both- | Planned v0.3           |
|       | sides intervals                                    |                        |

Comparison (`compareTo`, `covers`) and epoch-millisecond bounds
(`min` / `max`) are implemented for every supported type. Canonical
string rendering matches {@code edtf.js} for parity; see
`ATTRIBUTION.md` for documented deviations.

Formatting and localization land in v0.3+ with English first; additional
locales contributed via [Transifex](https://app.transifex.com/).

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
