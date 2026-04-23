package io.github.openhistoricalmap.edtf;

/**
 * The eight top-level EDTF value kinds a parsed string may represent.
 *
 * <p>Every concrete implementation of {@link EdtfTemporal} reports one
 * of these via {@link EdtfTemporal#type()}. The naming mirrors
 * {@code edtf.js}'s {@code type} field for easy cross-referencing.
 */
public enum EdtfType {
    /** A calendar date with year, optional month, optional day, and optional time. */
    DATE,
    /** A bare year, including Y-notation and large / significant-digits years. */
    YEAR,
    /** A decade (e.g., {@code 199X}). */
    DECADE,
    /** A century (e.g., {@code 20}). */
    CENTURY,
    /** A season code (spring, summer, fall, winter, or one of the L2/L3 extended codes). */
    SEASON,
    /** A date / date interval with two endpoints. */
    INTERVAL,
    /** A list of dates (brace-delimited), representing one or more possibilities. */
    LIST,
    /** A set of dates (bracket-delimited), representing alternates. */
    SET
}
