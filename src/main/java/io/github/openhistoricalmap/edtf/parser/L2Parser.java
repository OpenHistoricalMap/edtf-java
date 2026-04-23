package io.github.openhistoricalmap.edtf.parser;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.internal.Bitmask;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfList;
import io.github.openhistoricalmap.edtf.types.EdtfSet;
import io.github.openhistoricalmap.edtf.types.ListMember;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written parser for the L2 subset covered in v0.2:
 *
 * <ul>
 *   <li>Non-progressive X-mask patterns in any YYYY / MM / DD
 *       position, e.g. {@code 2X1X}, {@code 2020-0X}, {@code 2020-X5},
 *       {@code 2020-05-X5}.</li>
 *   <li>Sets delimited by {@code []} and lists delimited by
 *       {@code {}} with single-value members. {@code earlier}
 *       ({@code ..,}) and {@code later} ({@code ,..}) markers are
 *       supported inside the delimiters.</li>
 * </ul>
 *
 * <p>Deferred to a subsequent release: consecutive members
 * ({@code start..end} inside a list), decades ({@code EdtfDecade}),
 * L2Y significant-digits / exponential year notation, L2S extended
 * season codes (25-41), and positional UA markers around individual
 * date components.
 */
public final class L2Parser {

    private L2Parser() {}

    public static EdtfTemporal parse(String input) {
        if (input.isEmpty()) {
            throw new EdtfParseException("empty input", input, 0);
        }
        char first = input.charAt(0);
        if (first == '[') return parseSet(input);
        if (first == '{') return parseList(input);
        return parseMaskedDate(input);
    }

    /**
     * Parse an arbitrary-position X-mask date. Tolerant of any
     * {@code X} placement in YYYY, MM, or DD. The resulting
     * {@link EdtfDate}'s {@code level()} will return L2 when the mask
     * shape is non-canonical-L1.
     */
    static EdtfDate parseMaskedDate(String input) {
        int length = input.length();
        String y, m = null, d = null;

        if (length == 4) {
            y = input;
        } else if (length == 7 && input.charAt(4) == '-') {
            y = input.substring(0, 4);
            m = input.substring(5, 7);
        } else if (length == 10 && input.charAt(4) == '-' && input.charAt(7) == '-') {
            y = input.substring(0, 4);
            m = input.substring(5, 7);
            d = input.substring(8, 10);
        } else {
            throw new EdtfParseException(
                "L2 mask must be 4, 7, or 10 characters", input, 0);
        }

        validateDigitOrX(input, y, 0);
        if (m != null) validateDigitOrX(input, m, 5);
        if (d != null) validateDigitOrX(input, d, 8);

        // Reject patterns with no X at all (those are plain dates, not masks)
        // and fall through to L0 / L1.
        if (!containsX(input)) {
            throw new EdtfParseException("not a masked date (no X)", input, 0);
        }

        int yr = digitsOrZero(y);
        Bitmask mask = new Bitmask(Bitmask.compute(buildLowercasePattern(y, m, d)));

        if (d != null) {
            int mo = monthFromPattern(m);
            int dy = dayFromPattern(d);
            return EdtfDate.ofYearMonthDay(yr, mo, dy)
                .withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
        }
        if (m != null) {
            int mo = monthFromPattern(m);
            return EdtfDate.ofYearMonth(yr, mo)
                .withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
        }
        return EdtfDate.ofYear(yr)
            .withQualifiers(Bitmask.EMPTY, Bitmask.EMPTY, mask);
    }

    private static int monthFromPattern(String m) {
        // If every digit position is X, choose 1 (January) as the base
        // value; Bitmask.min / max will ignore it anyway.
        int v = digitsOrZero(m);
        return v < 1 || v > 12 ? 1 : v;
    }

    private static int dayFromPattern(String d) {
        int v = digitsOrZero(d);
        return v < 1 || v > 31 ? 1 : v;
    }

    private static String buildLowercasePattern(String y, String m, String d) {
        StringBuilder sb = new StringBuilder(y);
        sb.append(m == null ? "mm" : m);
        sb.append(d == null ? "dd" : d);
        return sb.toString().toLowerCase();
    }

    private static void validateDigitOrX(String input, String s, int offset) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!Character.isDigit(ch) && ch != 'X' && ch != 'x') {
                throw new EdtfParseException(
                    "unexpected char '" + ch + "' in mask", input, offset + i);
            }
        }
    }

    private static boolean containsX(String s) {
        return s.indexOf('X') >= 0 || s.indexOf('x') >= 0;
    }

    private static int digitsOrZero(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            sb.append((ch == 'X' || ch == 'x') ? '0' : ch);
        }
        return Integer.parseInt(sb.toString());
    }

    // ----- set / list -----

    private static EdtfSet parseSet(String input) {
        if (!input.endsWith("]")) {
            throw new EdtfParseException("set must end with ']'", input,
                input.length());
        }
        String body = input.substring(1, input.length() - 1);
        ParsedList p = parseMembers(input, body);
        try {
            return EdtfSet.of(p.members, p.earlier, p.later);
        } catch (IllegalArgumentException e) {
            throw new EdtfParseException(e.getMessage(), input, 0);
        }
    }

    private static EdtfList parseList(String input) {
        if (!input.endsWith("}")) {
            throw new EdtfParseException("list must end with '}'", input,
                input.length());
        }
        String body = input.substring(1, input.length() - 1);
        ParsedList p = parseMembers(input, body);
        try {
            return EdtfList.of(p.members, p.earlier, p.later);
        } catch (IllegalArgumentException e) {
            throw new EdtfParseException(e.getMessage(), input, 0);
        }
    }

    private record ParsedList(List<ListMember> members, boolean earlier, boolean later) {}

    /**
     * Parse the body of a set / list. The {@code ..} earlier and later
     * markers attach directly to the adjacent member (no separator
     * comma), matching edtf.js. That is:
     * <ul>
     *   <li>{@code [..2020]} &mdash; earlier only</li>
     *   <li>{@code [..2020, 2021]} &mdash; earlier + two members</li>
     *   <li>{@code [2020..]} &mdash; later only</li>
     *   <li>{@code [2020, 2021..]} &mdash; two members + later</li>
     * </ul>
     */
    private static ParsedList parseMembers(String input, String body) {
        body = body.strip();
        boolean earlier = false;
        boolean later = false;
        if (body.startsWith("..")) {
            earlier = true;
            body = body.substring(2).stripLeading();
        }
        if (body.endsWith("..")) {
            later = true;
            body = body.substring(0, body.length() - 2).stripTrailing();
        }

        List<ListMember> members = new ArrayList<>();
        if (!body.isEmpty()) {
            for (String part : body.split(",")) {
                String trimmed = part.strip();
                if (trimmed.isEmpty()) {
                    throw new EdtfParseException(
                        "empty list member", input, 0);
                }
                if (trimmed.contains("..")) {
                    throw new EdtfParseException(
                        "consecutive (start..end) members are not yet supported",
                        input, 0);
                }
                EdtfTemporal value = Edtf.parse(trimmed);
                members.add(new ListMember.Single(value));
            }
        }
        return new ParsedList(members, earlier, later);
    }
}
