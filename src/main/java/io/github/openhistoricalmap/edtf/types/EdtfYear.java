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
    /**
     * L2 significant-digits count (the {@code S} suffix). Zero means
     * no significant-digits qualifier was present.
     */
    private final int significantDigits;
    /**
     * True when the L2Y canonical form used exponential notation
     * ({@code Y1E5}). Affects {@link #toEdtfString()} only.
     */
    private final boolean exponential;

    private EdtfYear(BigInteger year, EdtfLevel level,
                     int significantDigits, boolean exponential) {
        this.year = Objects.requireNonNull(year, "year");
        this.level = Objects.requireNonNull(level, "level");
        this.significantDigits = significantDigits;
        this.exponential = exponential;
    }

    /** L1 Y-notation year; always at least 5 digits in canonical output. */
    public static EdtfYear ofY(BigInteger year) {
        return new EdtfYear(year, EdtfLevel.L1, 0, false);
    }

    /** Convenience for {@code long}-sized L1 Y-notation. */
    public static EdtfYear ofY(long year) {
        return new EdtfYear(BigInteger.valueOf(year), EdtfLevel.L1, 0, false);
    }

    /**
     * L2 Y-notation with significant-digits qualifier, as in
     * {@code Y1234S3} (meaning 1234 but only the first three digits
     * are significant).
     */
    public static EdtfYear ofSignificant(BigInteger year, int significant) {
        if (significant < 1) {
            throw new IllegalArgumentException(
                "significant-digits count must be at least 1");
        }
        return new EdtfYear(year, EdtfLevel.L2, significant, false);
    }

    /**
     * L2 Y-notation with exponential form, as in {@code Y1E5} (=
     * {@code 100000}). The value is fully expanded; only the canonical
     * string form uses the exponential syntax.
     */
    public static EdtfYear ofExponential(BigInteger year) {
        return new EdtfYear(year, EdtfLevel.L2, 0, true);
    }

    /** L2 Y-notation combining exponential form and significant-digits. */
    public static EdtfYear ofExponentialSignificant(BigInteger year, int significant) {
        if (significant < 1) {
            throw new IllegalArgumentException(
                "significant-digits count must be at least 1");
        }
        return new EdtfYear(year, EdtfLevel.L2, significant, true);
    }

    public BigInteger year() { return year; }

    /** 0 when no {@code S} qualifier; otherwise the significant-digits count. */
    public int significantDigits() { return significantDigits; }

    /** True for L2 Y-notation rendered in exponential form. */
    public boolean exponential() { return exponential; }

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
        StringBuilder sb = new StringBuilder();
        if (exponential) {
            sb.append('Y').append(formatExponential(year));
        } else {
            sb.append('Y').append(year.toString());
        }
        if (significantDigits > 0) {
            sb.append('S').append(significantDigits);
        }
        return sb.toString();
    }

    /**
     * Render the value in the {@code coefficient E exponent} form if
     * lossless, otherwise fall back to plain digits. This matches
     * edtf.js's canonical output for {@code Y…E…} values by stripping
     * trailing zeros from the value into the exponent.
     */
    private static String formatExponential(BigInteger value) {
        BigInteger abs = value.abs();
        if (abs.signum() == 0) return "0E0";
        String digits = abs.toString();
        int trailingZeros = 0;
        while (trailingZeros < digits.length() - 1
            && digits.charAt(digits.length() - 1 - trailingZeros) == '0') {
            trailingZeros++;
        }
        String coefficient = digits.substring(0, digits.length() - trailingZeros);
        String sign = value.signum() < 0 ? "-" : "";
        return sign + coefficient + "E" + trailingZeros;
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfYear y
            && y.year.equals(year)
            && y.level == level
            && y.significantDigits == significantDigits
            && y.exponential == exponential;
    }

    @Override public int hashCode() {
        return Objects.hash(year, level, significantDigits, exponential);
    }
}
