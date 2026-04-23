package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;

/**
 * An L2 list of dates, written {@code {2020, 2021, 2023..2025}}.
 *
 * <p><strong>Stub.</strong> Real state (a list of {@code ListMember}s
 * plus {@code earlier}/{@code later} flags) lands in Phase 6.
 */
public final class EdtfList implements EdtfTemporal {

    private static final String NOT_YET =
        "EdtfList is a Phase 2 stub; full behavior lands in Phase 6";

    @Override
    public EdtfType type() {
        return EdtfType.LIST;
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
