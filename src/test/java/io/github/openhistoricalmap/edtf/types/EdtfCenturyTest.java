package io.github.openhistoricalmap.edtf.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class EdtfCenturyTest {

    @Test
    void basics() {
        EdtfCentury c = EdtfCentury.of(20);
        assertThat(c.type()).isEqualTo(EdtfType.CENTURY);
        assertThat(c.level()).isEqualTo(EdtfLevel.L0);
        assertThat(c.century()).isEqualTo(20);
        assertThat(c.firstYear()).isEqualTo(2000);
        assertThat(c.toEdtfString()).isEqualTo("20");
    }

    @Test
    void twentiethCenturyBounds() {
        EdtfCentury c = EdtfCentury.of(20);
        assertThat(c.min()).isEqualTo(utcMillis(2000, 1, 1));
        assertThat(c.max()).isEqualTo(utcMillis(2100, 1, 1) - 1);
    }

    @Test
    void centuryZeroPadsTo00() {
        EdtfCentury c = EdtfCentury.of(0);
        assertThat(c.toEdtfString()).isEqualTo("00");
        assertThat(c.min()).isEqualTo(utcMillis(0, 1, 1));
    }

    @Test
    void singleDigitPositivePads() {
        assertThat(EdtfCentury.of(5).toEdtfString()).isEqualTo("05");
    }

    @Test
    void negativeCenturyFormats() {
        assertThat(EdtfCentury.of(-1).toEdtfString()).isEqualTo("-01");
        assertThat(EdtfCentury.of(-15).toEdtfString()).isEqualTo("-15");
    }

    @Test
    void rejectsOutOfRange() {
        assertThatThrownBy(() -> EdtfCentury.of(100))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EdtfCentury.of(-100))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static long utcMillis(int y, int mo, int d) {
        return LocalDateTime.of(y, mo, d, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
