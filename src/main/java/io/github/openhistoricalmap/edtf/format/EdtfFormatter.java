package io.github.openhistoricalmap.edtf.format;

import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.internal.Bitmask;
import io.github.openhistoricalmap.edtf.types.EdtfCentury;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfDecade;
import io.github.openhistoricalmap.edtf.types.EdtfInterval;
import io.github.openhistoricalmap.edtf.types.EdtfList;
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import io.github.openhistoricalmap.edtf.types.EdtfSet;
import io.github.openhistoricalmap.edtf.types.EdtfYear;
import io.github.openhistoricalmap.edtf.types.Endpoint;
import io.github.openhistoricalmap.edtf.types.ListMember;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Locale-aware human-readable formatter for {@link EdtfTemporal}
 * values.
 *
 * <p>Where {@link EdtfTemporal#toEdtfString()} produces the canonical
 * machine-readable form ({@code 2020-05}, {@code 199?},
 * {@code 2020/2021}), this class produces a readable form suitable for
 * UI display:
 *
 * <pre>{@code
 * EdtfFormatter f = EdtfFormatter.forLocale(Locale.US);
 * f.format(Edtf.parse("2020-05"));        // -> "May 2020"
 * f.format(Edtf.parse("199"));            // -> "the 1990s"
 * f.format(Edtf.parse("2020-21"));        // -> "Q1 2020"
 * f.format(Edtf.parse("2020/2021"));      // -> "2020 to 2021"
 * f.format(Edtf.parse("2020?"));          // -> "2020 (uncertain)"
 * f.format(Edtf.parse("circa-1990"));     // -> "circa 1990"
 * }</pre>
 *
 * <p>Strings are loaded from a {@link ResourceBundle} at
 * {@code io.github.openhistoricalmap.edtf.format.messages}. The default
 * bundle ships English; translations are managed via Transifex.
 *
 * <p>Forms not yet handled by the formatter (datetime precision,
 * partial unspecified masks, complex set/list membership, exponential
 * Y-notation) fall back to the canonical {@link
 * EdtfTemporal#toEdtfString()} output.
 *
 * <p>Instances are immutable and thread-safe.
 */
public final class EdtfFormatter {

    private static final String BUNDLE_NAME =
        "io.github.openhistoricalmap.edtf.format.messages";

    private final Locale locale;
    private final ResourceBundle bundle;

    private EdtfFormatter(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale");
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    /** Formatter for the JVM's default locale. */
    public static EdtfFormatter forDefaultLocale() {
        return new EdtfFormatter(Locale.getDefault());
    }

    /** Formatter for the given locale. */
    public static EdtfFormatter forLocale(Locale locale) {
        return new EdtfFormatter(locale);
    }

    public Locale locale() { return locale; }

    /** Render {@code value} as a human-readable string in this formatter's locale. */
    public String format(EdtfTemporal value) {
        Objects.requireNonNull(value, "value");
        String body = formatCore(value);
        return wrapQualifier(body, value);
    }

    private String formatCore(EdtfTemporal value) {
        if (value instanceof EdtfDate d) return formatDate(d);
        if (value instanceof EdtfYear y) return formatYear(y);
        if (value instanceof EdtfDecade d) return formatDecade(d);
        if (value instanceof EdtfCentury c) return formatCentury(c);
        if (value instanceof EdtfSeason s) return formatSeason(s);
        if (value instanceof EdtfInterval i) return formatInterval(i);
        if (value instanceof EdtfSet s) return formatSet(s);
        if (value instanceof EdtfList l) return formatList(l);
        return value.toEdtfString();
    }

    /** Apply the L1 trailing-qualifier wrap if the value's UA bitmasks call for it. */
    private String wrapQualifier(String body, EdtfTemporal value) {
        if (!(value instanceof EdtfDate d)) {
            return body;
        }
        int u = d.uncertain().value();
        int a = d.approximate().value();
        if (u == 0 && a == 0) return body;
        // Whole-date L1 qualifiers only — partial / positional UA falls
        // through to the canonical form for now.
        if (!isWholeDateMask(u, d) || !isWholeDateMask(a, d)) {
            return body;
        }
        if (u != 0 && a != 0) return msg("qualifier.mixed", body);
        if (u != 0) return msg("qualifier.uncertain", body);
        return msg("qualifier.approximate", body);
    }

    private static boolean isWholeDateMask(int mask, EdtfDate d) {
        if (mask == 0) return true;
        return switch (d.precision()) {
            case YEAR -> mask == Bitmask.YEAR;
            case MONTH -> mask == Bitmask.YM;
            default -> mask == Bitmask.YMD;
        };
    }

    private String formatDate(EdtfDate d) {
        // Masked dates fall back to canonical for now.
        if (d.unspecified().value() != 0) {
            return d.toEdtfString();
        }
        return switch (d.precision()) {
            case YEAR -> formatYearNumber(d.year());
            case MONTH -> {
                LocalDate marker = LocalDate.of(safeYear(d.year()), d.month(), 1);
                yield DateTimeFormatter.ofPattern("MMMM yyyy", locale).format(marker)
                    + suffixForBce(d.year());
            }
            case DAY -> {
                LocalDate ld = LocalDate.of(safeYear(d.year()), d.month(), d.day());
                yield DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    .withLocale(locale)
                    .format(ld) + suffixForBce(d.year());
            }
            default -> d.toEdtfString();
        };
    }

    /**
     * java.time only handles years in 0001-9999 cleanly through the
     * localised pattern. For BCE / year 0 we still try a positive
     * absolute value and append a BCE suffix via {@link #suffixForBce}.
     */
    private static int safeYear(int year) {
        if (year <= 0) return Math.max(1, 1 - year); // year 0 -> 1; -44 -> 45
        return year;
    }

    private String suffixForBce(int year) {
        return year <= 0 ? " BCE" : "";
    }

    private String formatYearNumber(int year) {
        if (year < 0) return msg("year.bce", String.valueOf(-year));
        return msg("year.ce", String.valueOf(year));
    }

    private String formatYear(EdtfYear y) {
        java.math.BigInteger v = y.year();
        if (v.bitLength() < 31) {
            int i = v.intValueExact();
            if (Math.abs(i) < 10000) {
                return formatYearNumber(i);
            }
        }
        return msg("year.large",
            java.text.NumberFormat.getNumberInstance(locale).format(v));
    }

    private String formatDecade(EdtfDecade d) {
        return msg("decade", String.valueOf(d.firstYear()));
    }

    private String formatCentury(EdtfCentury c) {
        return msg("century", String.valueOf(c.firstYear()));
    }

    private String formatSeason(EdtfSeason s) {
        String key = "season." + s.season();
        if (bundle.containsKey(key)) {
            return msg(key, String.valueOf(s.year()));
        }
        return s.toEdtfString();
    }

    private String formatInterval(EdtfInterval i) {
        boolean lowerBounded = i.lower() instanceof Endpoint.Bounded;
        boolean upperBounded = i.upper() instanceof Endpoint.Bounded;
        boolean lowerOpen = i.lower() instanceof Endpoint.Open;
        boolean upperOpen = i.upper() instanceof Endpoint.Open;
        boolean lowerUnknown = i.lower() instanceof Endpoint.Unknown;
        boolean upperUnknown = i.upper() instanceof Endpoint.Unknown;

        if (lowerBounded && upperBounded) {
            return msg("interval.bounded",
                format(((Endpoint.Bounded) i.lower()).value()),
                format(((Endpoint.Bounded) i.upper()).value()));
        }
        if (lowerOpen && upperOpen) {
            return msg("interval.openBoth");
        }
        if (lowerOpen && upperBounded) {
            return msg("interval.openLower",
                format(((Endpoint.Bounded) i.upper()).value()));
        }
        if (lowerBounded && upperOpen) {
            return msg("interval.openUpper",
                format(((Endpoint.Bounded) i.lower()).value()));
        }
        if (lowerUnknown && upperBounded) {
            return msg("interval.unknownLower",
                format(((Endpoint.Bounded) i.upper()).value()));
        }
        if (lowerBounded && upperUnknown) {
            return msg("interval.unknownUpper",
                format(((Endpoint.Bounded) i.lower()).value()));
        }
        return i.toEdtfString();
    }

    private String formatSet(EdtfSet s) {
        return formatGroup(s.members(), "set");
    }

    private String formatList(EdtfList l) {
        return formatGroup(l.members(), "list");
    }

    private String formatGroup(java.util.List<ListMember> members, String prefix) {
        if (members.isEmpty()) return msg(prefix + ".empty");
        if (members.size() == 1) return msg(prefix + ".one", formatMember(members.get(0)));
        if (members.size() == 2) {
            return msg(prefix + ".two",
                formatMember(members.get(0)),
                formatMember(members.get(1)));
        }
        String joined = members.stream()
            .map(this::formatMember)
            .collect(Collectors.joining(", "));
        return msg(prefix + ".many", joined);
    }

    private String formatMember(ListMember m) {
        if (m instanceof ListMember.Single s) return format(s.value());
        if (m instanceof ListMember.Consecutive c) {
            return msg("interval.bounded", format(c.start()), format(c.end()));
        }
        return m.toEdtfFragment();
    }

    private String msg(String key, Object... args) {
        String pattern = bundle.containsKey(key) ? bundle.getString(key) : "{0}";
        if (args.length == 0) return pattern;
        return new MessageFormat(pattern, locale).format(args);
    }
}
