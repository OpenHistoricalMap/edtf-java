package io.github.openhistoricalmap.edtf;

/**
 * EDTF conformance level per ISO 8601-2:2019 (and its 2025 amendment)
 * and the Library of Congress EDTF specification.
 *
 * <ul>
 *   <li>{@link #L0} &mdash; ISO 8601-1 compatible dates, times, and intervals.</li>
 *   <li>{@link #L1} &mdash; Uncertain (?), approximate (~), and mixed (%) markers;
 *       unspecified-digit masking (X); open / unknown interval endpoints;
 *       Y-notation for years with more than four digits.</li>
 *   <li>{@link #L2} &mdash; Sets, lists, partial unspecified masks, decades,
 *       centuries with qualifiers, significant-digits and exponential year
 *       notation.</li>
 *   <li>{@link #L3} &mdash; Experimental: season qualifiers and season-on-both-sides
 *       intervals beyond what Level 1 and Level 2 provide.</li>
 * </ul>
 */
public enum EdtfLevel {
    L0,
    L1,
    L2,
    L3;

    /** Returns true when this level is at least as permissive as {@code other}. */
    public boolean includes(EdtfLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
