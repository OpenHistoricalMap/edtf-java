package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * A date / date interval with two endpoints, which may be bounded,
 * open ({@code ..}), or unknown.
 *
 * <p><strong>Stub.</strong> The endpoint sealed type and interval
 * semantics land in Phase 4 (L1 intervals) and Phase 6 (L2 intervals).
 */
public final class EdtfInterval implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfInterval is a Phase 2 stub; full behavior lands in Phase 4+";

    @Override
    public EdtfType type() {
        return EdtfType.INTERVAL;
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
