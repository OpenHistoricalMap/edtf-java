package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfParseException;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import io.github.openhistoricalmap.edtf.types.EdtfDate;
import io.github.openhistoricalmap.edtf.types.EdtfList;
import io.github.openhistoricalmap.edtf.types.EdtfSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class L2ParserTest {

    @Test
    void nonProgressiveYearMask() {
        EdtfDate d = (EdtfDate) Edtf.parse("2X1X");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        assertThat(d.toEdtfString()).isEqualTo("2X1X");
    }

    @Test
    void partialMonthMask() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-0X");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        assertThat(d.toEdtfString()).isEqualTo("2020-0X");
    }

    @Test
    void partialDayMask() {
        EdtfDate d = (EdtfDate) Edtf.parse("2020-05-X5");
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        assertThat(d.toEdtfString()).isEqualTo("2020-05-X5");
    }

    @Test
    void setWithSingleMembers() {
        EdtfSet s = (EdtfSet) Edtf.parse("[2020, 2021, 2023]");
        assertThat(s.type()).isEqualTo(EdtfType.SET);
        assertThat(s.level()).isEqualTo(EdtfLevel.L2);
        assertThat(s.members()).hasSize(3);
        // Canonical output drops the space after commas, matching edtf.js.
        assertThat(s.toEdtfString()).isEqualTo("[2020,2021,2023]");
    }

    @Test
    void listWithSingleMembers() {
        EdtfList l = (EdtfList) Edtf.parse("{2020, 2021}");
        assertThat(l.members()).hasSize(2);
        assertThat(l.toEdtfString()).isEqualTo("{2020,2021}");
    }

    @Test
    void setEarlier() {
        EdtfSet s = (EdtfSet) Edtf.parse("[..2020, 2021]");
        assertThat(s.earlier()).isTrue();
        assertThat(s.later()).isFalse();
        assertThat(s.min()).isEqualTo(Long.MIN_VALUE);
        assertThat(s.toEdtfString()).isEqualTo("[..2020,2021]");
    }

    @Test
    void listLater() {
        EdtfList l = (EdtfList) Edtf.parse("{2020..}");
        assertThat(l.later()).isTrue();
        assertThat(l.max()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void setBoundsCoverAllMembers() {
        EdtfSet s = (EdtfSet) Edtf.parse("[2019, 2025, 2020]");
        // min = Jan 1 2019, max = Dec 31 2025.
        assertThat(s.min()).isEqualTo(Edtf.parse("2019").min());
        assertThat(s.max()).isEqualTo(Edtf.parse("2025").max());
    }

    @Test
    void mixedLevelSetElevatesWholeToL2() {
        EdtfSet s = (EdtfSet) Edtf.parse("[2020?, 2021]");
        assertThat(s.level()).isEqualTo(EdtfLevel.L2);
        assertThat(s.members()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "[]",                        // empty set
        "{}",                        // empty list
        "[2020",                     // unterminated set
        "2020]",                     // missing open
        "[2020,,2021]",              // empty member
        "{2019..2021}",              // consecutive range not supported yet
    })
    void rejectsInvalid(String input) {
        assertThatThrownBy(() -> Edtf.parse(input))
            .isInstanceOf(EdtfParseException.class);
    }

    @Test
    void progressiveMaskStaysL1() {
        // 201X is progressive -> should still parse as L1
        EdtfDate d = (EdtfDate) Edtf.parse("201X");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
    }
}
