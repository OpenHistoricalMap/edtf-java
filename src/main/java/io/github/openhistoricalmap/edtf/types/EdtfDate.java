package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import io.github.openhistoricalmap.edtf.internal.Bitmask;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * A calendar date, optionally with time and timezone, optionally with
 * L1 / L2 uncertainty, approximation, and unspecified-digit masking.
 * Immutable.
 *
 * <p>Months are 1-indexed (1 = January). Year 0 represents 1 BCE
 * (proleptic ISO, matching EDTF semantics). The time zone is nullable;
 * when absent, {@link #min()} and {@link #max()} treat the instant as
 * UTC.
 *
 * <p>Datetime precisions (minute, second, millisecond) are atomic
 * (min == max) and render as UTC-normalised full ISO strings, matching
 * edtf.js behaviour.
 *
 * <p>The three qualification {@link Bitmask}s &mdash; {@code uncertain},
 * {@code approximate}, {@code unspecified} &mdash; default to empty for
 * L0 values. At L1 they are always {@link Bitmask#YMD} (whole-date
 * qualifier) or all-zero. L2 allows partial masks.
 */
public final class EdtfDate implements EdtfTemporal {

    public enum Precision {
        YEAR, MONTH, DAY, MINUTE, SECOND, MILLISECOND;
        public boolean isAtomic() {
            return this == MINUTE || this == SECOND || this == MILLISECOND;
        }
    }

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;
    private final int millisecond;
    private final Precision precision;
    private final ZoneOffset timeZone;
    private final Bitmask uncertain;
    private final Bitmask approximate;
    private final Bitmask unspecified;

    private EdtfDate(int year, int month, int day,
                     int hour, int minute, int second, int millisecond,
                     Precision precision, ZoneOffset timeZone,
                     Bitmask uncertain, Bitmask approximate, Bitmask unspecified) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
        this.precision = precision;
        this.timeZone = timeZone;
        this.uncertain = uncertain != null ? uncertain : Bitmask.EMPTY;
        this.approximate = approximate != null ? approximate : Bitmask.EMPTY;
        this.unspecified = unspecified != null ? unspecified : Bitmask.EMPTY;
    }

    public static EdtfDate ofYear(int year) {
        return new EdtfDate(year, 0, 0, 0, 0, 0, 0,
            Precision.YEAR, null, null, null, null);
    }

    public static EdtfDate ofYearMonth(int year, int month) {
        checkMonth(month);
        return new EdtfDate(year, month, 0, 0, 0, 0, 0,
            Precision.MONTH, null, null, null, null);
    }

    public static EdtfDate ofYearMonthDay(int year, int month, int day) {
        checkMonth(month);
        checkDay(day);
        return new EdtfDate(year, month, day, 0, 0, 0, 0,
            Precision.DAY, null, null, null, null);
    }

    public static EdtfDate ofMinute(int year, int month, int day,
                                    int hour, int minute, ZoneOffset tz) {
        checkMonth(month); checkDay(day); checkTime(hour, minute, 0, 0);
        return new EdtfDate(year, month, day, hour, minute, 0, 0,
            Precision.MINUTE, tz, null, null, null);
    }

    public static EdtfDate ofSecond(int year, int month, int day,
                                    int hour, int minute, int second, ZoneOffset tz) {
        checkMonth(month); checkDay(day); checkTime(hour, minute, second, 0);
        return new EdtfDate(year, month, day, hour, minute, second, 0,
            Precision.SECOND, tz, null, null, null);
    }

    public static EdtfDate ofMillisecond(int year, int month, int day,
                                         int hour, int minute, int second, int millisecond,
                                         ZoneOffset tz) {
        checkMonth(month); checkDay(day); checkTime(hour, minute, second, millisecond);
        return new EdtfDate(year, month, day, hour, minute, second, millisecond,
            Precision.MILLISECOND, tz, null, null, null);
    }

    /** Returns a copy with the given qualification bitmasks applied. */
    public EdtfDate withQualifiers(Bitmask uncertain, Bitmask approximate, Bitmask unspecified) {
        return new EdtfDate(year, month, day, hour, minute, second, millisecond,
            precision, timeZone, uncertain, approximate, unspecified);
    }

    public int year() { return year; }
    public int month() { return month; }
    public int day() { return day; }
    public int hour() { return hour; }
    public int minute() { return minute; }
    public int second() { return second; }
    public int millisecond() { return millisecond; }
    public Precision precision() { return precision; }
    public ZoneOffset timeZone() { return timeZone; }
    public Bitmask uncertain() { return uncertain; }
    public Bitmask approximate() { return approximate; }
    public Bitmask unspecified() { return unspecified; }

    @Override public EdtfType type() { return EdtfType.DATE; }

    @Override public EdtfLevel level() {
        boolean hasFlags =
            uncertain.value() != 0 || approximate.value() != 0 || unspecified.value() != 0;
        if (!hasFlags) return EdtfLevel.L0;

        // L1 masks are the "whole-field" values only: YEAR, MONTH, DAY,
        // YM, MD, YMD, YYXX, YYYX, XXXX (= YEAR), plus 0. Any other
        // value is an L2 partial mask.
        if (!isL1Mask(unspecified.value())) return EdtfLevel.L2;

        // L1 qualifiers are whole-date only: uncertain / approximate
        // either 0 or YMD.
        int u = uncertain.value();
        int a = approximate.value();
        if (u != 0 && u != Bitmask.YMD) return EdtfLevel.L2;
        if (a != 0 && a != Bitmask.YMD) return EdtfLevel.L2;

        return EdtfLevel.L1;
    }

    private static boolean isL1Mask(int mask) {
        return mask == 0
            || mask == Bitmask.YEAR
            || mask == Bitmask.MONTH
            || mask == Bitmask.DAY
            || mask == Bitmask.YM
            || mask == Bitmask.MD
            || mask == Bitmask.YMD
            || mask == Bitmask.YYXX
            || mask == Bitmask.YYYX;
    }

    @Override public long min() {
        if (unspecified.value() != 0) {
            return computeMaskedMin();
        }
        return toInstantMillis(false);
    }

    @Override public long max() {
        if (unspecified.value() != 0) {
            return computeMaskedMax();
        }
        if (precision.isAtomic()) {
            return min();
        }
        return toInstantMillis(true) - 1;
    }

    public boolean isAtomic() {
        return precision.isAtomic() && unspecified.value() == 0;
    }

    /**
     * Use Bitmask.min() to compute the lower bound for a partially
     * unspecified date. Reconstructs the YYYY[MM[DD]] pattern with X's
     * at masked positions.
     */
    private long computeMaskedMin() {
        String[] pattern = reconstructPattern();
        int[] vals = unspecified.min(pattern);
        int y = vals[0];
        int mo = vals.length > 1 ? vals[1] + 1 : 1; // Bitmask.min returns 0-indexed month
        int d = vals.length > 2 ? vals[2] : 1;
        return LocalDateTime.of(Math.abs(y) * (y < 0 || year < 0 ? -1 : 1), mo, d, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private long computeMaskedMax() {
        String[] pattern = reconstructPattern();
        int[] vals = unspecified.max(pattern);
        if (vals.length == 0) {
            return toInstantMillis(true) - 1;
        }
        int y = vals[0];
        int signedY = (year < 0) ? -y : y;
        int mo = vals.length > 1 ? vals[1] + 1 : 12;
        int d = vals.length > 2 ? vals[2] : lastDayOfMonth(signedY, mo);
        return LocalDateTime.of(signedY, mo, d, 23, 59, 59, 999_000_000)
            .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * Rebuild the masked-date pattern (e.g., {@code "201X"},
     * {@code "2020"}, {@code "XX"}, {@code "05"}) from the stored base
     * values and the unspecified bitmask. Feeds Bitmask.min/max.
     */
    private String[] reconstructPattern() {
        int absYear = Math.abs(year);
        char[] y = new char[]{'0','0','0','0'};
        int v = absYear;
        for (int i = 3; i >= 0; i--) { y[i] = (char)('0' + v % 10); v /= 10; }
        for (int i = 0; i < 4; i++) {
            if (unspecified.bit(i) != 0) y[i] = 'X';
        }
        String yStr = new String(y);
        if (precision == Precision.YEAR) return new String[]{yStr};

        char[] m = {(char)('0' + month / 10), (char)('0' + month % 10)};
        for (int i = 0; i < 2; i++) {
            if (unspecified.bit(4 + i) != 0) m[i] = 'X';
        }
        String mStr = new String(m);
        if (precision == Precision.MONTH) return new String[]{yStr, mStr};

        char[] d = {(char)('0' + day / 10), (char)('0' + day % 10)};
        for (int i = 0; i < 2; i++) {
            if (unspecified.bit(6 + i) != 0) d[i] = 'X';
        }
        String dStr = new String(d);
        return new String[]{yStr, mStr, dStr};
    }

    private static int lastDayOfMonth(int year, int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> isLeap(year) ? 29 : 28;
            default -> 31;
        };
    }

    private static boolean isLeap(int y) {
        return (y % 4 == 0 && y % 100 != 0) || y % 400 == 0;
    }

    private long toInstantMillis(boolean exclusiveEnd) {
        int y = year;
        int mo = month == 0 ? 1 : month;
        int d = day == 0 ? 1 : day;
        int h = hour;
        int mi = minute, s = second, ms = millisecond;
        if (h == 24) h = 0;

        LocalDateTime base;
        switch (precision) {
            case YEAR -> base = LocalDateTime.of(y, 1, 1, 0, 0);
            case MONTH -> base = LocalDateTime.of(y, mo, 1, 0, 0);
            case DAY -> base = LocalDateTime.of(y, mo, d, 0, 0);
            case MINUTE -> base = LocalDateTime.of(y, mo, d, h, mi);
            case SECOND -> base = LocalDateTime.of(y, mo, d, h, mi, s);
            case MILLISECOND -> base = LocalDateTime.of(y, mo, d, h, mi, s, ms * 1_000_000);
            default -> throw new IllegalStateException();
        }
        if (hour == 24) base = base.plusDays(1);

        if (exclusiveEnd) {
            base = switch (precision) {
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
        // Masked dates: emit pattern with X's.
        if (unspecified.value() != 0) {
            return renderMasked();
        }
        // Qualified dates (L1 whole-date UA): emit with trailing marker.
        String body = renderPlainDate();
        String marker = resolveUaMarker();
        return body + marker;
    }

    private String renderMasked() {
        String[] pattern = reconstructPattern();
        String sign = year < 0 ? "-" : "";
        StringBuilder sb = new StringBuilder(sign).append(pattern[0]);
        for (int i = 1; i < pattern.length; i++) {
            sb.append('-').append(pattern[i]);
        }
        return sb.toString();
    }

    private String resolveUaMarker() {
        int u = uncertain.value();
        int a = approximate.value();
        if (u != 0 && a != 0) return "%";
        if (u != 0) return "?";
        if (a != 0) return "~";
        return "";
    }

    private String renderPlainDate() {
        StringBuilder sb = new StringBuilder();
        sb.append(padYear(year));
        if (precision == Precision.YEAR) return sb.toString();
        sb.append('-').append(pad2(month));
        if (precision == Precision.MONTH) return sb.toString();
        sb.append('-').append(pad2(day));
        if (precision == Precision.DAY) return sb.toString();
        return formatUtcIso(min());
    }

    private static String formatUtcIso(long epochMillis) {
        java.time.Instant inst = java.time.Instant.ofEpochMilli(epochMillis);
        LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneOffset.UTC);
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
            && Objects.equals(timeZone, d.timeZone)
            && Objects.equals(uncertain, d.uncertain)
            && Objects.equals(approximate, d.approximate)
            && Objects.equals(unspecified, d.unspecified);
    }

    @Override public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second, millisecond,
            precision, timeZone, uncertain, approximate, unspecified);
    }

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

    static String pad2(int n) { return (n < 10) ? "0" + n : String.valueOf(n); }

    static String pad3(int n) {
        if (n < 10) return "00" + n;
        if (n < 100) return "0" + n;
        return String.valueOf(n);
    }

    private static void checkMonth(int m) {
        if (m < 1 || m > 12) throw new IllegalArgumentException("month out of range: " + m);
    }
    private static void checkDay(int d) {
        if (d < 1 || d > 31) throw new IllegalArgumentException("day out of range: " + d);
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
