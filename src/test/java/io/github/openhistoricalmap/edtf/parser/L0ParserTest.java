package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import io.github.openhistoricalmap.edtf.ParseOptions;
import io.github.openhistoricalmap.edtf.types.EdtfCentury;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class L0ParserTest {

    @Test
    void yearOnly() {
        EdtfTemporal t = Edtf.parse("2020");
        assertThat(t).isInstanceOf(EdtfDate.class);
        assertThat(t.type()).isEqualTo(EdtfType.DATE);
        assertThat(t.toEdtfString()).isEqualTo("2020");
        assertThat(((EdtfDate) t).precision()).isEqualTo(EdtfDate.Precision.YEAR);
    }

    @Test
    void yearZero() {
        EdtfTemporal t = Edtf.parse("0000");
        assertThat(t.toEdtfString()).isEqualTo("0000");
        assertThat(((EdtfDate) t).year()).isEqualTo(0);
    }

    @Test
    void negativeYear() {
        EdtfTemporal t = Edtf.parse("-0044");
        assertThat(((EdtfDate) t).year()).isEqualTo(-44);
        assertThat(t.toEdtfString()).isEqualTo("-0044");
    }

    @Test
    void yearMonth() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05");
        assertThat(d.year()).isEqualTo(2020);
        assertThat(d.month()).isEqualTo(5);
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.MONTH);
    }

    @Test
    void yearMonthDay() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-02-29");
        assertThat(d.day()).isEqualTo(29);
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.DAY);
    }

    @Test
    void datetimeMinute() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-01T10:30Z");
        assertThat(d.hour()).isEqualTo(10);
        assertThat(d.minute()).isEqualTo(30);
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.MINUTE);
        assertThat(d.timeZone().getTotalSeconds()).isZero();
    }

    @Test
    void datetimeSecondsWithOffset() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-01T10:30:45-05:00");
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.SECOND);
        assertThat(d.timeZone().getTotalSeconds()).isEqualTo(-5 * 3600);
    }

    @Test
    void datetimeMillisecondsWithOffset() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-01T10:30:45.123+01:00");
        assertThat(d.precision()).isEqualTo(EdtfDate.Precision.MILLISECOND);
        assertThat(d.millisecond()).isEqualTo(123);
        // Datetimes normalize to UTC in the canonical output.
        assertThat(d.toEdtfString()).isEqualTo("2020-05-01T09:30:45.123Z");
    }

    @Test
    void datetimeWithoutTimezone() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-01T10:30:45");
        assertThat(d.timeZone()).isNull();
    }

    @Test
    void twentyFourHundred() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-01T24:00:00Z");
        // 24:00:00 == next day 00:00:00
        assertThat(d.min()).isEqualTo(
            EdtfDate.ofYearMonthDay(2020, 5, 2).min());
    }

    @Test
    void centuryBasic() {
        EdtfTemporal t = Edtf.parse("20");
        assertThat(t).isInstanceOf(EdtfCentury.class);
        assertThat(((EdtfCentury) t).century()).isEqualTo(20);
    }

    @Test
    void centuryZero() {
        assertThat(((EdtfCentury) Edtf.parse("00")).century()).isZero();
    }

    @Test
    void centuryNegative() {
        assertThat(((EdtfCentury) Edtf.parse("-05")).century()).isEqualTo(-5);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",                  // empty
        "202",               // incomplete year
        "2020-",             // trailing dash, no month
        "2020-13",           // month out of range
        "2020-02-30",        // invalid day in Feb
        "2020-04-31",        // invalid day in April
        "2020-00-01",        // zero month
        "2020-01-00",        // zero day
        "-0000",             // grammar forbids -0000
        "2020T10:30",        // datetime requires full date
        "2020-05T10:30",     // datetime requires full date
        "abcd",              // non-digits
        "2020 ",             // trailing whitespace
        " 2020",             // leading whitespace
        "2020-05-01T25:00",  // hour > 24
        "2020-05-01T24:30",  // 24: MM must be 00
        "2020-05-01T10:60",  // minute > 59
        "2020-05-01T10:30+15:00",   // offset out of range
    })
    void rejectsInvalid(String input) {
        assertThatThrownBy(() -> Edtf.parse(input))
            .isInstanceOf(EdtfParseException.class);
    }

    @Test
    void parseOptionsRejectWhenLevelCapExceeded() {
        ParseOptions strict = ParseOptions.DEFAULT.withMaxLevel(EdtfLevel.L0);
        EdtfTemporal t = Edtf.parse("2020", strict);
        assertThat(t.level()).isEqualTo(EdtfLevel.L0);
    }

    @Test
    void parseOptionsRejectWhenTypeForbidden() {
        ParseOptions onlyYears = ParseOptions.DEFAULT
            .withAllowedTypes(java.util.Set.of(EdtfType.YEAR));
        assertThatThrownBy(() -> Edtf.parse("2020", onlyYears))
            .isInstanceOf(EdtfParseException.class);
    }

    @Test
    void nullInputThrowsNpe() {
        assertThatThrownBy(() -> Edtf.parse(null))
            .isInstanceOf(NullPointerException.class);
    }
}
