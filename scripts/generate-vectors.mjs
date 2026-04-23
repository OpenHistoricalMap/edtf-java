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

const INPUTS = [
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

function emit(input) {
  try {
    const r = edtf(input, { level: 0 });
    const row = [
      input,
      r.type,
      0,                  // all entries here are Level 0
      r.min,
      r.max,
      r.toEDTF(),
    ];
    console.log(row.join('\t'));
  } catch (e) {
    console.error(`SKIP\t${input}\t${e.message}`);
  }
}

console.log(['input', 'type', 'level', 'min', 'max', 'edtf'].join('\t'));
for (const input of INPUTS) emit(input);
