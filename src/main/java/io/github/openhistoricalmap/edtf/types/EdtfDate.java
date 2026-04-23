package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * A calendar date, optionally with time and timezone, and optional
 * uncertainty / approximation / unspecified-digit masking per EDTF
 * levels 1 and 2.
 *
 * <p><strong>Stub.</strong> Full state (bitmasks, precision, time
 * zone, and bound calculation) lands in Phase 3 (L0) and Phase 4 (L1).
 */
public final class EdtfDate implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfDate is a Phase 2 stub; full behavior lands in Phase 3+";

    @Override
    public EdtfType type() {
        return EdtfType.DATE;
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
