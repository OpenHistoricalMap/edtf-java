#!/usr/bin/env node
// Generate TSV test vectors for edtf-java from the upstream edtf.js
// library. Emits rows of the form:
//
//   input \t type \t level \t min \t max \t toEDTF
//
// Only Level 0 single-value cases are emitted here; intervals land with
// Phase 4 (level1.tsv). Run from the repo root:
//
//   node scripts/generate-vectors.mjs > src/test/resources/vectors/level0.tsv
//
// The edtf.js checkout must be present at ./edtf.js (see
// scripts/fetch-reference.sh or the top-level .gitignore which lets you
// clone it alongside).
import edtf from '../edtf.js/index.js';

const LEVEL0 = [
  // Year
  '2020', '2019', '1999', '0000', '0001', '-0001', '-0044', '-9999', '9999',
  // Year-month
  '2020-01', '2020-12', '2020-02', '1999-11', '-0500-08',
  // Year-month-day
  '2020-01-01', '2020-12-31', '2020-02-29', '2019-02-28',
  '2020-04-30', '2020-07-15', '0001-01-01', '-0044-03-15',
  // Datetime — minutes
  '2020-05-01T10:30Z',
  '2020-05-01T00:00Z',
  '2020-05-01T23:59Z',
  // Datetime — seconds
  '2020-05-01T10:30:45Z',
  '2020-05-01T10:30:45-05:00',
  '2020-05-01T10:30:45+14:00',
  // Datetime — milliseconds
  '2020-05-01T10:30:45.123Z',
  '2020-05-01T10:30:45.500-05:00',
  // 24:00
  '2020-05-01T24:00:00Z',
  // Century
  '20', '00', '19', '-01', '-05', '-99',
];

const LEVEL2 = [
  // Non-progressive partial masks
  '2X1X', '20X0',
  // Partial month / day
  '2020-0X', '2020-05-X5', '2020-05-1X',
  // Sets (square brackets) with edtf.js-compatible .. placement
  '[2020, 2021]', '[2020, 2021, 2023]', '[..2020]', '[2020..]',
  // Lists (curly braces)
  '{2020, 2021}', '{2020}',
  // Extended seasons (L2S codes 25-41)
  '2020-25', '2020-30', '2020-37', '2020-40', '2020-41',
  // Decades (standalone 3-digit)
  '199', '202', '-005', '199?', '199~', '199%',
];

const LEVEL1 = [
  // Qualified dates (trailing UA)
  '2020?', '2020~', '2020%',
  '2020-05?', '2020-05~', '2020-05%',
  '2020-05-15?', '2020-05-15~', '2020-05-15%',
  // Unspecified digits: year-only (progressive)
  'XXXX', '201X', '20XX',
  // year-month
  '2020-XX', 'XXXX-XX',
  // year-month-day
  '2020-05-XX', '2020-XX-XX', 'XXXX-XX-XX',
  // Y-notation
  'Y10000', 'Y-10000', 'Y99999',
  // L1 seasons
  '2020-21', '2020-22', '2020-23', '2020-24', '1999-24',
  // Intervals
  '2020/2021', '1950/1999', '2020-05/2020-06',
  '2020/..', '../2020', '../..', '/2020', '2020/',
  '2020?/2021', '2020/2021?',
];

function emit(input, level) {
  try {
    const r = edtf(input, { level });
    const row = [
      input,
      r.type,
      // r.level may be undefined; coerce sensibly
      (r.level !== undefined ? r.level : level),
      r.min,
      r.max,
      r.toEDTF(),
    ];
    console.log(row.join('\t'));
  } catch (e) {
    console.error(`SKIP\t${input}\t${e.message}`);
  }
}

const [,, which = 'all'] = process.argv;

