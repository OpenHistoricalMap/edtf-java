/**
 * Public entry points for edtf-java.
 *
 * <p>Typical usage:
 * <pre>{@code
 * EdtfTemporal t = Edtf.parse("2020-XX");
 * long lo = t.min();
 * long hi = t.max();
 * }</pre>
 *
 * <p>This package exposes the {@link io.github.openhistoricalmap.edtf.Edtf Edtf}
 * static façade and the sealed
 * {@link io.github.openhistoricalmap.edtf.EdtfTemporal EdtfTemporal}
 * interface that all parsed EDTF values implement.
 *
 * <p>The library targets Java 17+ and has zero runtime dependencies.
 */
package io.github.openhistoricalmap.edtf;
