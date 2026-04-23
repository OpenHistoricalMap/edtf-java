package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.util.Objects;

/**
 * An interval with two {@link Endpoint}s. Each endpoint is independently
 * {@link Endpoint.Bounded}, {@link Endpoint.Open}, or
 * {@link Endpoint.Unknown}.
 *
 * <p>Bounds semantics: when an endpoint is open the corresponding
 * {@link #min()} / {@link #max()} returns {@link Long#MIN_VALUE} /
 * {@link Long#MAX_VALUE}. When an endpoint is unknown the corresponding
 * method throws {@link IllegalStateException} &mdash; callers should
 * inspect {@link #lower()} / {@link #upper()} with the
 * {@code isBounded} / {@code isOpen} / {@code isUnknown} convenience
 * methods on {@link Endpoint}.
 *
 * <p>The level of the interval is the maximum level of its endpoints.
 */
public final class EdtfInterval implements EdtfTemporal {

    private final Endpoint lower;
    private final Endpoint upper;

    private EdtfInterval(Endpoint lower, Endpoint upper) {
        this.lower = Objects.requireNonNull(lower, "lower");
        this.upper = Objects.requireNonNull(upper, "upper");
    }

    public static EdtfInterval of(Endpoint lower, Endpoint upper) {
        return new EdtfInterval(lower, upper);
    }

    public Endpoint lower() { return lower; }

    public Endpoint upper() { return upper; }

    @Override public EdtfType type() { return EdtfType.INTERVAL; }

    @Override public EdtfLevel level() {
        EdtfLevel lo = levelOf(lower);
        EdtfLevel hi = levelOf(upper);
        return lo.ordinal() >= hi.ordinal() ? lo : hi;
    }

    private static EdtfLevel levelOf(Endpoint e) {
        if (e instanceof Endpoint.Bounded b) {
            return b.value().level();
        }
        // Open / Unknown endpoints are themselves L1 features.
        return EdtfLevel.L1;
    }

    @Override public long min() {
        if (lower instanceof Endpoint.Bounded b) return b.value().min();
        if (lower instanceof Endpoint.Open) return Long.MIN_VALUE;
        throw new IllegalStateException("interval lower endpoint is unknown");
    }

    @Override public long max() {
        if (upper instanceof Endpoint.Bounded b) return b.value().max();
        if (upper instanceof Endpoint.Open) return Long.MAX_VALUE;
        throw new IllegalStateException("interval upper endpoint is unknown");
    }

    @Override public String toEdtfString() {
        return lower.toEdtfFragment() + "/" + upper.toEdtfFragment();
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfInterval i
            && Objects.equals(i.lower, lower)
            && Objects.equals(i.upper, upper);
    }

    @Override public int hashCode() { return Objects.hash(lower, upper); }
}
