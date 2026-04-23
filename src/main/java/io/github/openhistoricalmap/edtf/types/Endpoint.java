package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfTemporal;
import java.util.Objects;

/**
 * One endpoint of an {@link EdtfInterval}. EDTF allows an endpoint to
 * be a concrete date ({@link Bounded}), the open-ended {@code ..}
 * marker ({@link Open}), or absent entirely ({@link Unknown}),
 * corresponding to intervals like {@code 2020/..}, {@code 2020/}, or
 * {@code /2020}.
 *
 * <p>The sealed hierarchy lets callers switch on endpoint kind
 * exhaustively rather than juggling nullable / sentinel values.
 */
public sealed interface Endpoint permits Endpoint.Bounded, Endpoint.Open, Endpoint.Unknown {

    /** Short "is this Open?" convenience. */
    default boolean isOpen() { return this instanceof Open; }

    /** Short "is this Unknown?" convenience. */
    default boolean isUnknown() { return this instanceof Unknown; }

    /** Short "is this Bounded?" convenience. */
    default boolean isBounded() { return this instanceof Bounded; }

    /** Canonical EDTF fragment rendering for this endpoint. */
    String toEdtfFragment();

    /** An explicit date value endpoint. */
    record Bounded(EdtfTemporal value) implements Endpoint {
        public Bounded {
            Objects.requireNonNull(value, "value");
        }
        @Override public String toEdtfFragment() { return value.toEdtfString(); }
    }

    /**
     * The EDTF {@code ..} marker &mdash; an open-ended endpoint meaning
     * "any time before / after" the opposite endpoint.
     */
    record Open() implements Endpoint {
        public static final Open INSTANCE = new Open();
        @Override public String toEdtfFragment() { return ".."; }
    }

    /**
     * An absent endpoint. Written as an empty string in the EDTF form
     * (e.g., {@code /2020} has an Unknown lower endpoint).
     */
    record Unknown() implements Endpoint {
        public static final Unknown INSTANCE = new Unknown();
        @Override public String toEdtfFragment() { return ""; }
    }
}
