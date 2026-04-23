package io.github.openhistoricalmap.edtf;

import io.github.openhistoricalmap.edtf.types.EdtfCentury;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfDecade;
import io.github.openhistoricalmap.edtf.types.EdtfInterval;
import io.github.openhistoricalmap.edtf.types.EdtfList;
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import io.github.openhistoricalmap.edtf.types.EdtfSet;
import io.github.openhistoricalmap.edtf.types.EdtfYear;

/**
 * A parsed EDTF value. Every concrete subtype is one of the eight
 * permitted kinds listed in {@link EdtfType}.
 *
 * <p>This interface is sealed so consumers can use exhaustive
 * {@code switch} expressions to handle every possible outcome of
 * {@link Edtf#parse(String)} without a default branch:
 *
 * <pre>{@code
 * EdtfTemporal t = Edtf.parse(input);
 * String label = switch (t) {
 *     case EdtfDate d -> "date";
 *     case EdtfYear y -> "year";
 *     case EdtfDecade d -> "decade";
 *     case EdtfCentury c -> "century";
 *     case EdtfSeason s -> "season";
 *     case EdtfInterval i -> "interval";
 *     case EdtfList l -> "list";
 *     case EdtfSet s -> "set";
 * };
 * }</pre>
 *
 * <p>All implementations are immutable and must be thread-safe.
 *
 * <p>Range note: {@link #min()} and {@link #max()} return {@code long}
 * milliseconds since the Unix epoch, matching upstream edtf.js
 * semantics. For EDTF values whose bounds fall outside {@code long}
 * range (reachable only via L2Y exponential notation), implementations
 * throw {@link ArithmeticException} from {@code min()} / {@code max()}.
 * The value itself still parses and renders correctly via
 * {@link #toEdtfString()}.
 */
public sealed interface EdtfTemporal extends Comparable<EdtfTemporal>
    permits EdtfDate, EdtfYear, EdtfDecade, EdtfCentury, EdtfSeason,
            EdtfInterval, EdtfList, EdtfSet {

    /** Which of the eight EDTF kinds this value represents. */
    EdtfType type();

    /** The lowest EDTF conformance level at which this value is legal. */
    EdtfLevel level();

    /**
     * Lower bound of this value, as milliseconds since the Unix epoch
     * (UTC). Inclusive.
     *
     * @throws ArithmeticException if the lower bound cannot be
     *                             represented as a {@code long}
     */
    long min();

    /**
     * Upper bound of this value, as milliseconds since the Unix epoch
     * (UTC). Inclusive.
     *
     * @throws ArithmeticException if the upper bound cannot be
     *                             represented as a {@code long}
     */
    long max();

    /** Canonical EDTF string representation of this value. */
    String toEdtfString();

    /**
     * Default ordering by {@link #min()} first, then {@link #max()}.
     * Concrete subtypes may override for more efficient or
     * level-specific comparisons.
     */
    @Override
    default int compareTo(EdtfTemporal other) {
        int c = Long.compare(this.min(), other.min());
        if (c != 0) {
            return c;
        }
        return Long.compare(this.max(), other.max());
    }

    /** True when this value's range fully contains {@code other}'s range. */
    default boolean covers(EdtfTemporal other) {
        return this.min() <= other.min() && this.max() >= other.max();
    }
}
