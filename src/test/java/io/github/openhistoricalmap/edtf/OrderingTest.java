package io.github.openhistoricalmap.edtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Exercises the default {@link EdtfTemporal#compareTo(EdtfTemporal)} and
 * {@link EdtfTemporal#covers(EdtfTemporal)} behaviour across the type
 * hierarchy. Ordering is by {@code min()} then {@code max()}, so it
 * works across mixed types (a year covers the months inside it, etc.).
 */
class OrderingTest {

    @Test
    void yearsOrderAscending() {
        EdtfTemporal a = Edtf.parse("1999");
        EdtfTemporal b = Edtf.parse("2000");
        EdtfTemporal c = Edtf.parse("2001");
        assertThat(List.of(c, a, b).stream().sorted().toList())
            .containsExactly(a, b, c);
    }

    @Test
    void yearAndMonthCompareByMin() {
        EdtfTemporal year2020 = Edtf.parse("2020");
        EdtfTemporal may = Edtf.parse("2020-05");
        // Year 2020 starts Jan 1; May 2020 starts May 1 -> year < month
        assertThat(year2020.compareTo(may)).isNegative();
    }

    @Test
    void yearCoversMonthInsideIt() {
        EdtfTemporal year = Edtf.parse("2020");
        EdtfTemporal may = Edtf.parse("2020-05");
        EdtfTemporal dec = Edtf.parse("2020-12-31");
        assertThat(year.covers(may)).isTrue();
        assertThat(year.covers(dec)).isTrue();
        // Reverse direction: a single day does not cover the whole year.
        assertThat(may.covers(year)).isFalse();
    }

    @Test
    void coversIsReflexive() {
        EdtfTemporal d = Edtf.parse("2020-05-15");
        assertThat(d.covers(d)).isTrue();
    }

    @Test
    void negativeAndPositiveYears() {
        EdtfTemporal bce = Edtf.parse("-0044");
        EdtfTemporal ce = Edtf.parse("2020");
        assertThat(bce.compareTo(ce)).isNegative();
    }

    @Test
    void centuryCoversYearInside() {
        EdtfTemporal c20 = Edtf.parse("20");        // century 2000-2099
        EdtfTemporal y2020 = Edtf.parse("2020");
        assertThat(c20.covers(y2020)).isTrue();
    }

    @Test
    void intervalWithOpenUpperOrdersAfterFiniteYear() {
        EdtfTemporal open = Edtf.parse("2020/..");
        EdtfTemporal y2019 = Edtf.parse("2019");
        // 2019 starts before 2020 -> 2019 < open
        assertThat(y2019.compareTo(open)).isNegative();
        // open extends to +infinity
        assertThat(open.max()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void intervalWithOpenLowerOrdersBeforeAnyFiniteYear() {
        EdtfTemporal open = Edtf.parse("../2020");
        EdtfTemporal y2021 = Edtf.parse("2021");
        assertThat(open.compareTo(y2021)).isNegative();
        assertThat(open.min()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void intervalWithUnknownEndpointThrowsOnBounds() {
        EdtfTemporal i = Edtf.parse("/2020");
        // min should throw IllegalStateException; max should work
        assertThatThrownBy(i::min).isInstanceOf(IllegalStateException.class);
        assertThat(i.max()).isGreaterThan(0L);
    }

    @Test
    void seasonOrdersWithinItsYear() {
        EdtfTemporal q1 = Edtf.parse("2020-21");
        EdtfTemporal q4 = Edtf.parse("2020-24");
        assertThat(q1.compareTo(q4)).isNegative();
    }

    @Test
    void uncertainAndApproximateDatesOrderByBaseValue() {
        // The qualifier doesn't change bounds in L1, so ordering is
        // still by the underlying date.
        EdtfTemporal q = Edtf.parse("2020-05?");
        EdtfTemporal plain = Edtf.parse("2020-05");
        assertThat(q.compareTo(plain)).isZero();
    }

    @Test
    void maskedDateBoundsDriveOrdering() {
        EdtfTemporal m = Edtf.parse("201X");
        EdtfTemporal y = Edtf.parse("2020");
        // 201X spans 2010..2019 so it starts before 2020.
        assertThat(m.compareTo(y)).isNegative();
    }
}
