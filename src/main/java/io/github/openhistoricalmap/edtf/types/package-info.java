/**
 * Concrete EDTF temporal value types permitted by the sealed
 * {@link io.github.openhistoricalmap.edtf.EdtfTemporal} interface.
 *
 * <p>Every class in this package corresponds to one of the eight
 * {@link io.github.openhistoricalmap.edtf.EdtfType} kinds:
 * {@link io.github.openhistoricalmap.edtf.types.EdtfDate},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfYear},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfDecade},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfCentury},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfSeason},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfInterval},
 * {@link io.github.openhistoricalmap.edtf.types.EdtfList}, and
 * {@link io.github.openhistoricalmap.edtf.types.EdtfSet}.
 *
 * <p>All types are immutable.
 *
 * <p><strong>Phase 2 note:</strong> at {@code 0.1.0-SNAPSHOT} these
 * classes are stubs that throw
 * {@link UnsupportedOperationException} from every method other than
 * {@code type()}. Real implementations land as subsequent phases
 * complete the parser and bound-calculation work.
 */
package io.github.openhistoricalmap.edtf.types;
