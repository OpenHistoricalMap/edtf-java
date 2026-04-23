import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.format.EdtfFormatter;
import java.util.Locale;

/**
 * Smoke test asserting that the published edtf jar is usable from
 * a vanilla Ant + Ivy classpath. Prints two lines, both of which are
 * checked by build.xml's `test` target:
 *   1. The canonical round-tripped EDTF string.
 *   2. The English-localised formatter output.
 */
public class Smoke {
    public static void main(String[] args) {
        String input = "2020-05-15";
        EdtfTemporal t = Edtf.parse(input);

        System.out.println("canonical: " + t.toEdtfString());
        System.out.println("localised: " + EdtfFormatter.forLocale(Locale.US).format(t));
        System.out.println("min: " + t.min());
        System.out.println("max: " + t.max());
        System.out.println("type: " + t.type());

        // A second exercise: an open-ended interval with the formatter.
        EdtfTemporal i = Edtf.parse("2020/..");
        System.out.println("interval canonical: " + i.toEdtfString());
        System.out.println("interval localised: "
            + EdtfFormatter.forLocale(Locale.US).format(i));
    }
}
