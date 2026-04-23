package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;
import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.types.EdtfYear;
import org.junit.jupiter.api.Test;

class L2YearTest {
    @Test
    void exponentialBasic() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y1E5");
        assertThat(y.year().longValueExact()).isEqualTo(100000L);
        assertThat(y.level()).isEqualTo(EdtfLevel.L2);
        assertThat(y.toEdtfString()).isEqualTo("Y1E5");
    }

    @Test
    void exponentialNegative() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y-5E6");
        assertThat(y.year().longValueExact()).isEqualTo(-5_000_000L);
    }

    @Test
    void significantDigitsOnLargeYear() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y12345S3");
        assertThat(y.year().intValueExact()).isEqualTo(12345);
        assertThat(y.significantDigits()).isEqualTo(3);
        assertThat(y.level()).isEqualTo(EdtfLevel.L2);
        assertThat(y.toEdtfString()).isEqualTo("Y12345S3");
    }

    @Test
    void exponentialWithSignificant() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y1E5S2");
        assertThat(y.year().longValueExact()).isEqualTo(100000L);
        assertThat(y.significantDigits()).isEqualTo(2);
        assertThat(y.toEdtfString()).isEqualTo("Y1E5S2");
    }
}
