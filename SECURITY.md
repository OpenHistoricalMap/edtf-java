# Security policy

## Supported versions

During the 0.x series, only the latest minor release receives fixes.
Post-1.0, we will maintain the latest two minor releases.

| Version   | Supported |
|-----------|-----------|
| 0.x (latest) | Yes    |
| older 0.x    | No     |

## Reporting a vulnerability

Please **do not** report security issues in public GitHub Issues.

Preferred channel: open a private report via GitHub Security Advisories
on this repository
(<https://github.com/OpenHistoricalMap/edtf-java/security/advisories/new>).

Alternative: email a project maintainer directly. A dedicated
security contact address will be added here ahead of the 1.0.0
release. Until then, please use the GitHub Security Advisory
channel above &mdash; it routes to the repository's owners and is
private.

## What to include

- A description of the issue and its impact.
- Steps to reproduce, or a proof-of-concept input string if the bug is
  parser-related.
- The affected version(s).
- Whether you'd like credit in the advisory.

## Response expectations

We aim to acknowledge reports within **7 days** and to publish a fix or
mitigation within **90 days** of confirmation. If a coordinated disclosure
timeline is needed, we will work with the reporter to agree on one.

## Scope

In-scope:

- Parsing-related memory or CPU denial-of-service (e.g., pathological
  input that causes exponential backtracking).
- Incorrect parsing that leads to downstream security issues in known
  consumers.
- Dependency vulnerabilities that affect this library (the core jar
  has zero runtime dependencies, so the surface is small).

Out of scope:

- Issues in applications or libraries that consume edtf-java, unless
  caused by a defect in this library.
- Feature requests, general bugs, or spec-conformance disagreements —
  please file those as normal issues.
