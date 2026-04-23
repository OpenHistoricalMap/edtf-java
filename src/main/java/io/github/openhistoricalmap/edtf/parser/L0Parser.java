package io.github.openhistoricalmap.edtf.parser;

import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.types.EdtfCentury;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import java.time.ZoneOffset;

/**
 * Hand-written recursive-descent parser for EDTF Level 0
 * (ISO 8601-1 compatible dates, times, and centuries).
 *
 * <p>Intervals ({@code date/date}) are deferred to {@code L1Parser}
 * because Phase 4 introduces the open / unknown endpoint sealed type
 * that L0 and L1 intervals share; handling them in one place keeps the
 * design consistent.
 *
 * <p>This class is {@code public} but lives in the
 * {@code io.github.openhistoricalmap.edtf.parser} package which is not
 * exported from the module. End users invoke it via
 * {@link io.github.openhistoricalmap.edtf.Edtf#parse(String)}.
 */
public final class L0Parser {

    private L0Parser() {}

    /** Parse an L0 EDTF string or throw {@link EdtfParseException}. */
    public static EdtfTemporal parse(String input) {
        Cursor c = new Cursor(input);
        EdtfTemporal result = parseTopLevel(c);
        if (!c.atEnd()) {
            throw c.farthestError();
        }
        return result;
    }

    private static EdtfTemporal parseTopLevel(Cursor c) {
        int start = c.mark();

        // Try full date_time first. If the input starts with 1-4 digits
        // or '-<digit>', date_time has the longest reach and will emit
        // a more useful error message if it fails late.
        try {
            return parseDateTime(c);
        } catch (EdtfParseException ignored) {
            c.reset(start);
        }

        try {
            EdtfCentury century = parseCentury(c);
            if (c.atEnd()) {
                return century;
            }
            c.reset(start);
        } catch (EdtfParseException ignored) {
            c.reset(start);
        }

        throw c.farthestError();
    }

    private static EdtfDate parseDateTime(Cursor c) {
        EdtfDate date = parseDate(c);
        if (!c.atEnd() && c.peek() == 'T') {
            c.accept('T');
            return parseTime(c, date);
        }
        return date;
    }

    /**
     * Parse a {@code year}, {@code year-month}, or
     * {@code year-month-day}. Returns an {@link EdtfDate} at the
     * corresponding precision.
     */
    static EdtfDate parseDate(Cursor c) {
        int sign = 1;
        if (c.peek() == '-') {
            c.accept('-');
            sign = -1;
        }

        String y4 = c.digits(4);
        if (y4 == null) {
            throw c.error("4-digit year");
        }
        int year = Integer.parseInt(y4) * sign;

        // The grammar forbids '-0000' (positive_year requires a non-zero digit).
        if (sign < 0 && year == 0) {
            throw c.error("positive year after '-'");
        }

        if (c.atEnd() || c.peek() != '-') {
            return EdtfDate.ofYear(year);
        }

        c.accept('-');
        String m2 = c.digits(2);
        if (m2 == null) {
            throw c.error("2-digit month");
        }
        int month = Integer.parseInt(m2);
        if (month < 1 || month > 12) {
            throw c.error("month 01-12");
        }

        if (c.atEnd() || c.peek() != '-') {
            return EdtfDate.ofYearMonth(year, month);
        }

        c.accept('-');
        String d2 = c.digits(2);
        if (d2 == null) {
            throw c.error("2-digit day");
        }
        int day = Integer.parseInt(d2);
        if (!isDayValid(month, day)) {
            throw c.error("day valid for month " + month);
        }

        return EdtfDate.ofYearMonthDay(year, month, day);
    }

