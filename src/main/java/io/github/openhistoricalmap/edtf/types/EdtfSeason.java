package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * A season code attached to a year, at L1 (codes 21-24) or L2 / L3
 * (extended codes 25-41).
 *
 * <p><strong>Stub.</strong> Real state (year, season code, and
 * qualifier bitmasks) lands in Phase 4 (L1S) and Phase 7 (L2S / L3S).
 */
public final class EdtfSeason implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfSeason is a Phase 2 stub; full behavior lands in Phase 4+";

    @Override
    public EdtfType type() {
        return EdtfType.SEASON;
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
