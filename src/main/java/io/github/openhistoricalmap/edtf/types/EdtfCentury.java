package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * A century value (e.g., {@code 20}), at L0 or L2.
 *
 * <p><strong>Stub.</strong> Real state (century value plus optional
 * uncertain / approximate flags) lands in Phase 3 (L0) and Phase 6 (L2).
 */
public final class EdtfCentury implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfCentury is a Phase 2 stub; full behavior lands in Phase 3+";

    @Override
    public EdtfType type() {
        return EdtfType.CENTURY;
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
