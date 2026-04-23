package io.github.openhistoricalmap.edtf.internal;

import java.util.regex.Pattern;

/**
 * Bitmask records Unspecified, Uncertain, and Approximate flags on a
 * date at single-digit precision across the YYYY-MM-DD pattern.
 *
 * <p>Each of the eight {@code YYYYMMDD} character positions maps to one
 * bit, with the leftmost character mapping to bit 0:
 *
 * <pre>
 *   char position: Y Y Y Y M M D D
 *   bit index:     0 1 2 3 4 5 6 7
 * </pre>
 *
 * <p>Derived masks for whole-field matching:
 *
 * <pre>
 *   DAY   = bits 6,7   = 0xC0 (192)
 *   MONTH = bits 4,5   = 0x30 (48)
 *   YEAR  = bits 0..3  = 0x0F (15)
 * </pre>
 *
 * <p>Instances are immutable; the {@link #add(int)}, {@link #set(int)},
 * and {@link #qualify(int)} methods return new {@code Bitmask} objects
 * rather than mutating in place.
 *
 * <p>This class is a faithful port of {@code src/bitmask.js} from the
 * upstream edtf.js library (v4.11.0) and is validated bit-for-bit by
 * {@code BitmaskTest}.
 */
public final class Bitmask {

    /** Mask for a fully unspecified day (bits 6 and 7). */
    public static final int DAY = compute("yyyymmxx");

    /** Mask for a fully unspecified month (bits 4 and 5). */
    public static final int MONTH = compute("yyyyxxdd");

    /** Mask for a fully unspecified year (bits 0-3). */
    public static final int YEAR = compute("xxxxmmdd");

    /** Short alias for {@link #DAY}. */
    public static final int D = DAY;

    /** Short alias for {@link #MONTH}. */
    public static final int M = MONTH;

    /** Short alias for {@link #YEAR}. */
    public static final int Y = YEAR;

    /** Mask for month + day both unspecified. */
    public static final int MD = MONTH | DAY;

    /** Mask for year + month + day all unspecified. */
    public static final int YMD = YEAR | MONTH | DAY;

    /** Mask for year + month both unspecified. */
    public static final int YM = YEAR | MONTH;

    /** Mask with the last two year digits unspecified (e.g., 19XX). */
    public static final int YYXX = compute("yyxxmmdd");

    /** Mask with the last year digit unspecified (e.g., 201X). */
    public static final int YYYX = compute("yyyxmmdd");

    /** Mask with all four year digits unspecified; numerically equal to {@link #YEAR}. */
    public static final int XXXX = compute("xxxxmmdd");

    /** Mask with the day tens digit known and units digit unspecified (e.g., 3X). */
    public static final int DX = compute("yyyymmdx");

    /** Mask with the day tens digit unspecified and units digit known (e.g., X3). */
    public static final int XD = compute("yyyymmxd");

    /** Mask with the month tens digit known and units digit unspecified (e.g., 0X, 1X). */
    public static final int MX = compute("yyyymxdd");

    /** Mask with the month tens digit unspecified and units digit known (e.g., X1). */
    public static final int XM = compute("yyyyxmdd");

    /**
     * Maps each uncertain/approximate marker position in the
     * {@code ~YYYY~-~MM~-~DD~} template to the mask it qualifies:
     * <pre>
     *   position 0: before year  -> YEAR
     *   position 1: after year   -> YEAR
     *   position 2: before month -> MONTH
     *   position 3: after month  -> YM
     *   position 4: before day   -> DAY
     *   position 5: after day    -> YMD
     * </pre>
     */
    public static final int[] UA = {YEAR, YEAR, MONTH, YM, DAY, YMD};

    /** Convenience zero-valued bitmask. */
    public static final Bitmask EMPTY = new Bitmask(0);

    private static final int[] MAXDAYS = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final char[] YYYYMMDD = {'Y', 'Y', 'Y', 'Y', 'M', 'M', 'D', 'D'};

