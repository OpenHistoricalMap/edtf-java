package io.github.openhistoricalmap.edtf;

import io.github.openhistoricalmap.edtf.parser.L0Parser;
import io.github.openhistoricalmap.edtf.parser.L1Parser;
import io.github.openhistoricalmap.edtf.parser.L2Parser;
import java.util.Objects;

/**
 * Static façade for parsing and rendering EDTF (Extended Date/Time
 * Format) strings. This is the primary entry point for consumers of
 * the library.
 *
 * <p>At Phase 3 only Level 0 ({@link EdtfLevel#L0}) is supported.
 * Levels 1 through 3 land in subsequent phases.
 *
 * @see <a href="https://www.loc.gov/standards/datetime/">LoC EDTF specification</a>
 * @see <a href="https://www.iso.org/standard/70908.html">ISO 8601-2:2019</a>
 */
public final class Edtf {

    private Edtf() {
        // static façade — no instances
    }

    /**
     * Parse {@code input} as an EDTF value.
     *
     * @throws EdtfParseException if the input is not a valid EDTF string
     *                            at any supported level
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public static EdtfTemporal parse(String input) {
        return parse(input, ParseOptions.DEFAULT);
    }

    /**
     * Parse {@code input} as an EDTF value, constrained by
     * {@code options}.
     *
     * @throws EdtfParseException if the input does not parse, or parses
     *                            only at a level / type the options forbid
     */
    public static EdtfTemporal parse(String input, ParseOptions options) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(options, "options");

        // Try L0, then L1, then L2. Lowest-level legal result wins per
        // edtf.js behaviour. L3 will join this chain in a later phase.
        EdtfTemporal result;
        try {
            result = L0Parser.parse(input);
        } catch (EdtfParseException l0Err) {
            try {
                result = L1Parser.parse(input);
            } catch (EdtfParseException l1Err) {
                try {
                    result = L2Parser.parse(input);
                } catch (EdtfParseException l2Err) {
                    throw l0Err;
                }
            }
        }

        if (!options.permits(result.level())) {
            throw new EdtfParseException(
                "value parses at level " + result.level()
                    + " but options cap at " + options.maxLevel(),
                input
            );
        }
        if (!options.permits(result.type())) {
            throw new EdtfParseException(
                "value is of type " + result.type()
                    + " which is not permitted by options",
                input
            );
        }
        return result;
    }
}
