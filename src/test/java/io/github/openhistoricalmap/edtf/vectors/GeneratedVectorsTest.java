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
        assertThat(t.level()).as("level for %s", v.input).isEqualTo(v.level);
        assertThat(t.min()).as("min for %s", v.input).isEqualTo(v.min);
        assertThat(t.max()).as("max for %s", v.input).isEqualTo(v.max);
        assertThat(t.toEdtfString()).as("round-trip for %s", v.input).isEqualTo(v.edtf);
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
                    Long.parseLong(cols[3]),
                    Long.parseLong(cols[4]),
                    cols[5]
                ));
            }
        }
        return vectors;
    }

    private record Vector(String input, EdtfType type, EdtfLevel level,
                          long min, long max, String edtf) {}
}
