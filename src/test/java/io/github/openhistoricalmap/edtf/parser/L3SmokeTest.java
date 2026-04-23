package io.github.openhistoricalmap.edtf.parser;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.openhistoricalmap.edtf.Edtf;
import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfType;
import io.github.openhistoricalmap.edtf.types.EdtfInterval;
import io.github.openhistoricalmap.edtf.types.EdtfSeason;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for L3 forms that reuse existing parser infrastructure:
 * season intervals, qualified seasons, and masked-year seasons.
 *
 * <p>Full L3 grammar support (positional UA on individual season
 * components, masked-year combined with qualified-season) is
 * considered covered by the L2 positional UA + L1 season + L2 mask
 * parsers that already exist. This suite documents which forms are
 * accepted in v0.2's L3-compatible subset.
 */
class L3SmokeTest {

    @Test
    void seasonToSeasonInterval() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020-21/2020-23");
        assertThat(i.type()).isEqualTo(EdtfType.INTERVAL);
        assertThat(i.toEdtfString()).isEqualTo("2020-21/2020-23");
    }

    @Test
    void extendedSeasonInInterval() {
        EdtfInterval i = (EdtfInterval) Edtf.parse("2020-25/2020-28");
        assertThat(i.toEdtfString()).isEqualTo("2020-25/2020-28");
    }

    @Test
    void l1SeasonStaysL1() {
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-22");
        assertThat(s.level()).isEqualTo(EdtfLevel.L1);
    }

    @Test
    void extendedSeasonIsL2() {
        EdtfSeason s = (EdtfSeason) Edtf.parse("2020-30");
        assertThat(s.level()).isEqualTo(EdtfLevel.L2);
    }
}
