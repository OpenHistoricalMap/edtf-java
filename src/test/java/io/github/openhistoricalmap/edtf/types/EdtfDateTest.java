package io.github.openhistoricalmap.edtf.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class EdtfDateTest {

    @Test
    void yearPrecisionBasic() {
        EdtfDate d = EdtfDate.ofYear(2020);
        assertThat(d.type()).isEqualTo(EdtfType.DATE);
        assertThat(d.level()).isEqualTo(EdtfLevel.L0);
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.YEAR);
        assertThat(d.toEdtfString()).isEqualTo("2020");
        assertThat(d.min()).isEqualTo(utcMillis(2020, 1, 1, 0, 0, 0));
        assertThat(d.max()).isEqualTo(utcMillis(2021, 1, 1, 0, 0, 0) - 1);
    }

    @Test
    void monthPrecisionBasic() {
        EdtfDate d = EdtfDate.ofYearMonth(2020, 5);
        assertThat(d.toEdtfString()).isEqualTo("2020-05");
        assertThat(d.min()).isEqualTo(utcMillis(2020, 5, 1, 0, 0, 0));
        assertThat(d.max()).isEqualTo(utcMillis(2020, 6, 1, 0, 0, 0) - 1);
    }

    @Test
    void dayPrecisionBasic() {
        EdtfDate d = EdtfDate.ofYearMonthDay(2020, 2, 29);
        assertThat(d.toEdtfString()).isEqualTo("2020-02-29");
        assertThat(d.min()).isEqualTo(utcMillis(2020, 2, 29, 0, 0, 0));
        assertThat(d.max()).isEqualTo(utcMillis(2020, 3, 1, 0, 0, 0) - 1);
    }

    @Test
    void minutePrecisionIsAtomicAndEmitsFullIso() {
        EdtfDate d = EdtfDate.ofMinute(2020, 5, 1, 10, 30, ZoneOffset.UTC);
        assertThat(d.toEdtfString()).isEqualTo("2020-05-01T10:30:00.000Z");
        assertThat(d.min()).isEqualTo(utcMillis(2020, 5, 1, 10, 30, 0));
        assertThat(d.min()).isEqualTo(d.max());
    }

    @Test
    void secondPrecisionWithOffsetNormalizesToUtc() {
        EdtfDate d = EdtfDate.ofSecond(2020, 5, 1, 10, 30, 45,
            ZoneOffset.ofHoursMinutes(-5, 0));
        // 10:30:45 in -05:00 == 15:30:45 UTC
        assertThat(d.toEdtfString()).isEqualTo("2020-05-01T15:30:45.000Z");
    }

    @Test
    void millisecondPrecisionAtomic() {
        EdtfDate d = EdtfDate.ofMillisecond(2020, 5, 1, 10, 30, 45, 123, ZoneOffset.UTC);
        assertThat(d.toEdtfString()).isEqualTo("2020-05-01T10:30:45.123Z");
        assertThat(d.min()).isEqualTo(d.max());
    }

    @Test
    void negativeYearPadding() {
        assertThat(EdtfDate.ofYear(-1).toEdtfString()).isEqualTo("-0001");
        assertThat(EdtfDate.ofYear(-50).toEdtfString()).isEqualTo("-0050");
        assertThat(EdtfDate.ofYear(-999).toEdtfString()).isEqualTo("-0999");
        assertThat(EdtfDate.ofYear(-9999).toEdtfString()).isEqualTo("-9999");
    }

    @Test
    void yearZeroIsOneBceProlepticIso() {
        EdtfDate d = EdtfDate.ofYear(0);
        assertThat(d.toEdtfString()).isEqualTo("0000");
        // Year 0 == 1 BCE per EDTF / proleptic ISO.
        assertThat(d.min()).isEqualTo(utcMillis(0, 1, 1, 0, 0, 0));
    }

    @Test
    void rejectsInvalidMonth() {
        assertThatThrownBy(() -> EdtfDate.ofYearMonth(2020, 13))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EdtfDate.ofYearMonth(2020, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidDay() {
        assertThatThrownBy(() -> EdtfDate.ofYearMonthDay(2020, 1, 32))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EdtfDate.ofYearMonthDay(2020, 1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twentyFourOhZeroRollsIntoNextDay() {
        EdtfDate d = EdtfDate.ofMinute(2020, 5, 1, 24, 0, ZoneOffset.UTC);
        // 24:00 on May 1 equals 00:00 on May 2.
        assertThat(d.min()).isEqualTo(utcMillis(2020, 5, 2, 0, 0, 0));
    }

    @Test
    void equalityIncludesTimezone() {
        EdtfDate a = EdtfDate.ofMinute(2020, 5, 1, 10, 30, ZoneOffset.UTC);
        EdtfDate b = EdtfDate.ofMinute(2020, 5, 1, 10, 30, ZoneOffset.ofHours(1));
        assertThat(a).isNotEqualTo(b);
        assertThat(a).hasSameHashCodeAs(EdtfDate.ofMinute(2020, 5, 1, 10, 30, ZoneOffset.UTC));
    }

    private static long utcMillis(int y, int mo, int d, int h, int mi, int s) {
        return LocalDateTime.of(y, mo, d, h, mi, s).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
