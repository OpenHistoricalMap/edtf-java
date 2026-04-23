package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * A season attached to a year, e.g., {@code 2020-21} (spring 2020).
 *
 * <p>Season codes 21-24 are L1. Bound semantics follow
 * {@code edtf.js}, which treats them as calendar quarters:
 * <ul>
 *   <li>{@code 21} &mdash; Q1 (Jan-Mar)</li>
 *   <li>{@code 22} &mdash; Q2 (Apr-Jun)</li>
 *   <li>{@code 23} &mdash; Q3 (Jul-Sep)</li>
 *   <li>{@code 24} &mdash; Q4 (Oct-Dec)</li>
 * </ul>
 * This diverges from the meteorological-seasons interpretation some
 * EDTF readers expect; the calendar-quarter bounds are the upstream
 * reference implementation's choice and are preserved for parity.
 *
 * <p>Codes 25-41 (extended Northern / Southern / half-year seasons)
 * are L2 / L3 and will be added in a later phase.
 */
public final class EdtfSeason implements EdtfTemporal {

    private final int year;
    private final int season;
    private final EdtfLevel level;

    private EdtfSeason(int year, int season, EdtfLevel level) {
        if (season < 21 || season > 24) {
            throw new IllegalArgumentException(
                "L1 season code must be 21-24 (got " + season + ")");
        }
        this.year = year;
        this.season = season;
        this.level = level;
    }

    /** L1 season: spring (21), summer (22), autumn (23), or winter (24). */
    public static EdtfSeason of(int year, int season) {
        return new EdtfSeason(year, season, EdtfLevel.L1);
    }

    public int year() { return year; }

    public int season() { return season; }

    @Override public EdtfType type() { return EdtfType.SEASON; }

    @Override public EdtfLevel level() { return level; }

    @Override public long min() {
        int startMonth = switch (season) {
            case 21 -> 1;  // Q1 starts Jan 1
            case 22 -> 4;  // Q2 starts Apr 1
            case 23 -> 7;  // Q3 starts Jul 1
            case 24 -> 10; // Q4 starts Oct 1
            default -> throw new IllegalStateException();
        };
        return LocalDateTime.of(year, startMonth, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override public long max() {
        int nextQuarterStart = switch (season) {
            case 21 -> 4;   // end of Q1 = Apr 1 - 1ms
            case 22 -> 7;   // end of Q2 = Jul 1 - 1ms
            case 23 -> 10;  // end of Q3 = Oct 1 - 1ms
            case 24 -> 13;  // end of Q4 = Jan 1 next year - 1ms
            default -> throw new IllegalStateException();
        };
        int yy = year;
        int mo = nextQuarterStart;
        if (mo == 13) { yy = year + 1; mo = 1; }
        return LocalDateTime.of(yy, mo, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    }

    @Override public String toEdtfString() {
        return EdtfDate.padYear(year) + "-" + season;
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfSeason s
            && s.year == year && s.season == season && s.level == level;
    }

    @Override public int hashCode() { return Objects.hash(year, season, level); }
}
