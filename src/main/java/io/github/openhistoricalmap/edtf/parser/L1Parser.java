package io.github.openhistoricalmap.edtf.parser;

import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.internal.Bitmask;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfInterval;
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import io.github.openhistoricalmap.edtf.types.EdtfYear;
import io.github.openhistoricalmap.edtf.types.Endpoint;
import java.math.BigInteger;

/**
 * Hand-written recursive-descent parser for EDTF Level 1.
 *
 * <p>Covers:
 * <ul>
 *   <li>Qualified dates with trailing {@code ?}, {@code ~}, or
 *       {@code %} markers (whole-date uncertain / approximate / both).</li>
 *   <li>Unspecified-digit masks: {@code XXXX}, {@code 201X}, {@code 20XX},
 *       {@code YYYY-XX}, {@code YYYY-MM-XX}, {@code YYYY-XX-XX},
 *       {@code XXXX-XX}, {@code XXXX-XX-XX}.</li>
 *   <li>Y-notation for five-or-more-digit years: {@code Y10000},
 *       {@code Y-10000}.</li>
 *   <li>Season codes 21-24 (spring / summer / autumn / winter) as
 *       {@code YYYY-SS}.</li>
 *   <li>Intervals whose endpoints may be concrete dates, the open
 *       {@code ..} marker, or empty (unknown).</li>
 * </ul>
 *
 * <p>Both-sided UA markers (e.g., {@code 2020?-05~-15}) are L2 and
 * deferred to that phase.
 */
public final class L1Parser {

    private L1Parser() {}

    public static EdtfTemporal parse(String input) {
        // Intervals own the whole input; the '/' is the dispatching token.
        if (input.indexOf('/') >= 0) {
            return parseInterval(input);
        }
        Cursor c = new Cursor(input);
        EdtfTemporal result = parseNonInterval(c);
        if (!c.atEnd()) {
            throw c.farthestError();
        }
        return result;
    }

    private static EdtfTemporal parseNonInterval(Cursor c) {
        int start = c.mark();

        if (c.peek() == 'Y') {
            try { return parseYNotation(c); }
            catch (EdtfParseException ignored) { c.reset(start); }
        }

        try {
            EdtfTemporal x = parseXMasked(c);
            if (c.atEnd()) return x;
            c.reset(start);
        } catch (EdtfParseException ignored) { c.reset(start); }

        try {
            EdtfSeason s = parseSeason(c);
            if (c.atEnd()) return s;
            c.reset(start);
        } catch (EdtfParseException ignored) { c.reset(start); }

        try {
            EdtfDate d = parseUaDate(c);
            if (c.atEnd()) return d;
            c.reset(start);
        } catch (EdtfParseException ignored) { c.reset(start); }

        throw c.farthestError();
    }

    private static EdtfYear parseYNotation(Cursor c) {
        if (!c.accept('Y')) throw c.error("'Y'");
        int sign = 1;
        if (c.peek() == '-') { c.accept('-'); sign = -1; }
        String digits = c.digitsBetween(5, 50);
        if (digits == null) throw c.error("5+ digits after 'Y'");
        BigInteger v = new BigInteger(digits);
        if (sign < 0) v = v.negate();
        return EdtfYear.ofY(v);
    }

    private static EdtfSeason parseSeason(Cursor c) {
        int sign = 1;
        if (c.peek() == '-') { c.accept('-'); sign = -1; }
        String y = c.digits(4);
        if (y == null) throw c.error("4-digit year");
        int year = Integer.parseInt(y) * sign;
        if (!c.accept('-')) throw c.error("'-'");
        String s = c.digits(2);
        if (s == null) throw c.error("2-digit season code");
        int code = Integer.parseInt(s);
        if (code < 21 || code > 24) {
            throw c.error("L1 season code (21-24)");
        }
        return EdtfSeason.of(year, code);
    }

    /**
     * Parse a date followed by an optional UA marker ({@code ?}, {@code ~},
     * or {@code %}). The marker applies to the whole date.
     */
    private static EdtfDate parseUaDate(Cursor c) {
        EdtfDate date = L0Parser.parseDate(c);
        if (c.atEnd()) {
            // Plain L0 date; not an L1 qualified form.
            throw c.error("UA marker (?, ~, or %)");
        }
        char ch = c.peek();
        if (ch != '?' && ch != '~' && ch != '%') {
            throw c.error("UA marker (?, ~, or %)");
        }
        c.accept(ch);

        Bitmask ymdMask = new Bitmask(Bitmask.YMD);
        Bitmask u = (ch == '?' || ch == '%') ? ymdMask : Bitmask.EMPTY;
        Bitmask a = (ch == '~' || ch == '%') ? ymdMask : Bitmask.EMPTY;
        return date.withQualifiers(u, a, Bitmask.EMPTY);
    }

