/**
 * edtf-java — Java library for parsing, comparing, and rendering
 * Extended Date/Time Format (EDTF) strings per ISO 8601-2:2019
 * (with Amendment 1, 2025) and the Library of Congress EDTF
 * specification.
 *
 * <p>Public API packages are exported; {@code parser} and
 * {@code internal} packages are implementation details and may
 * change without notice.
 */
module io.github.openhistoricalmap.edtf {
    exports io.github.openhistoricalmap.edtf;
    exports io.github.openhistoricalmap.edtf.types;
    // The format package will be exported when localized formatting
    // lands in Phase 7. The parser and internal packages remain
    // unexported implementation detail.
}
