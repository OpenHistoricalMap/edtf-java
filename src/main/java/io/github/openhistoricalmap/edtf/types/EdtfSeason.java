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
        if (season < 21 || season > 41) {
            throw new IllegalArgumentException(
                "season code must be 21-41 (got " + season + ")");
        }
        this.year = year;
        this.season = season;
        this.level = level;
    }

    /**
     * L1 season: codes 21-24 (calendar quarters Q1-Q4). Use
     * {@link #ofExtended(int, int)} for L2 codes 25-41.
     */
    public static EdtfSeason of(int year, int season) {
        if (season < 21 || season > 24) {
            throw new IllegalArgumentException(
                "L1 season code must be 21-24; use ofExtended for 25-41");
        }
        return new EdtfSeason(year, season, EdtfLevel.L1);
    }

    /**
     * L2 extended season: codes 25-41 cover hemispheric variants,
     * quadrimesters, and half-year divisions. Bounds follow the
     * upstream edtf.js implementation; exact semantics (N vs S
     * hemisphere, etc.) are not reflected in the bounds since
     * edtf.js ignores the distinction.
     */
    public static EdtfSeason ofExtended(int year, int season) {
        if (season < 25 || season > 41) {
            throw new IllegalArgumentException(
                "L2 extended season code must be 25-41");
        }
        return new EdtfSeason(year, season, EdtfLevel.L2);
    }

    public int year() { return year; }

    public int season() { return season; }

    @Override public EdtfType type() { return EdtfType.SEASON; }

    @Override public EdtfLevel level() { return level; }

    @Override public long min() {
        // Start month table per edtf.js/src/season.js. Java 1-indexed.
        int startMonth = switch (season) {
            case 21, 25, 32, 33, 37, 40 -> 1;  // January
            case 22, 26, 31, 34 -> 4;          // April
            case 23, 27, 30, 35, 41 -> 7;      // July
            case 24, 28, 29, 36 -> 10;         // October
            case 38 -> 5;                      // May
            case 39 -> 9;                      // September
            default -> throw new IllegalStateException("season " + season);
        };
        return LocalDateTime.of(year, startMonth, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override public long max() {
        int yy = year;
        int endMonth = switch (season) {
            case 21, 25, 32, 33 -> 4;           // April 1 - 1ms
            case 22, 26, 31, 34, 40 -> 7;       // July 1 - 1ms
            case 23, 27, 30, 35 -> 10;          // October 1 - 1ms
            case 24, 28, 29, 36, 39, 41 -> {    // January 1 of next year
                yy = year + 1;
                yield 1;
            }
            case 37 -> 6;                       // June 1 - 1ms
            case 38 -> 10;                      // October 1 - 1ms
            default -> throw new IllegalStateException("season " + season);
        };
        return LocalDateTime.of(yy, endMonth, 1, 0, 0)
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
