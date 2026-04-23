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
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import io.github.openhistoricalmap.edtf.types.EdtfSet;
import io.github.openhistoricalmap.edtf.types.ListMember;
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
        "[]",                        // empty set (no markers, no members)
        "{}",                        // empty list
        "[2020",                     // unterminated set
        "2020]",                     // missing open
        "[2020,,2021]",              // empty member
    })
    void rejectsInvalid(String input) {
        assertThatThrownBy(() -> Edtf.parse(input))
            .isInstanceOf(EdtfParseException.class);
    }

    @Test
    void consecutiveRangeInsideList() {
        EdtfList l = (EdtfList) Edtf.parse("{2019..2021}");
        assertThat(l.members()).hasSize(1);
        assertThat(l.members().get(0)).isInstanceOf(ListMember.Consecutive.class);
        assertThat(l.toEdtfString()).isEqualTo("{2019..2021}");
    }

    @Test
    void extendedSeasonCode() {
        // Code 30 = Southern Hemisphere summer per edtf.js, Jul-Sep bounds.
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-30");
        assertThat(s.level()).isEqualTo(EdtfLevel.L2);
        assertThat(s.toEdtfString()).isEqualTo("2020-30");
    }

    @Test
    void extendedSeasonHalfYear() {
        // Code 40 = first half of year (Jan-Jun)
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-40");
        assertThat(s.level()).isEqualTo(EdtfLevel.L2);
    }

    @Test
    void progressiveMaskStaysL1() {
        // 201X is progressive -> should still parse as L1
        EdtfDate d = (EdtfDate) Edtf.parse("201X");
        assertThat(d.level()).isEqualTo(EdtfLevel.L1);
    }

    @Test
    void decadeBasic() {
        io.github.openhistoricalmap.edtf.types.EdtfDecade d =
            (io.github.openhistoricalmap.edtf.types.EdtfDecade) Edtf.parse("199");
        assertThat(d.decade()).isEqualTo(199);
        assertThat(d.firstYear()).isEqualTo(1990);
        assertThat(d.level()).isEqualTo(EdtfLevel.L2);
        assertThat(d.toEdtfString()).isEqualTo("199");
    }

    @Test
    void decadeWithUncertain() {
        io.github.openhistoricalmap.edtf.types.EdtfDecade d =
            (io.github.openhistoricalmap.edtf.types.EdtfDecade) Edtf.parse("199?");
        assertThat(d.uncertain()).isTrue();
        assertThat(d.toEdtfString()).isEqualTo("199?");
    }

    @Test
    void decadeNegative() {
        io.github.openhistoricalmap.edtf.types.EdtfDecade d =
            (io.github.openhistoricalmap.edtf.types.EdtfDecade) Edtf.parse("-005");
        assertThat(d.decade()).isEqualTo(-5);
        assertThat(d.toEdtfString()).isEqualTo("-005");
    }

    @Test
    void consecutiveRangeBoundsCoverStartAndEnd() {
        // Java implementation treats {start..end} as spanning from
        // start.min through end.max, which is semantically the correct
        // interpretation (edtf.js returns start.max for this case,
        // which we consider a bug and diverge from).
        EdtfList l = (EdtfList) Edtf.parse("{2019..2021}");
        assertThat(l.min()).isEqualTo(Edtf.parse("2019").min());
        assertThat(l.max()).isEqualTo(Edtf.parse("2021").max());
    }
}