console.log(['input', 'type', 'level', 'min', 'max', 'edtf'].join('\t'));
if (which === 'all' || which === 'level0') {
  for (const input of LEVEL0) emit(input, 0);
}
if (which === 'all' || which === 'level1') {
  for (const input of LEVEL1) emit(input, 1);
}
if (which === 'all' || which === 'level2') {
  for (const input of LEVEL2) emit(input, 2);
}

// Library of Congress EDTF specification examples, drawn from the
// public spec page (https://www.loc.gov/standards/datetime/) and the
// LoC NetDev mirror (lcnetdev.github.io/standards/datetime/edtf.html).
// Same TSV shape as the level files so the same harness handles them.
const LOC_SPEC = [
  // Level 0 — Date
  '1985-04-12', '1985-04', '1985',
  // Level 0 — Date and Time
  // Note: the no-TZ form `1985-04-12T23:20:30` is excluded because
  // edtf.js interprets it as machine-local time (so the generated
  // bounds depend on the runner's timezone) whereas this library
  // treats it as UTC. Both are defensible; only the explicit-TZ
  // forms are deterministic across platforms.
  '1985-04-12T23:20:30Z',
  '1985-04-12T23:20:30-04:00',
  '1985-04-12T23:20:30+04:30',
  // Level 0 — Time interval
  '1964/2008',
  '2004-06/2006-08',
  '2004-02-01/2005-02-08',
  '2004-02-01/2005-02',
  '2004-02-01/2005',
  '2005/2006-02',
  // Level 1 — Letter-prefixed calendar year
  // (Y170000002 / Y-170000002 from the LoC examples are excluded
  // because edtf.js can't represent them via JS Date and emits
  // NaN bounds, whereas the BigInteger-backed Java implementation
  // succeeds; the value is identical, the parity assertion is
  // not.)
  'Y10000', 'Y-10000',
  // Level 1 — Seasons
  '2001-21',  // Spring, 2001
  // Level 1 — Qualification of a date (entire)
  '1984?', '2004-06~', '2004-06-11%',
  // Level 1 — Unspecified digit(s)
  '201X', '20XX', '2004-XX',
  '1985-04-XX', '1985-XX-XX',
  // Level 1 — Extended interval
  '1985-04-12/..',
  '1985-04/..',
  '1985/..',
  '../1985-04-12',
  '/1985-04-12',
  '1985-04-12/',
  // Level 2 — Set representations
  // Sets with consecutive (start..end) members are excluded because
  // edtf.js's list-max bound returns start.max while this library
  // returns end.max (a documented v0.2 divergence — semantically
  // correct here, parity-incompatible).
  '[..1760-12-03]',
  '[1760-12..]',
  '[1667,1760-12]',
  '{1960,1961-12}',
];

if (which === 'all' || which === 'loc-spec') {
  for (const input of LOC_SPEC) emit(input, 2);
}

// ISO 8601-2:2019 example expressions, restricted to those that fall
// within the EDTF / LoC profile this library implements (i.e., the
// implicit forms — explicit `Y`, `M`, `D`, `J`, `C` suffix forms from
// ISO are out of scope). Each input is taken from a quoted example
// in the standard.
const ISO_8601_2 = [
  // 4.4.1.2 — calendar year (negative / zero)
  '0000', '-0001',
  // 4.x — basic dates
  '1985', '1985-04', '1985-04-12',
  // 5.x — qualifications
  '1985-04-12?', '1985-04?',
  '1984~',
  // 5.x — unspecified digits
  '1985-04-XX', '1985-XX-XX',
  // 6.x — intervals (open-ended)
  '1985/..', '1985-04/..', '1985-04-12/..',
  '../1985-04-12', '../1985-04', '../1985',
  '1985-04-12/', '/1985-04-12',
  '1964/2008',
  // 6.x — qualified intervals
  '1984~/2004-06',
  '1984/2004-06~',
];

if (which === 'all' || which === 'iso-8601-2') {
  for (const input of ISO_8601_2) emit(input, 2);
}
