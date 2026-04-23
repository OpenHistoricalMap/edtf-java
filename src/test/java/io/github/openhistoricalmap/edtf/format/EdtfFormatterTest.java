package io.github.openhistoricalmap.edtf.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.openhistoricalmap.edtf.Edtf;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class EdtfFormatterTest {

    private final EdtfFormatter f = EdtfFormatter.forLocale(Locale.US);

    @Test
    void yearOnly() {
        assertThat(f.format(Edtf.parse("2020"))).isEqualTo("2020");
    }

    @Test
    void bceYear() {
        // -44 represents the year ASE Caesar was assassinated.
        assertThat(f.format(Edtf.parse("-0044"))).isEqualTo("44 BCE");
    }

    @Test
    void yearMonth() {
        assertThat(f.format(Edtf.parse("2020-05"))).isEqualTo("May 2020");
    }

    @Test
    void yearMonthDay() {
        // US locale: "May 15, 2020" or similar (long form).
        String out = f.format(Edtf.parse("2020-05-15"));
        assertThat(out).contains("2020").contains("May").contains("15");
    }

    @Test
    void uncertainYear() {
        assertThat(f.format(Edtf.parse("2020?"))).isEqualTo("2020 (uncertain)");
    }

    @Test
    void approximateYear() {
        assertThat(f.format(Edtf.parse("2020~"))).isEqualTo("circa 2020");
    }

    @Test
    void mixedQualifierYear() {
        assertThat(f.format(Edtf.parse("2020%"))).isEqualTo("circa 2020 (uncertain)");
    }

    @Test
    void approximateMonth() {
        assertThat(f.format(Edtf.parse("2020-05~"))).isEqualTo("circa May 2020");
    }

    @Test
    void decade() {
        assertThat(f.format(Edtf.parse("199"))).isEqualTo("the 1990s");
    }

    @Test
    void centuryFromCenturyForm() {
        assertThat(f.format(Edtf.parse("20"))).isEqualTo("the 2000s");
    }

    @Test
    void seasonQ1() {
        assertThat(f.format(Edtf.parse("2020-21"))).isEqualTo("Q1 2020");
    }

    @Test
    void seasonQ4() {
        assertThat(f.format(Edtf.parse("2020-24"))).isEqualTo("Q4 2020");
    }

    @Test
    void boundedInterval() {
        assertThat(f.format(Edtf.parse("2020/2021"))).isEqualTo("2020 to 2021");
    }

    @Test
    void openLowerInterval() {
        assertThat(f.format(Edtf.parse("../2020"))).isEqualTo("before 2020");
    }

    @Test
    void openUpperInterval() {
        assertThat(f.format(Edtf.parse("2020/.."))).isEqualTo("2020 onwards");
    }

    @Test
    void doubleOpenInterval() {
        assertThat(f.format(Edtf.parse("../.."))).isEqualTo("any time");
    }

    @Test
    void largeYNotation() {
        // Y100000 -> "Year 100,000" (US locale)
        assertThat(f.format(Edtf.parse("Y100000"))).isEqualTo("Year 100,000");
    }

    @Test
    void setTwoMembers() {
        // Default rule: "X or Y"
        assertThat(f.format(Edtf.parse("[2020,2021]"))).isEqualTo("2020 or 2021");
    }

    @Test
    void listTwoMembers() {
        assertThat(f.format(Edtf.parse("{2020,2021}"))).isEqualTo("2020 and 2021");
    }

    @Test
    void setManyMembers() {
        assertThat(f.format(Edtf.parse("[2020,2021,2023]")))
            .isEqualTo("one of: 2020, 2021, 2023");
    }

    @Test
    void maskedDateFallsBackToCanonical() {
        // X-masks aren't yet handled by the formatter; falls back.
        assertThat(f.format(Edtf.parse("2020-XX"))).isEqualTo("2020-XX");
    }
}
