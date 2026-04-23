package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * A calendar date, optionally with time and timezone. Immutable.
 *
 * <p>Months are 1-indexed (1 = January). Year 0 represents 1 BCE
 * (proleptic ISO, matching EDTF semantics). The time zone is nullable;
 * when absent, {@link #min()} and {@link #max()} treat the instant as
 * UTC.
 *
 * <p>Phase 3 implementation covers L0 (no uncertainty / approximation /
 * unspecified masks). L1 adds those as additional optional state.
 */
public final class EdtfDate implements EdtfTemporal {

    /** Precision of a calendar / clock value. */
    public enum Precision {
        YEAR, MONTH, DAY, MINUTE, SECOND, MILLISECOND;

        /**
         * True when this precision names an exact instant (no range).
         * Datetime precisions are atomic &mdash; matching edtf.js, which
         * treats any value with a {@code T} component as an instant.
         */
        public boolean isAtomic() {
            return this == MINUTE || this == SECOND || this == MILLISECOND;
        }
    }

    private final int year;
    private final int month;        // 1..12, 0 if absent (precision < MONTH)
    private final int day;          // 1..31, 0 if absent (precision < DAY)
    private final int hour;         // 0..24 (24 normalizes to next day 00:00)
    private final int minute;       // 0..59
    private final int second;       // 0..59
    private final int millisecond;  // 0..999
    private final Precision precision;
    private final ZoneOffset timeZone; // nullable

    private EdtfDate(int year, int month, int day,
                     int hour, int minute, int second, int millisecond,
                     Precision precision, ZoneOffset timeZone) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
        this.precision = precision;
        this.timeZone = timeZone;
    }

    /** Year-precision: {@code YYYY}. */
    public static EdtfDate ofYear(int year) {
        return new EdtfDate(year, 0, 0, 0, 0, 0, 0, Precision.YEAR, null);
    }

    /** Month-precision: {@code YYYY-MM}. */
    public static EdtfDate ofYearMonth(int year, int month) {
        checkMonth(month);
        return new EdtfDate(year, month, 0, 0, 0, 0, 0, Precision.MONTH, null);
    }

    /** Day-precision: {@code YYYY-MM-DD}. */
    public static EdtfDate ofYearMonthDay(int year, int month, int day) {
        checkMonth(month);
        checkDay(day);
        return new EdtfDate(year, month, day, 0, 0, 0, 0, Precision.DAY, null);
    }

    /** Minute-precision datetime. */
    public static EdtfDate ofMinute(int year, int month, int day,
                                    int hour, int minute, ZoneOffset tz) {
        checkMonth(month);
        checkDay(day);
        checkTime(hour, minute, 0, 0);
        return new EdtfDate(year, month, day, hour, minute, 0, 0, Precision.MINUTE, tz);
    }

    /** Second-precision datetime. */
    public static EdtfDate ofSecond(int year, int month, int day,
                                    int hour, int minute, int second, ZoneOffset tz) {
        checkMonth(month);
        checkDay(day);
        checkTime(hour, minute, second, 0);
        return new EdtfDate(year, month, day, hour, minute, second, 0, Precision.SECOND, tz);
    }

    /** Millisecond-precision datetime. */
    public static EdtfDate ofMillisecond(int year, int month, int day,
                                         int hour, int minute, int second, int millisecond,
                                         ZoneOffset tz) {
        checkMonth(month);
        checkDay(day);
        checkTime(hour, minute, second, millisecond);
        return new EdtfDate(year, month, day, hour, minute, second, millisecond,
            Precision.MILLISECOND, tz);
    }

    public int year() { return year; }
    public int month() { return month; }
    public int day() { return day; }
    public int hour() { return hour; }
    public int minute() { return minute; }
    public int second() { return second; }
    public int millisecond() { return millisecond; }
    public Precision precision() { return precision; }
    /** Returns the time zone offset, or {@code null} if none was specified. */
    public ZoneOffset timeZone() { return timeZone; }

    @Override public EdtfType type() { return EdtfType.DATE; }

    @Override public EdtfLevel level() { return EdtfLevel.L0; }

    @Override public long min() {
        return toInstantMillis(precision, false);
    }

    @Override public long max() {
        if (precision.isAtomic()) {
            return min();
        }
        return toInstantMillis(precision, true) - 1;
    }

    /** True when this value denotes an exact instant. */
    public boolean isAtomic() {
        return precision.isAtomic();
    }

    /**
     * Compute epoch millis for the start (exclusive=false) or the end
     * (exclusive=true, one tick past the end) of this date's precision
     * window.
     */
    private long toInstantMillis(Precision p, boolean exclusiveEnd) {
        int y = year;
        int mo = month == 0 ? 1 : month;
        int d = day == 0 ? 1 : day;
        int h = hour;
        int mi = minute;
        int s = second;
        int ms = millisecond;

        // Normalize 24:00[:00[.000]] -> next day 00:00:00.000
        if (h == 24) {
            h = 0;
            // LocalDateTime.plusDays below handles the roll-over
        }

        LocalDateTime base;
        switch (p) {
            case YEAR -> base = LocalDateTime.of(y, 1, 1, 0, 0);
            case MONTH -> base = LocalDateTime.of(y, mo, 1, 0, 0);
            case DAY -> base = LocalDateTime.of(y, mo, d, 0, 0);
            case MINUTE -> base = LocalDateTime.of(y, mo, d, h, mi);
            case SECOND -> base = LocalDateTime.of(y, mo, d, h, mi, s);
            case MILLISECOND -> base = LocalDateTime.of(y, mo, d, h, mi, s, ms * 1_000_000);
            default -> throw new IllegalStateException(p.toString());
        }

        if (hour == 24) {
            base = base.plusDays(1);
        }

        if (exclusiveEnd) {
            base = switch (p) {
                case YEAR -> base.plusYears(1);
                case MONTH -> base.plusMonths(1);
                case DAY -> base.plusDays(1);
                case MINUTE -> base.plusMinutes(1);
                case SECOND -> base.plusSeconds(1);
                case MILLISECOND -> base;
            };
        }

        ZoneOffset zo = timeZone != null ? timeZone : ZoneOffset.UTC;
        return base.toInstant(zo).toEpochMilli();
    }

    @Override public String toEdtfString() {
        StringBuilder sb = new StringBuilder();
        sb.append(padYear(year));
        if (precision == Precision.YEAR) return sb.toString();
        sb.append('-').append(pad2(month));
        if (precision == Precision.MONTH) return sb.toString();
        sb.append('-').append(pad2(day));
        if (precision == Precision.DAY) return sb.toString();
        // Datetime: normalize to UTC and emit full precision ISO form,
        // matching edtf.js's toISOString() output. Lossy with respect
        // to the caller's original timezone + precision choice, but
        // matches upstream parity.
        long ms = min();
        return formatUtcIso(ms);
    }

    private static String formatUtcIso(long epochMillis) {
        java.time.Instant inst = java.time.Instant.ofEpochMilli(epochMillis);
        java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(inst,
            java.time.ZoneOffset.UTC);
        return padYear(ldt.getYear())
            + "-" + pad2(ldt.getMonthValue())
            + "-" + pad2(ldt.getDayOfMonth())
            + "T" + pad2(ldt.getHour())
            + ":" + pad2(ldt.getMinute())
            + ":" + pad2(ldt.getSecond())
            + "." + pad3(ldt.getNano() / 1_000_000)
            + "Z";
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof EdtfDate d)) return false;
        return year == d.year && month == d.month && day == d.day
            && hour == d.hour && minute == d.minute && second == d.second
            && millisecond == d.millisecond && precision == d.precision
            && Objects.equals(timeZone, d.timeZone);
    }

    @Override public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second, millisecond,
            precision, timeZone);
    }

    // ----- helpers -----

    static String padYear(int year) {
        boolean negative = year < 0;
        int k = Math.abs(year);
        String body;
        if (k < 10) body = "000" + k;
        else if (k < 100) body = "00" + k;
        else if (k < 1000) body = "0" + k;
        else body = String.valueOf(k);
        return negative ? "-" + body : body;
    }

    static String pad2(int n) {
        return (n < 10) ? "0" + n : String.valueOf(n);
    }

    static String pad3(int n) {
        if (n < 10) return "00" + n;
        if (n < 100) return "0" + n;
        return String.valueOf(n);
    }

    static String formatOffset(ZoneOffset zo) {
        if (zo.getTotalSeconds() == 0) return "Z";
        int total = zo.getTotalSeconds();
        char sign = total < 0 ? '-' : '+';
        int abs = Math.abs(total);
        int h = abs / 3600;
        int m = (abs % 3600) / 60;
        return "" + sign + pad2(h) + ":" + pad2(m);
    }

    private static void checkMonth(int m) {
        if (m < 1 || m > 12) {
            throw new IllegalArgumentException("month out of range: " + m);
        }
    }

    private static void checkDay(int d) {
        if (d < 1 || d > 31) {
            throw new IllegalArgumentException("day out of range: " + d);
        }
    }

    private static void checkTime(int h, int mi, int s, int ms) {
        if (h < 0 || h > 24) throw new IllegalArgumentException("hour out of range: " + h);
        if (mi < 0 || mi > 59) throw new IllegalArgumentException("minute out of range: " + mi);
        if (s < 0 || s > 59) throw new IllegalArgumentException("second out of range: " + s);
        if (ms < 0 || ms > 999) throw new IllegalArgumentException("ms out of range: " + ms);
        if (h == 24 && (mi != 0 || s != 0 || ms != 0)) {
            throw new IllegalArgumentException("24:MM:SS.mmm only allowed as 24:00:00.000");
        }
    }
}