    /**
     * Parse one of the L1X unspecified-mask forms, matching the grammar
     * strictly:
     * <pre>
     *   year-only:       XXXX, YYYX, YYXX   (progressive from the right)
     *   year-month:      YYYY-XX, XXXX-XX
     *   year-month-day:  YYYY-MM-XX, YYYY-XX-XX, XXXX-XX-XX
     * </pre>
     * Non-progressive masks like {@code YXYX}, or partial-day masks
     * like {@code YYYY-MM-DX}, are L2 and rejected here.
     */
    private static EdtfDate parseXMasked(Cursor c) {
        String input = c.input().substring(c.pos());
        int length = input.length();

        if (length == 4 && isProgressiveMaskedYear(input)) {
            int base = digitsOrZero(input);
            Bitmask mask = new Bitmask(Bitmask.compute(input.toLowerCase()));
            c.reset(c.pos() + 4);
            return EdtfDate.ofYear(base).withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
        }

        if (length == 7 && input.charAt(4) == '-') {
            String y = input.substring(0, 4);
            String m = input.substring(5, 7);
            // Only YYYY-XX (m fully masked) or XXXX-XX (y also fully masked)
            if (m.equalsIgnoreCase("XX") && (isFourDigit(y) || y.equalsIgnoreCase("XXXX"))) {
                int yr = digitsOrZero(y);
                Bitmask mask = new Bitmask(Bitmask.compute((y + m).toLowerCase() + "dd"));
                c.reset(c.pos() + 7);
                return EdtfDate.ofYearMonth(yr, 1)
                    .withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
            }
        }

        if (length == 10 && input.charAt(4) == '-' && input.charAt(7) == '-') {
            String y = input.substring(0, 4);
            String m = input.substring(5, 7);
            String d = input.substring(8, 10);
            // Day always fully masked in L1X y-m-d forms.
            if (d.equalsIgnoreCase("XX")) {
                // YYYY-MM-XX   (y digits, m digits)
                // YYYY-XX-XX   (y digits, m masked)
                // XXXX-XX-XX   (all masked)
                boolean yValid = isFourDigit(y) || y.equalsIgnoreCase("XXXX");
                boolean mValid = isTwoDigitMonth(m) || m.equalsIgnoreCase("XX");
                // XXXX with numeric month is not in grammar; require pairing.
                boolean pairOk = !(y.equalsIgnoreCase("XXXX") && isTwoDigitMonth(m));
                if (yValid && mValid && pairOk) {
                    int yr = digitsOrZero(y);
                    int mo = m.equalsIgnoreCase("XX") ? 1 : Integer.parseInt(m);
                    Bitmask mask = new Bitmask(Bitmask.compute(
                        (y + m + d).toLowerCase()));
                    c.reset(c.pos() + 10);
                    return EdtfDate.ofYearMonthDay(yr, mo, 1)
                        .withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
                }
            }
        }

        throw c.error("L1 mask pattern");
    }

    private static boolean isProgressiveMaskedYear(String y) {
        if (y.length() != 4) return false;
        boolean xStarted = false;
        boolean sawX = false;
        for (char ch : y.toCharArray()) {
            if (ch == 'X' || ch == 'x') {
                xStarted = true;
                sawX = true;
            } else if (Character.isDigit(ch)) {
                if (xStarted) return false; // digit after X == not progressive
            } else {
                return false;
            }
        }
        return sawX;
    }

    private static boolean isFourDigit(String s) {
        if (s.length() != 4) return false;
        for (char ch : s.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        return true;
    }

    private static boolean isTwoDigitMonth(String s) {
        if (s.length() != 2) return false;
        for (char ch : s.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        int v = Integer.parseInt(s);
        return v >= 1 && v <= 12;
    }

    private static int digitsOrZero(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            sb.append((ch == 'X' || ch == 'x') ? '0' : ch);
        }
        return Integer.parseInt(sb.toString());
    }

    // ----- intervals -----

    private static EdtfInterval parseInterval(String input) {
        int slash = input.indexOf('/');
        String left = input.substring(0, slash);
        String right = input.substring(slash + 1);
        if (right.indexOf('/') >= 0) {
            throw new EdtfParseException(
                "interval may have at most one '/' separator", input, slash);
        }
        Endpoint lower = parseEndpoint(left);
        Endpoint upper = parseEndpoint(right);
        return EdtfInterval.of(lower, upper);
    }

    private static Endpoint parseEndpoint(String s) {
        if (s.isEmpty()) return Endpoint.Unknown.INSTANCE;
        if (s.equals("..")) return Endpoint.Open.INSTANCE;
        try {
            return new Endpoint.Bounded(L0Parser.parse(s));
        } catch (EdtfParseException ignored) {}
        try {
            Cursor c = new Cursor(s);
            EdtfTemporal t = parseNonInterval(c);
            if (c.atEnd()) return new Endpoint.Bounded(t);
        } catch (EdtfParseException ignored) {}
        throw new EdtfParseException("invalid interval endpoint: " + s, s);
    }
}