    private static EdtfDate parseTime(Cursor c, EdtfDate date) {
        if (date.precision() != EdtfDate.Precision.DAY) {
            throw c.error("date with day precision before 'T'");
        }

        String h2 = c.digits(2);
        if (h2 == null) {
            throw c.error("2-digit hour");
        }
        int hour = Integer.parseInt(h2);
        if (hour > 24) {
            throw c.error("hour 00-24");
        }

        if (!c.accept(':')) {
            throw c.error("':'");
        }
        String m2 = c.digits(2);
        if (m2 == null) {
            throw c.error("2-digit minute");
        }
        int minute = Integer.parseInt(m2);
        if (minute > 59) {
            throw c.error("minute 00-59");
        }
        if (hour == 24 && minute != 0) {
            throw c.error("24:MM only when MM=00");
        }

        EdtfDate.Precision precision = EdtfDate.Precision.MINUTE;
        int second = 0;
        int millisecond = 0;

        if (!c.atEnd() && c.peek() == ':') {
            c.accept(':');
            String s2 = c.digits(2);
            if (s2 == null) {
                throw c.error("2-digit second");
            }
            second = Integer.parseInt(s2);
            if (second > 59) {
                throw c.error("second 00-59");
            }
            if (hour == 24 && second != 0) {
                throw c.error("24:00:SS only when SS=00");
            }
            precision = EdtfDate.Precision.SECOND;

            if (!c.atEnd() && c.peek() == '.') {
                c.accept('.');
                String frac = c.digitsBetween(1, 9);
                if (frac == null) {
                    throw c.error("fractional-second digits");
                }
                String threeDigits;
                if (frac.length() >= 3) {
                    threeDigits = frac.substring(0, 3);
                } else {
                    threeDigits = frac + "0".repeat(3 - frac.length());
                }
                millisecond = Integer.parseInt(threeDigits);
                precision = EdtfDate.Precision.MILLISECOND;
            }
        }

        ZoneOffset tz = parseTimezone(c);

        int year = date.year();
        int month = date.month();
        int day = date.day();

        return switch (precision) {
            case MINUTE -> EdtfDate.ofMinute(year, month, day, hour, minute, tz);
            case SECOND -> EdtfDate.ofSecond(year, month, day, hour, minute, second, tz);
            case MILLISECOND -> EdtfDate.ofMillisecond(year, month, day,
                hour, minute, second, millisecond, tz);
            default -> throw new IllegalStateException(precision.toString());
        };
    }

    private static ZoneOffset parseTimezone(Cursor c) {
        if (c.atEnd()) {
            return null;
        }
        char ch = c.peek();
        if (ch == 'Z') {
            c.accept('Z');
            return ZoneOffset.UTC;
        }
        if (ch == '+' || ch == '-' || ch == '\u2212') {
            c.accept(ch);
            int sign = (ch == '+') ? 1 : -1;
            String h2 = c.digits(2);
            if (h2 == null) {
                throw c.error("2-digit offset hour");
            }
            int hours = Integer.parseInt(h2);
            int minutes = 0;
            if (!c.atEnd() && c.peek() == ':') {
                c.accept(':');
                String m2 = c.digits(2);
                if (m2 == null) {
                    throw c.error("2-digit offset minute");
                }
                minutes = Integer.parseInt(m2);
            }
            int total = sign * (hours * 3600 + minutes * 60);
            // EDTF constrains offsets to -12:00 .. +14:00.
            if (total > 14 * 3600 || total < -12 * 3600) {
                throw c.error("offset in [-12:00, +14:00]");
            }
            return ZoneOffset.ofTotalSeconds(total);
        }
        return null;
    }

    private static EdtfCentury parseCentury(Cursor c) {
        int sign = 1;
        if (c.peek() == '-') {
            c.accept('-');
            sign = -1;
        }
        String cc = c.digits(2);
        if (cc == null) {
            throw c.error("2-digit century");
        }
        int century = Integer.parseInt(cc);
        if (sign < 0 && century == 0) {
            throw c.error("non-zero century after '-'");
        }
        return EdtfCentury.of(century * sign);
    }

    private static boolean isDayValid(int month, int day) {
        if (day < 1) return false;
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> day <= 31;
            case 4, 6, 9, 11 -> day <= 30;
            case 2 -> day <= 29; // EDTF grammar permits Feb 29 in any year
            default -> false;
        };
    }
}
