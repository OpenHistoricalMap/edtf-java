package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * An L2 set of alternate dates, written {@code [2020, 2021]}.
 *
 * <p><strong>Stub.</strong> Real state lands in Phase 6.
 */
public final class EdtfSet implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfSet is a Phase 2 stub; full behavior lands in Phase 6";

    @Override
    public EdtfType type() {
        return EdtfType.SET;
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
