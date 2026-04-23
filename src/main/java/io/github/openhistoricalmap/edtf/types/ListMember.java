package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfTemporal;
import java.util.Objects;

/**
 * One entry inside an {@link EdtfList} or {@link EdtfSet}. EDTF lists
 * allow either a single value ({@link Single}) or an inclusive
 * start..end range ({@link Consecutive}).
 *
 * <p>Phase 6 ships only {@link Single}. The {@link Consecutive}
 * variant is reserved for a later release; the sealed hierarchy is
 * declared now so downstream callers can switch exhaustively.
 */
public sealed interface ListMember permits ListMember.Single, ListMember.Consecutive {

    /** Canonical EDTF fragment (no surrounding delimiters or separators). */
    String toEdtfFragment();

    /** A single parsed EDTF value inside a list / set. */
    record Single(EdtfTemporal value) implements ListMember {
        public Single {
            Objects.requireNonNull(value, "value");
        }
        @Override public String toEdtfFragment() { return value.toEdtfString(); }
    }

    /**
     * An inclusive range {@code start..end} inside a list / set, as in
     * {@code {2019..2021}} meaning &quot;2019, 2020, or 2021&quot;.
     * Reserved for a future release.
     */
    record Consecutive(EdtfTemporal start, EdtfTemporal end) implements ListMember {
        public Consecutive {
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
        }
        @Override public String toEdtfFragment() {
            return start.toEdtfString() + ".." + end.toEdtfString();
        }
    }
}
