package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * A bare year, including Y-notation for years with more than four
 * digits and L2 significant-digits / exponential notation.
 *
 * <p><strong>Stub.</strong> Real state (a {@code BigInteger} value, an
 * optional significant-digits count, and level resolution) lands in
 * Phase 4 (L1 Y-notation) and Phase 6 (L2 significant digits).
 */
public final class EdtfYear implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfYear is a Phase 2 stub; full behavior lands in Phase 4+";

    @Override
    public EdtfType type() {
        return EdtfType.YEAR;
    }

    @Override
    public EdtfLevel level() {
        throw new UnsupportedOperationException(NOT_YET);
    }

    @Override
    public long min() {
        throw new UnsupportedOperationException(NOT_YET);
    }

    @Override
    public long max() {
        throw new UnsupportedOperationException(NOT_YET);
    }

    @Override
    public String toEdtfString() {
        throw new UnsupportedOperationException(NOT_YET);
    }
}
