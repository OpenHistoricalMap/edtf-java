package io.github.openhistoricalmap.edtf.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 port of {@code edtf.js/test/bitmask.js}. Each top-level block
 * in the JS test file maps to a {@link Nested} class here, and each
 * JS {@code it(...)} assertion is preserved either verbatim or as the
 * nearest Java equivalent. Any additional test is an extension clearly
 * marked {@code extra: ...}.
 */
class BitmaskTest {

    @Nested
    @DisplayName("instance")
    class InstanceTests {

        @Test
        @DisplayName("is zero by default")
        void zeroByDefault() {
            Bitmask bm = new Bitmask();
            assertThat(bm.value()).isEqualTo(0);
            assertThat(bm.test(0)).isEqualTo(0);
            assertThat(bm.test("day")).isEqualTo(0);
            assertThat(bm.test("month")).isEqualTo(0);
            assertThat(bm.test("year")).isEqualTo(0);
        }

        @Nested
        @DisplayName("mask()")
        class MaskTests {
            @Test
            @DisplayName("YYYYMMXX")
            void yyyymmxx() {
                assertThat(new String(new Bitmask().set("day").mask()))
                    .isEqualTo("YYYYMMXX");
            }

            @Test
            @DisplayName("YYYYXXDD")
            void yyyyxxdd() {
                assertThat(new String(new Bitmask().set("month").mask()))
                    .isEqualTo("YYYYXXDD");
            }

            @Test
            @DisplayName("XXXXMMDD")
            void xxxxmmdd() {
                assertThat(new String(new Bitmask().set("year").mask()))
                    .isEqualTo("XXXXMMDD");
            }
        }

        @Nested
        @DisplayName("toString()")
        class ToStringTests {
            @Test
            @DisplayName("YYYY-MM-DD")
            void blank() {
                assertThat(new Bitmask().toString()).isEqualTo("YYYY-MM-DD");
            }

            @Test
            @DisplayName("YYYY-MM-XX")
            void dayUnspecified() {
                assertThat(new Bitmask().set("day").toString())
                    .isEqualTo("YYYY-MM-XX");
            }
        }
    }

    @Nested
    @DisplayName("max()")
    class MaxTests {

        @Test
        @DisplayName("year")
        void year() {
            assertThat(new Bitmask().max(new String[]{"2016"}))
                .containsExactly(2016);
            assertThat(new Bitmask("year").max(new String[]{"2016"}))
                .containsExactly(9999);
            assertThat(new Bitmask("yxyxmmdd").max(new String[]{"2016"}))
                .containsExactly(2919);
        }

        @Test
        @DisplayName("month")
        void month() {
            assertThat(new Bitmask().max(new String[]{"2016", "01"}))
                .containsExactly(2016, 0);
            assertThat(new Bitmask("yyyyxxdd").max(new String[]{"2016", "01"}))
                .containsExactly(2016, 11);
            assertThat(new Bitmask("yyyymxdd").max(new String[]{"2016", "01"}))
                .containsExactly(2016, 8);
            assertThat(new Bitmask("yyyymxdd").max(new String[]{"2016", "10"}))
                .containsExactly(2016, 11);
            assertThat(new Bitmask("yyyyxmdd").max(new String[]{"2016", "01"}))
                .containsExactly(2016, 10);
            assertThat(new Bitmask("yyyyxmdd").max(new String[]{"2016", "02"}))
                .containsExactly(2016, 11);
            assertThat(new Bitmask("yyyyxmdd").max(new String[]{"2016", "03"}))
                .containsExactly(2016, 2);
        }

        @Test
        @DisplayName("day")
        void day() {
            assertThat(new Bitmask().max(new String[]{"2016", "01", "01"}))
                .containsExactly(2016, 0, 1);
            assertThat(new Bitmask("yyyymmxx").max(new String[]{"2016", "01", "01"}))
                .containsExactly(2016, 0, 31);
            assertThat(new Bitmask("yyyymmxx").max(new String[]{"2016", "02", "01"}))
                .containsExactly(2016, 1, 29);
            assertThat(new Bitmask("yyyymmxx").max(new String[]{"2016", "04", "01"}))
                .containsExactly(2016, 3, 30);
        }
    }

    @Nested
    @DisplayName("min()")
    class MinTests {

        @Test
        @DisplayName("year")
        void year() {
            assertThat(new Bitmask().min(new String[]{"2016"}))
                .containsExactly(2016);
            assertThat(new Bitmask("year").min(new String[]{"2016"}))
                .containsExactly(0);
            assertThat(new Bitmask("yxyxmmdd").min(new String[]{"2016"}))
                .containsExactly(2010);
        }

