# Ant + Ivy consumption smoke test

This sibling project verifies that `io.github.openhistoricalmap:edtf`
is consumable end-to-end from a real Ant + Ivy build &mdash; the
build shape that JOSM plugins use.

The smoke test deliberately lives outside the main Maven module so
that:

- It exercises the *published* artifact from Maven Central, not the
  in-repo classes (no classpath shortcuts).
- Failures here mean &quot;something is wrong with how downstream
  Ant/Ivy consumers see the artifact&quot;, distinct from main-build
  failures.

## What it does

1. `ivy:retrieve` resolves
   `io.github.openhistoricalmap:edtf:${edtf.version}` from Maven
   Central into `lib/`.
2. `javac` compiles `src/Smoke.java`, which calls
   `Edtf.parse("2020-05-15")` and a `2020/..` open-upper interval.
3. `java` runs the compiled class and the script asserts on the
   captured stdout; any deviation exits non-zero.

The smoke source uses only API that has been stable since 0.2.0
(the first published release). The daily workflow therefore
exercises whichever version is currently on Maven Central without
needing to be in lockstep with new feature releases.

## Prerequisites

- Apache Ant 1.10+ on the path.
- Apache Ivy 2.5+ JAR available to Ant. On most installs Ivy ships
  with Ant; otherwise drop `ivy-2.5.x.jar` into `~/.ant/lib/` or
  pass `-lib /path/to/ivy.jar`.
- JDK 17+ on the path.
- Network access to Maven Central.

## Running

```bash
cd smoke
ant test                 # runs the full smoke pipeline
ant -Dedtf.version=0.2.0 test   # pin to a specific version
```

Successful run output (last line):

```
PASSED: smoke test against io.github.openhistoricalmap:edtf:0.2.0
```

## Versioning

`ivy.xml` references `${edtf.version}`. The default in `build.xml`
tracks the most recently released version of this library (bumped
in the same commit that tags a new release). Override on the
command line as shown above to test a specific version, including
SNAPSHOT versions if they're staged.

## CI

`.github/workflows/smoke.yml` runs this against the latest published
release on a daily schedule, so a Maven Central propagation issue
(or a takedown / move) is caught proactively.
