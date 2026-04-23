package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import org.junit.jupiter.api.Test;

/**
 * L2 positional UA (uncertain / approximate / mixed markers attached
 * to individual year, month, or day components, rather than to the
 * whole date).
 */
class L2PositionalUaTest {

    @Test
    void leadingUaOnYearOnly() {
        EdtfDate d = (EdtfDate) Edtf.parse("?2020");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
        // Position 0 alone sets YEAR bits -> same as L1 trailing form
        assertThat(d.toEdtfString()).isEqualTo("2020?");
    }

    @Test
    void uaBeforeMonthOnly() {
        // 2020-?05: month-only uncertain -> bitmask = MONTH (0x30), L2
        EdtfDate d = (EdtfDate) Edtf.parse("2020-?05");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        // Qualified(2) = MONTH-only -> leading marker on month
        assertThat(d.toEdtfString()).isEqualTo("2020-?05");
    }

    @Test
    void uaBeforeDayOnly() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-?15");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        assertThat(d.toEdtfString()).isEqualTo("2020-05-?15");
    }

    @Test
    void mixedUaYearUncertainMonthApproximate() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020?-05~");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        // year is uncertain, month is approximate -> not a plain L1 whole-date marker
        assertThat(d.uncertain().value()).isNotZero();
        assertThat(d.approximate().value()).isNotZero();
    }

    @Test
    void trailingApproximateOnMonthPrecision() {
        // L1 shape (whole-date ~): YM mask applied, still L1
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05~");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
        assertThat(d.toEdtfString()).isEqualTo("2020-05~");
    }

    @Test
    void percentOnMonthPositional() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-%05");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        // % = both uncertain and approximate at position 2 (before month)
        assertThat(d.uncertain().value()).isNotZero();
        assertThat(d.approximate().value()).isNotZero();
    }
}