    private static final Pattern DAY_KEYWORD = Pattern.compile("^days?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MONTH_KEYWORD = Pattern.compile("^months?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEAR_KEYWORD = Pattern.compile("^years?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN = Pattern.compile("^[0-9xXdDmMyY]{8}$");
    private static final Pattern SYMBOLS = Pattern.compile("[xX]");

    private final int value;

    /** Creates an empty bitmask (value 0). */
    public Bitmask() {
        this(0);
    }

    /** Creates a bitmask with the given raw value. */
    public Bitmask(int value) {
        this.value = value;
    }

    /**
     * Creates a bitmask from a keyword ({@code day}, {@code month},
     * {@code year}, case-insensitive, optionally plural) or from an
     * eight-character {@code YYYYMMDD}-style pattern where {@code X}
     * or {@code x} marks unspecified positions.
     *
     * @throws IllegalArgumentException on any other input
     */
    public Bitmask(String value) {
        this.value = convert(value);
    }

    /** Creates a bitmask equal to {@link #YMD} when {@code true}, or 0 when {@code false}. */
    public Bitmask(boolean value) {
        this.value = value ? YMD : 0;
    }

    /** Returns the underlying 8-bit integer value. */
    public int value() {
        return value;
    }

    /**
     * Returns the bitwise AND of this mask's value and {@code other}.
     * Callers typically use {@code != 0} to test presence; the
     * non-boolean return matches {@code edtf.js}'s {@code test()}
     * which returns the intersection rather than a boolean.
     */
    public int test(int other) {
        return value & other;
    }

    /** Like {@link #test(int)} but accepts a keyword or 8-char pattern. */
    public int test(String other) {
        return value & convert(other);
    }

    /** Like {@link #test(int)} but accepts a boolean (true == YMD). */
    public int test(boolean other) {
        return value & convert(other);
    }

    /** Returns the value of the single bit at index {@code k} (masked). */
    public int bit(int k) {
        return value & (1 << k);
    }

    /** True when any of the day bits are set. */
    public boolean hasDay() {
        return test(DAY) != 0;
    }

    /** True when any of the month bits are set. */
    public boolean hasMonth() {
        return test(MONTH) != 0;
    }

    /** True when any of the year bits are set. */
    public boolean hasYear() {
        return test(YEAR) != 0;
    }

    /** Returns a new mask with {@code other} bits OR'd in. */
    public Bitmask add(int other) {
        return new Bitmask(value | other);
    }

    /** Like {@link #add(int)} but accepts a keyword or pattern. */
    public Bitmask add(String other) {
        return new Bitmask(value | convert(other));
    }

    /** Returns a new mask with value replaced by {@code other}. */
    public Bitmask set(int other) {
        return new Bitmask(other);
    }

    /** Like {@link #set(int)} but accepts a keyword or pattern. */
    public Bitmask set(String other) {
        return new Bitmask(convert(other));
    }

    /**
     * Apply this mask to the default {@code YYYYMMDD} template, replacing
     * any bit-set positions with {@code 'X'}. Equivalent to
     * {@code mask(YYYYMMDD, 0, 'X')}.
     */
    public char[] mask() {
        return mask(YYYYMMDD.clone(), 0, 'X');
    }

    /**
     * Apply this mask to {@code input}, replacing any position whose
     * bit (at {@code offset + idx}) is set with {@code symbol}. Returns
     * a new char array.
     */
    public char[] mask(char[] input, int offset, char symbol) {
        char[] out = new char[input.length];
        for (int idx = 0; idx < input.length; idx++) {
            out[idx] = bit(offset + idx) != 0 ? symbol : input[idx];
        }
        return out;
    }

    /**
     * Apply this mask across {@code values} with a running offset, so
     * that {@code masks(["YYYY", "MM", "DD"], 'X')} uses bits 0-3 for
     * the year, 4-5 for the month, and 6-7 for the day.
     */
    public String[] masks(String[] values, char symbol) {
        String[] out = new String[values.length];
        int offset = 0;
        for (int i = 0; i < values.length; i++) {
            char[] arr = mask(values[i].toCharArray(), offset, symbol);
            out[i] = new String(arr);
            offset += arr.length;
        }
        return out;
    }

    /**
     * Compute the maximum date consistent with the partially-unspecified
     * year/month/day strings in {@code ymd}. The returned array has
     * length 0, 1, 2, or 3 depending on how many of the inputs were
     * supplied. Month values are zero-indexed (0 = January).
     */
    public int[] max(String[] ymd) {
        if (ymd.length == 0 || isEmpty(ymd[0])) {
            return new int[0];
        }
        int year = Integer.parseInt(
            hasYear() ? masks(new String[]{ymd[0]}, '9')[0] : ymd[0]);

        if (ymd.length < 2 || isEmpty(ymd[1])) {
            return new int[]{year};
        }
        int month = parseWithXAsZero(ymd[1]) - 1;

        int monthTest = test(MONTH);
        if (monthTest == MONTH) {
            month = 11;
        } else if (monthTest == MX) {
            month = (month < 9) ? 8 : 11;
        } else if (monthTest == XM) {
            month = (month + 1) % 10;
            month = (month < 3) ? month + 9 : month - 1;
        }

        if (ymd.length < 3 || isEmpty(ymd[2])) {
            return new int[]{year, month};
        }
        int day = parseWithXAsZero(ymd[2]);

        int dayTest = test(DAY);
        if (dayTest == DAY) {
            day = MAXDAYS[month];
        } else if (dayTest == DX) {
            day = Math.min(MAXDAYS[month], day + (9 - (day % 10)));
        } else if (dayTest == XD) {
            day = day % 10;
            if (month == 1) {
                day = (day == 9 && !leap(year)) ? day + 10 : day + 20;
            } else {
                day = (day < 2) ? day + 30 : day + 20;
                if (day > MAXDAYS[month]) {
                    day -= 10;
                }
            }
        }

        if (month == 1 && day > 28 && !leap(year)) {
            day = 28;
        }
        return new int[]{year, month, day};
    }

    /**
     * Compute the minimum date consistent with the partially-unspecified
     * year/month/day strings in {@code ymd}. The returned array has
     * length 0, 1, 2, or 3. Month values are zero-indexed.
     */
    public int[] min(String[] ymd) {
        if (ymd.length == 0 || isEmpty(ymd[0])) {
            return new int[0];
        }
        int year = Integer.parseInt(
            hasYear() ? masks(new String[]{ymd[0]}, '0')[0] : ymd[0]);

        if (ymd.length < 2 || ymd[1] == null) {
            return new int[]{year};
        }
        int month = parseWithXAsZero(ymd[1]) - 1;

        int monthTest = test(MONTH);
        if (monthTest == MONTH || monthTest == XM) {
            month = 0;
        } else if (monthTest == MX) {
            month = (month < 9) ? 0 : 9;
        }

        if (ymd.length < 3 || isEmpty(ymd[2])) {
            return new int[]{year, month};
        }
        int day = parseWithXAsZero(ymd[2]);

        int dayTest = test(DAY);
        if (dayTest == DAY) {
            day = 1;
        } else if (dayTest == DX) {
            day = Math.max(1, (day / 10) * 10);
        } else if (dayTest == XD) {
            day = Math.max(1, day % 10);
        }

        return new int[]{year, month, day};
    }

    /**
     * Apply uncertain/approximate symbol markers around each value,
     * consulting {@link #qualified(int)} for positions {@code 2*idx}
     * (before) and {@code 2*idx + 1} (after).
     */
    public String[] marks(String[] values, char symbol) {
        String[] out = new String[values.length];
        for (int idx = 0; idx < values.length; idx++) {
            StringBuilder sb = new StringBuilder();
            if (qualified(idx * 2)) {
                sb.append(symbol);
            }
            sb.append(values[idx]);
            if (qualified(idx * 2 + 1)) {
                sb.append(symbol);
            }
            out[idx] = sb.toString();
        }
        return out;
    }

    /**
     * Whether the qualification-symbol position {@code idx} (in the
     * {@code ~YYYY~-~MM~-~DD~} template) should carry a symbol given
     * this mask's current value.
     */
    public boolean qualified(int idx) {
        return switch (idx) {
            case 1 -> value == YEAR
                || ((value & YEAR) != 0 && (value & MONTH) == 0);
            case 2 -> value == MONTH
                || ((value & MONTH) != 0 && (value & YEAR) == 0);
            case 3 -> value == YM;
            case 4 -> value == DAY
                || ((value & DAY) != 0 && value != YMD);
            case 5 -> value == YMD;
            default -> false;
        };
    }

    /** Returns a new mask with {@link #UA}{@code [idx]} OR'd in. */
    public Bitmask qualify(int idx) {
        return new Bitmask(value | UA[idx]);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Bitmask other && other.value == value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /** Renders the mask as a {@code YYYY-MM-DD} string with {@code 'X'}. */
    @Override
    public String toString() {
        return toString('X');
    }

    /** Renders the mask as a {@code YYYY-MM-DD} string with the given symbol. */
    public String toString(char symbol) {
        String[] parts = masks(new String[]{"YYYY", "MM", "DD"}, symbol);
        return String.join("-", parts);
    }

    // ----- static helpers -----

    /** Convenience equivalent of {@code convert(a) & convert(b)}. */
    public static int test(int a, int b) {
        return a & b;
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(String a, String b) {
        return convert(a) & convert(b);
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(boolean a, String b) {
        return convert(a) & convert(b);
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(String a, int b) {
        return convert(a) & b;
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(boolean a, boolean b) {
        return convert(a) & convert(b);
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(boolean a, int b) {
        return convert(a) & b;
    }

    /** Convenience matching edtf.js's static {@code test(a, b)}. */
    public static int test(String a, boolean b) {
        return convert(a) & convert(b);
    }

    /** Returns the bitmask value for a keyword or 8-char pattern. */
    public static int convert(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        if (DAY_KEYWORD.matcher(value).matches()) {
            return DAY;
        }
        if (MONTH_KEYWORD.matcher(value).matches()) {
            return MONTH;
        }
        if (YEAR_KEYWORD.matcher(value).matches()) {
            return YEAR;
        }
        if (PATTERN.matcher(value).matches()) {
            return compute(value);
        }
        throw new IllegalArgumentException("invalid bitmask value: " + value);
    }

    /** Returns {@link #YMD} for {@code true}, 0 for {@code false}. */
    public static int convert(boolean value) {
        return value ? YMD : 0;
    }

    /** Returns the underlying value of the given bitmask. */
    public static int convert(Bitmask value) {
        return value == null ? 0 : value.value;
    }

    /**
     * Compute a bitmask value by scanning {@code pattern} and setting
     * bit {@code i} for each {@code 'X'} or {@code 'x'} at position
     * {@code i}. Other characters are ignored.
     */
    public static int compute(String pattern) {
        int out = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == 'X' || c == 'x') {
                out |= (1 << i);
            }
        }
        return out;
    }

    /** Replace every {@code X}/{@code x} in {@code mask} with {@code digit}. */
    public static String numbers(String mask, char digit) {
        return SYMBOLS.matcher(mask).replaceAll(String.valueOf(digit));
    }

    /** See {@link #values(String, char, boolean)}. Uses digit {@code '0'} and normalizes. */
    public static int[] values(String mask) {
        return values(mask, '0', true);
    }

    /** See {@link #values(String, char, boolean)}. Normalizes by default. */
    public static int[] values(String mask, char digit) {
        return values(mask, digit, true);
    }

    /**
     * Decompose an 8-char (or 6-char, 4-char) {@code mask} into
     * {@code [year, month?, day?]}, replacing any {@code X}/{@code x}
     * with {@code digit}. When {@code normalize} is true, month and
     * day are clamped to legal ranges (month 0-11, day 1..max-days).
     */
    public static int[] values(String mask, char digit, boolean normalize) {
        String num = numbers(mask, digit);
        int[] result;
        if (num.length() > 6) {
            result = new int[]{
                Integer.parseInt(num.substring(0, 4)),
                Integer.parseInt(num.substring(4, 6)),
                Integer.parseInt(num.substring(6, 8))
            };
        } else if (num.length() > 4) {
            result = new int[]{
                Integer.parseInt(num.substring(0, 4)),
                Integer.parseInt(num.substring(4, 6))
            };
        } else {
            result = new int[]{Integer.parseInt(num.substring(0, 4))};
        }
        return normalize ? normalize(result) : result;
    }

    /** Clamp month to 0-11 and day to the month's legal range. */
    public static int[] normalize(int[] values) {
        if (values.length > 1) {
            values[1] = Math.min(11, Math.max(0, values[1] - 1));
        }
        if (values.length > 2) {
            int maxDay = MAXDAYS[values[1]];
            values[2] = Math.min(maxDay, Math.max(1, values[2]));
        }
        return values;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Parse an integer allowing {@code X}/{@code x} characters that
     * are treated as zero. Matches edtf.js's reliance on JS
     * {@code Number("XX") == NaN} being silently recovered by the
     * bitmask-guided bound logic that runs afterward.
     */
    private static int parseWithXAsZero(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            sb.append((ch == 'X' || ch == 'x') ? '0' : ch);
        }
        return Integer.parseInt(sb.toString());
    }

    private static boolean leap(int year) {
        if (year % 4 != 0) return false;
        if (year % 100 != 0) return true;
        return year % 400 == 0;
    }
}
