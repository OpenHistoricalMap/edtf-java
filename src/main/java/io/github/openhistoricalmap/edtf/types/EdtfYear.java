package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * A bare year value. Uses {@link BigInteger} internally so L1 Y-notation
 * (five or more digits, including huge values like {@code Y1000000}) and
 * L2 exponential / significant-digits forms are representable without
 * truncation.
 *
 * <p>For L1 Y-notation the {@link #toEdtfString()} output is prefixed
 * with {@code Y}; L0 four-digit years use the plain
 * {@link EdtfDate#ofYear(int)} factory and live as an
 * {@link EdtfDate} with {@link EdtfDate.Precision#YEAR}.
 *
 * <p>{@link #min()} and {@link #max()} throw
 * {@link ArithmeticException} when the resulting epoch milliseconds
 * would overflow {@code long} &mdash; the value still parses and renders
 * correctly through {@link #toEdtfString()}.
 */
public final class EdtfYear implements EdtfTemporal {

    /** Largest absolute value for a {@code long} epoch-ms derivable year. */
    private static final long EPOCH_MS_YEAR_LIMIT = 290_000_000L;

    private final BigInteger year;
    private final EdtfLevel level;

    private EdtfYear(BigInteger year, EdtfLevel level) {
        this.year = Objects.requireNonNull(year, "year");
        this.level = Objects.requireNonNull(level, "level");
    }

    /** L1 Y-notation year; always at least 5 digits in canonical output. */
    public static EdtfYear ofY(BigInteger year) {
        return new EdtfYear(year, EdtfLevel.L1);
    }

    /** Convenience for {@code long}-sized L1 Y-notation. */
    public static EdtfYear ofY(long year) {
        return new EdtfYear(BigInteger.valueOf(year), EdtfLevel.L1);
    }

    public BigInteger year() { return year; }

    @Override public EdtfType type() { return EdtfType.YEAR; }

    @Override public EdtfLevel level() { return level; }

    @Override public long min() {
        checkInLongRange();
        int y = year.intValueExact();
        return LocalDateTime.of(y, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override public long max() {
        checkInLongRange();
        int y = year.intValueExact();
        return LocalDateTime.of(y + 1, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    }

    private void checkInLongRange() {
        // LocalDateTime supports years up to ~1e9; epoch millis range
        // shrinks that to ~2.9e8. We bail early on anything larger.
        if (year.abs().compareTo(BigInteger.valueOf(EPOCH_MS_YEAR_LIMIT)) > 0) {
            throw new ArithmeticException(
                "year " + year + " exceeds the representable epoch-ms range");
        }
    }

    @Override public String toEdtfString() {
        // L1 Y-notation always carries the 'Y' prefix so the leading-zero
        // ambiguity with a four-digit year is resolved.
        return "Y" + year.toString();
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfYear y && y.year.equals(year) && y.level == level;
    }

    @Override public int hashCode() { return Objects.hash(year, level); }
}
