import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfTemporal;

/**
 * Smoke test asserting that the published edtf jar is usable from
 * a vanilla Ant + Ivy classpath. Uses only the API that has been
 * stable since the first published release (0.2.0), so the daily
 * smoke workflow exercises whichever version is on Maven Central
 * without coupling to features that may not yet be released.
 *
 * Prints lines that build.xml's `test` target checks for substrings.
 */
public class Smoke {
    public static void main(String[] args) {
        String input = "2020-05-15";
        EdtfTemporal t = Edtf.parse(input);
        System.out.println("canonical: " + t.toEdtfString());
        System.out.println("type: " + t.type());
        System.out.println("level: " + t.level());
        System.out.println("min: " + t.min());
        System.out.println("max: " + t.max());

        // Cover an interval too.
        EdtfTemporal i = Edtf.parse("2020/..");
        System.out.println("interval canonical: " + i.toEdtfString());
        System.out.println("interval upper-open: " + i.upper().getClass().getSimpleName());
    }
}