        @Test
        @DisplayName("month")
        void month() {
            assertThat(new Bitmask().min(new String[]{"2019", "11"}))
                .containsExactly(2019, 10);
            assertThat(new Bitmask("yyyyxxdd").min(new String[]{"2019", "11"}))
                .containsExactly(2019, 0);

            assertThat(new Bitmask("yyyymxdd").min(new String[]{"2019", "09"}))
                .containsExactly(2019, 0);
            assertThat(new Bitmask("yyyymxdd").min(new String[]{"2019", "10"}))
                .containsExactly(2019, 9);
            assertThat(new Bitmask("yyyymxdd").min(new String[]{"2019", "11"}))
                .containsExactly(2019, 9);
            assertThat(new Bitmask("yyyymxdd").min(new String[]{"2019", "12"}))
                .containsExactly(2019, 9);

            assertThat(new Bitmask("yyyyxmdd").min(new String[]{"2019", "03"}))
                .containsExactly(2019, 0);
            assertThat(new Bitmask("yyyyxmdd").min(new String[]{"2019", "12"}))
                .containsExactly(2019, 0);
        }

        @Test
        @DisplayName("day")
        void day() {
            assertThat(new Bitmask().min(new String[]{"2019", "01", "11"}))
                .containsExactly(2019, 0, 11);
            assertThat(new Bitmask("yyyymmxx").min(new String[]{"2019", "01", "03"}))
                .containsExactly(2019, 0, 1);
            assertThat(new Bitmask("yyyymmxx").min(new String[]{"2019", "01", "13"}))
                .containsExactly(2019, 0, 1);
            assertThat(new Bitmask("yyyymmxx").min(new String[]{"2019", "01", "31"}))
                .containsExactly(2019, 0, 1);
            assertThat(new Bitmask("yyyymmdx").min(new String[]{"2019", "01", "03"}))
                .containsExactly(2019, 0, 1);
            assertThat(new Bitmask("yyyymmdx").min(new String[]{"2019", "01", "13"}))
                .containsExactly(2019, 0, 10);
            assertThat(new Bitmask("yyyymmdx").min(new String[]{"2019", "01", "31"}))
                .containsExactly(2019, 0, 30);
            assertThat(new Bitmask("yyyymmxd").min(new String[]{"2019", "01", "03"}))
                .containsExactly(2019, 0, 3);
            assertThat(new Bitmask("yyyymmxd").min(new String[]{"2019", "01", "13"}))
                .containsExactly(2019, 0, 3);
            assertThat(new Bitmask("yyyymmxd").min(new String[]{"2019", "01", "31"}))
                .containsExactly(2019, 0, 1);
            assertThat(new Bitmask("yyyymmxd").min(new String[]{"2019", "01", "30"}))
                .containsExactly(2019, 0, 1);
        }
    }

    @Nested
    @DisplayName("static test()")
    class StaticTest {

        @Test
        @DisplayName("true")
        void trueForms() {
            assertThat(Bitmask.test(true, true)).isNotZero();
            assertThat(Bitmask.test(true, "day")).isNotZero();
            assertThat(Bitmask.test(true, "month")).isNotZero();
            assertThat(Bitmask.test(true, "year")).isNotZero();
            assertThat(Bitmask.test(true, false)).isZero();
        }

        @Test
        @DisplayName("day")
        void day() {
            assertThat(Bitmask.test("day", "day")).isNotZero();
            assertThat(Bitmask.test("day", "month")).isZero();
            assertThat(Bitmask.test("day", "year")).isZero();
            assertThat(Bitmask.test("day", true)).isNotZero();
        }

        @Test
        @DisplayName("month")
        void month() {
            assertThat(Bitmask.test("month", "month")).isNotZero();
            assertThat(Bitmask.test("month", "year")).isZero();
            assertThat(Bitmask.test("month", "day")).isZero();
            assertThat(Bitmask.test("month", true)).isNotZero();
            assertThat(Bitmask.test("month", Bitmask.Y)).isZero();
            assertThat(Bitmask.test("month", Bitmask.YM)).isNotZero();
            assertThat(Bitmask.test("month", Bitmask.YMD)).isNotZero();
        }

