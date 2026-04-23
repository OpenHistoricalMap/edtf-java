package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * An L2 decade value, written as a three-digit number such as
 * {@code 199} (meaning the 1990s, i.e. the span 1990-1999 inclusive),
 * or negative forms like {@code -199} (the 1990s BCE).
 *
 * <p>Valid range is -999 to 999 inclusive. Decade {@code N} represents
 * years {@code [N*10, N*10+9]}.
 *
 * <p>This form is distinct from the L1 {@code 199X} masked-year form
 * which renders as an {@link EdtfDate} with an unspecified bitmask.
 * The two notations carry the same semantic meaning but different
 * grammatical roles in EDTF.
 */
public final class EdtfDecade implements EdtfTemporal {

    private final int decade;
    private final boolean uncertain;
    private final boolean approximate;

    private EdtfDecade(int decade, boolean uncertain, boolean approximate) {
        if (decade <= -1000 || decade >= 1000) {
            throw new IllegalArgumentException("decade out of range: " + decade);
        }
        this.decade = decade;
        this.uncertain = uncertain;
        this.approximate = approximate;
    }

    /** Plain L2 decade (no qualifiers). */
    public static EdtfDecade of(int decade) {
        return new EdtfDecade(decade, false, false);
    }

    /** L2 decade with optional uncertain / approximate flags. */
    public static EdtfDecade of(int decade, boolean uncertain, boolean approximate) {
        return new EdtfDecade(decade, uncertain, approximate);
    }

    public int decade() { return decade; }
    public boolean uncertain() { return uncertain; }
    public boolean approximate() { return approximate; }

    /** First year in the span: {@code decade * 10}. */
    public int firstYear() { return decade * 10; }

    @Override public EdtfType type() { return EdtfType.DECADE; }

    @Override public EdtfLevel level() { return EdtfLevel.L2; }

    @Override public long min() {
        return LocalDateTime.of(firstYear(), 1, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override public long max() {
        return LocalDateTime.of(firstYear() + 10, 1, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    }

    @Override public String toEdtfString() {
        int k = Math.abs(decade);
        String body;
        if (k < 10) body = "00" + k;
        else if (k < 100) body = "0" + k;
        else body = String.valueOf(k);
        String s = decade < 0 ? "-" + body : body;
        if (uncertain && approximate) return s + "%";
        if (uncertain) return s + "?";
        if (approximate) return s + "~";
        return s;
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfDecade d
            && d.decade == decade
            && d.uncertain == uncertain
            && d.approximate == approximate;
    }

    @Override public int hashCode() {
        return Objects.hash(decade, uncertain, approximate);
    }
}
