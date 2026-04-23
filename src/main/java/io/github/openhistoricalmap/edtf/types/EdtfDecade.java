package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * An L2 decade value (e.g., {@code 199X}).
 *
 * <p><strong>Stub.</strong> Real state (decade value plus
 * uncertain / approximate flags) lands in Phase 6.
 */
public final class EdtfDecade implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfDecade is a Phase 2 stub; full behavior lands in Phase 6";

    @Override
    public EdtfType type() {
        return EdtfType.DECADE;
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