        @Test
        @DisplayName("year")
        void year() {
            assertThat(Bitmask.test("year", "year")).isNotZero();
            assertThat(Bitmask.test("year", "day")).isZero();
            assertThat(Bitmask.test("year", "month")).isZero();
            assertThat(Bitmask.test("year", true)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.Y)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.YM)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.YMD)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.YYXX)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.YYYX)).isNotZero();
            assertThat(Bitmask.test("year", Bitmask.XXXX)).isNotZero();
        }

        @Test
        @DisplayName("false")
        void falseForms() {
            assertThat(Bitmask.test(false, false)).isZero();
            assertThat(Bitmask.test(false, "day")).isZero();
            assertThat(Bitmask.test(false, "month")).isZero();
            assertThat(Bitmask.test(false, "year")).isZero();
            assertThat(Bitmask.test(false, true)).isZero();
        }
    }

    @Nested
    @DisplayName("static values()")
    class ValuesTests {

        @Test
        @DisplayName("XXXXXXXX")
        void allMasked() {
            assertThat(Bitmask.values("XXXXXXXX")).containsExactly(0, 0, 1);
            assertThat(Bitmask.values("XXXXXXXX", '9')).containsExactly(9999, 11, 31);
        }

        @Test
        @DisplayName("XXXXXXDD")
        void dayKnown() {
            assertThat(Bitmask.values("XXXXXX31")).containsExactly(0, 0, 31);
            assertThat(Bitmask.values("XXXXXX31", '9')).containsExactly(9999, 11, 31);
        }

        @Test
        @DisplayName("XXXXMMXX")
        void monthKnown() {
            assertThat(Bitmask.values("XXXX05XX")).containsExactly(0, 4, 1);
            assertThat(Bitmask.values("XXXX05XX", '9')).containsExactly(9999, 4, 31);
            assertThat(Bitmask.values("XXXX02XX", '9')).containsExactly(9999, 1, 29);
            assertThat(Bitmask.values("XXXX06XX", '9')).containsExactly(9999, 5, 30);
        }

        @Test
        @DisplayName("YYYYXXXX")
        void yearKnown() {
            assertThat(Bitmask.values("2014XXXX")).containsExactly(2014, 0, 1);
            assertThat(Bitmask.values("2014XXXX", '9')).containsExactly(2014, 11, 31);
        }

        @Test
        @DisplayName("XXXXXXDX")
        void dayTensKnown() {
            assertThat(Bitmask.values("XXXXXX3X")).containsExactly(0, 0, 30);
            assertThat(Bitmask.values("XXXXXX3X", '9')).containsExactly(9999, 11, 31);
            assertThat(Bitmask.values("XXXXXX2X")).containsExactly(0, 0, 20);
            assertThat(Bitmask.values("XXXXXX2X", '9')).containsExactly(9999, 11, 29);
        }

        @Test
        @DisplayName("XXXXMXXX")
        void monthTensKnown() {
            assertThat(Bitmask.values("XXXX0XXX")).containsExactly(0, 0, 1);
            assertThat(Bitmask.values("XXXX0XXX", '9')).containsExactly(9999, 8, 30);
            assertThat(Bitmask.values("XXXX1XXX")).containsExactly(0, 9, 1);
            assertThat(Bitmask.values("XXXX1XXX", '9')).containsExactly(9999, 11, 31);
        }

        @Test
        @DisplayName("XXXXMX")
        void monthTensKnownNoDay() {
            assertThat(Bitmask.values("XXXX0X")).containsExactly(0, 0);
            assertThat(Bitmask.values("XXXX0X", '9')).containsExactly(9999, 8);
            assertThat(Bitmask.values("XXXX1X")).containsExactly(0, 9);
            assertThat(Bitmask.values("XXXX1X", '9')).containsExactly(9999, 11);
        }
    }

    @Nested
    @DisplayName("extras")
    class Extras {

        @Test
        @DisplayName("convert rejects invalid strings")
        void convertRejects() {
            assertThatThrownBy(() -> Bitmask.convert("xyz"))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Bitmask.convert("yyymmdd"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constants have expected numeric values")
        void constants() {
            assertThat(Bitmask.YEAR).isEqualTo(0x0F);
            assertThat(Bitmask.MONTH).isEqualTo(0x30);
            assertThat(Bitmask.DAY).isEqualTo(0xC0);
            assertThat(Bitmask.YMD).isEqualTo(0xFF);
            assertThat(Bitmask.MX).isEqualTo(0x20);
            assertThat(Bitmask.XM).isEqualTo(0x10);
            assertThat(Bitmask.DX).isEqualTo(0x80);
            assertThat(Bitmask.XD).isEqualTo(0x40);
            assertThat(Bitmask.XXXX).isEqualTo(Bitmask.YEAR);
            assertThat(Bitmask.YYYX).isEqualTo(0x08);
            assertThat(Bitmask.YYXX).isEqualTo(0x0C);
        }

        @Test
        @DisplayName("instances are immutable; add and set return new instances")
        void immutable() {
            Bitmask empty = new Bitmask();
            Bitmask withDay = empty.add("day");
            assertThat(empty.value()).isEqualTo(0);
            assertThat(withDay.value()).isEqualTo(Bitmask.DAY);
            assertThat(withDay).isNotSameAs(empty);
        }

        @Test
        @DisplayName("qualified matches the edtf.js ~YYYY~-~MM~-~DD~ positional table")
        void qualifiedPositions() {
            // ? after the year: YEAR only, or year bit set and month bit clear
            Bitmask yearOnly = new Bitmask(Bitmask.YEAR);
            assertThat(yearOnly.qualified(1)).isTrue();
            assertThat(yearOnly.qualified(0)).isFalse();

            // ? after the month: YM
            Bitmask ym = new Bitmask(Bitmask.YM);
            assertThat(ym.qualified(3)).isTrue();

            // ? after the day: YMD
            Bitmask ymd = new Bitmask(Bitmask.YMD);
            assertThat(ymd.qualified(5)).isTrue();

            // default (no bits set): nothing is qualified
            Bitmask empty = new Bitmask();
            for (int i = 0; i < 6; i++) {
                assertThat(empty.qualified(i)).isFalse();
            }
        }
    }
}
