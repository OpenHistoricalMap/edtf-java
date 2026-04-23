package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * A century value (e.g., {@code 20}, representing years 2000 to 2099).
 *
 * <p>Valid range is -99 to 99 inclusive. Negative centuries represent
 * BCE windows; for example century {@code -1} covers years -99 to 0
 * (ISO year 0 = 1 BCE).
 *
 * <p>At L0 the value has no qualifiers. L2 may attach uncertain /
 * approximate flags; those land as a separate factory in Phase 6.
 */
public final class EdtfCentury implements EdtfTemporal {

    private final int century;
    private final EdtfLevel level;

    private EdtfCentury(int century, EdtfLevel level) {
        if (century <= -100 || century >= 100) {
            throw new IllegalArgumentException("century out of range: " + century);
        }
        this.century = century;
        this.level = level;
    }

    /** L0 century value. */
    public static EdtfCentury of(int century) {
        return new EdtfCentury(century, EdtfLevel.L0);
    }

    /** Two-digit century value: -99..99. */
    public int century() { return century; }

    /** First year in the span: {@code century * 100}. */
    public int firstYear() { return century * 100; }

    @Override public EdtfType type() { return EdtfType.CENTURY; }

    @Override public EdtfLevel level() { return level; }

    @Override public long min() {
        return LocalDateTime.of(firstYear(), 1, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override public long max() {
        return LocalDateTime.of(firstYear() + 100, 1, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    }

    @Override public String toEdtfString() {
        int k = Math.abs(century);
        String body = k < 10 ? "0" + k : String.valueOf(k);
        return century < 0 ? "-" + body : body;
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfCentury c && c.century == century && c.level == level;
    }

    @Override public int hashCode() { return Objects.hash(century, level); }
}
