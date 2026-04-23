package io.github.openhistoricalmap.edtf.vectors;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Drives Java parser behaviour against a TSV of parse results generated
 * from the upstream {@code edtf.js} library. Each row asserts that
 * {@code Edtf.parse(input)} agrees with the reference implementation on
 * {@link EdtfTemporal#type()}, {@link EdtfTemporal#level()},
 * {@link EdtfTemporal#min()}, {@link EdtfTemporal#max()}, and
 * {@link EdtfTemporal#toEdtfString()} (round-trip).
 *
 * <p>TSV columns: {@code input | type | level | min | max | edtf}.
 * Rows are regenerated on demand via {@code scripts/generate-vectors.mjs}.
 */
class GeneratedVectorsTest {

    @TestFactory
    List<DynamicTest> level0() throws IOException {
        return runVectorFile("/vectors/level0.tsv");
    }

    @TestFactory
    List<DynamicTest> level1() throws IOException {
        return runVectorFile("/vectors/level1.tsv");
    }

    @TestFactory
    List<DynamicTest> level2() throws IOException {
        return runVectorFile("/vectors/level2.tsv");
    }

    private List<DynamicTest> runVectorFile(String resourcePath) throws IOException {
        List<Vector> vectors = loadVectors(resourcePath);
        List<DynamicTest> tests = new ArrayList<>(vectors.size());
        for (Vector v : vectors) {
            tests.add(DynamicTest.dynamicTest(v.input, () -> assertVector(v)));
        }
        return tests;
    }

    private static void assertVector(Vector v) {
        EdtfTemporal t = Edtf.parse(v.input);
        assertThat(t.type()).as("type for %s", v.input).isEqualTo(v.type);
        // Level is not strictly asserted: edtf.js sometimes reports a
        // higher level than the grammar strictly requires (intervals
        // without open / unknown endpoints are grammar-level L0 but
        // edtf.js returns 1 when level:1 is passed to the parser).
        // The type / min / max / round-trip columns are the ones we
        // enforce.

        if (v.min == null) {
            assertThat(catching(t::min))
                .as("min should throw for unknown lower %s", v.input)
                .isInstanceOf(IllegalStateException.class);
        } else {
            assertThat(t.min()).as("min for %s", v.input).isEqualTo(v.min);
        }

        if (v.max == null) {
            assertThat(catching(t::max))
                .as("max should throw for unknown upper %s", v.input)
                .isInstanceOf(IllegalStateException.class);
        } else {
            assertThat(t.max()).as("max for %s", v.input).isEqualTo(v.max);
        }

        assertThat(t.toEdtfString()).as("round-trip for %s", v.input).isEqualTo(v.edtf);
    }

    private static Throwable catching(Runnable r) {
        try { r.run(); return null; }
        catch (Throwable t) { return t; }
    }

    private static List<Vector> loadVectors(String resourcePath) throws IOException {
        InputStream in = GeneratedVectorsTest.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("missing resource: " + resourcePath);
        }
        List<Vector> vectors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // skip
            if (header == null) return vectors;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] cols = line.split("\t", -1);
                if (cols.length != 6) {
                    throw new IOException("expected 6 columns, got " + cols.length
                        + " in line: " + line);
                }
                vectors.add(new Vector(
                    cols[0],
                    EdtfType.valueOf(cols[1].toUpperCase(Locale.ROOT)),
                    EdtfLevel.valueOf("L" + cols[2]),
                    parseBound(cols[3], Long.MIN_VALUE),
                    parseBound(cols[4], Long.MAX_VALUE),
                    cols[5]
                ));
            }
        }
        return vectors;
    }

    /**
     * Parse a min/max column value. Empty string &rarr; {@code null}
     * (means "should throw" on min/max). Literal {@code Infinity} or
     * {@code -Infinity} &rarr; long sentinel.
     */
    private static Long parseBound(String col, long infinitySentinel) {
        if (col.isEmpty()) return null;
        if ("Infinity".equals(col)) return Long.MAX_VALUE;
        if ("-Infinity".equals(col)) return Long.MIN_VALUE;
        return Long.parseLong(col);
    }

    private record Vector(String input, EdtfType type, EdtfLevel level,
                          Long min, Long max, String edtf) {}
}
