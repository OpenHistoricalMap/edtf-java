package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfInterval;
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import io.github.openhistoricalmap.edtf.types.EdtfYear;
import io.github.openhistoricalmap.edtf.types.Endpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class L1ParserTest {

    @Test
    void trailingQuestionMark() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020?");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
        assertThat(d.uncertain().value()).isNotZero();
        assertThat(d.toEdtfString()).isEqualTo("2020?");
    }

    @Test
    void trailingTildeApproximate() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05~");
        assertThat(d.approximate().value()).isNotZero();
        assertThat(d.uncertain().value()).isZero();
        assertThat(d.toEdtfString()).isEqualTo("2020-05~");
    }

    @Test
    void percentMixedQualifier() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-15%");
        assertThat(d.uncertain().value()).isNotZero();
        assertThat(d.approximate().value()).isNotZero();
        assertThat(d.toEdtfString()).isEqualTo("2020-05-15%");
    }

    @Test
    void unspecifiedAllYear() {
        EdtfDate d = (EdtfDate) Edtf.parse("XXXX");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
        assertThat(d.toEdtfString()).isEqualTo("XXXX");
    }

    @Test
    void unspecifiedLastYearDigit() {
        EdtfDate d = (EdtfDate) Edtf.parse("201X");
        assertThat(d.toEdtfString()).isEqualTo("201X");
        // Minimum is 2010-01-01, max is 2019-12-31
        assertThat(d.min()).isLessThan(d.max());
    }

    @Test
    void unspecifiedMonth() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-XX");
        assertThat(d.toEdtfString()).isEqualTo("2020-XX");
    }

    @Test
    void unspecifiedDay() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-XX");
        assertThat(d.toEdtfString()).isEqualTo("2020-05-XX");
    }

    @Test
    void unspecifiedMonthAndDay() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-XX-XX");
        assertThat(d.toEdtfString()).isEqualTo("2020-XX-XX");
    }

    @Test
    void unspecifiedAll() {
        EdtfDate d = (EdtfDate) Edtf.parse("XXXX-XX-XX");
        assertThat(d.toEdtfString()).isEqualTo("XXXX-XX-XX");
    }

    @Test
    void yNotationPositive() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y10000");
        assertThat(y.level()).isEqualTo(EdtfLevel.L1);
        assertThat(y.year().intValueExact()).isEqualTo(10000);
        assertThat(y.toEdtfString()).isEqualTo("Y10000");
    }

    @Test
    void yNotationNegative() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y-20000");
        assertThat(y.year().intValueExact()).isEqualTo(-20000);
    }

    @Test
    void yNotationLargeValue() {
        EdtfYear y = (EdtfYear) Edtf.parse("Y1000000000");
        assertThat(y.year().toString()).isEqualTo("1000000000");
        // 1 billion years > the ~290 million year epoch-ms horizon.
        assertThatThrownBy(y::min).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void seasonSpring() {
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-21");
        assertThat(s.type()).isEqualTo(EdtfType.SEASON);
        assertThat(s.year()).isEqualTo(2020);
        assertThat(s.season()).isEqualTo(21);
        assertThat(s.toEdtfString()).isEqualTo("2020-21");
    }

    @Test
    void seasonQ4IsOctToDec() {
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-24");
        // Matches edtf.js: season 24 is calendar Q4 (Oct-Dec), not
        // Dec-Feb winter.
        assertThat(s.min()).isEqualTo(utcMillis(2020, 10, 1));
        assertThat(s.max()).isEqualTo(utcMillis(2021, 1, 1) - 1);
    }

    @Test
    void intervalBothBounded() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020/2021");
        assertThat(i.type()).isEqualTo(EdtfType.INTERVAL);
        assertThat(i.lower().isBounded()).isTrue();
        assertThat(i.upper().isBounded()).isTrue();
        assertThat(i.toEdtfString()).isEqualTo("2020/2021");
    }

    @Test
    void intervalOpenUpper() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020/..");
        assertThat(i.upper().isOpen()).isTrue();
        assertThat(i.max()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void intervalOpenLower() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("../2020");
        assertThat(i.lower().isOpen()).isTrue();
        assertThat(i.min()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void intervalBothOpen() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("../..");
        assertThat(i.lower().isOpen()).isTrue();
        assertThat(i.upper().isOpen()).isTrue();
    }

    @Test
    void intervalUnknownLower() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("/2020");
        assertThat(i.lower().isUnknown()).isTrue();
        assertThatThrownBy(i::min).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void intervalUnknownUpper() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020/");
        assertThat(i.upper().isUnknown()).isTrue();
    }

    @Test
    void intervalWithQualifiedEndpoint() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020?/2021");
        assertThat(i.lower()).isInstanceOf(Endpoint.Bounded.class);
        EdtfTemporal lo = ((Endpoint.Bounded) i.lower()).value();
        assertThat(lo.level()).isEqualTo(EdtfLevel.L1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Y1234",              // Y with 4 digits - L0 range, invalid
        "2020-25",            // season code out of L1 range (25 is L2/L3)
        "2020?~",             // mixing UA markers outside of %
        "YYYY",               // Y_YY is year mask, not Y-notation
        "2020//2021",         // double slash
        // Note: 2020-XX-01 used to be rejected at L1, but L2 now
        // accepts it as a partial mask (non-progressive). That case
        // moved to the L2 parser tests.
    })
    void rejectsInvalidL1(String input) {
        assertThatThrownBy(() -> Edtf.parse(input))
            .isInstanceOf(EdtfParseException.class);
    }

    private static long utcMillis(int y, int mo, int d) {
        return java.time.LocalDateTime.of(y, mo, d, 0, 0)
            .toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }
}
