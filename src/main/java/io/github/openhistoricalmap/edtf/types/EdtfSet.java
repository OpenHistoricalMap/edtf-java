package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An L2 set of alternate dates, written in square brackets, as in
 * {@code [2020, 2021]}. Identical to {@link EdtfList} in content
 * model (supports {@link ListMember}s, {@code earlier} / {@code later}
 * flags) &mdash; only the delimiters differ in the canonical string
 * form.
 *
 * <p>In EDTF, a set represents "one of these values" whereas a list
 * represents "several of these values"; the distinction is semantic
 * rather than structural.
 */
public final class EdtfSet implements EdtfTemporal {

    private final List<ListMember> members;
    private final boolean earlier;
    private final boolean later;

    private EdtfSet(List<ListMember> members, boolean earlier, boolean later) {
        Objects.requireNonNull(members, "members");
        if (members.isEmpty() && !earlier && !later) {
            throw new IllegalArgumentException("set must have at least one member");
        }
        this.members = List.copyOf(members);
        this.earlier = earlier;
        this.later = later;
    }

    public static EdtfSet of(List<ListMember> members) {
        return new EdtfSet(members, false, false);
    }

    public static EdtfSet of(List<ListMember> members, boolean earlier, boolean later) {
        return new EdtfSet(members, earlier, later);
    }

    public List<ListMember> members() { return members; }
    public boolean earlier() { return earlier; }
    public boolean later() { return later; }

    @Override public EdtfType type() { return EdtfType.SET; }

    @Override public EdtfLevel level() { return EdtfLevel.L2; }

    @Override public long min() {
        if (earlier) return Long.MIN_VALUE;
        if (members.isEmpty()) return Long.MAX_VALUE;
        return members.stream().mapToLong(EdtfSet::memberMin).min().getAsLong();
    }

    @Override public long max() {
        if (later) return Long.MAX_VALUE;
        if (members.isEmpty()) return Long.MIN_VALUE;
        return members.stream().mapToLong(EdtfSet::memberMax).max().getAsLong();
    }

    private static long memberMin(ListMember m) {
        if (m instanceof ListMember.Single s) return s.value().min();
        if (m instanceof ListMember.Consecutive c) return c.start().min();
        throw new IllegalStateException();
    }

    private static long memberMax(ListMember m) {
        if (m instanceof ListMember.Single s) return s.value().max();
        if (m instanceof ListMember.Consecutive c) return c.end().max();
        throw new IllegalStateException();
    }

    @Override public String toEdtfString() {
        String body = members.stream().map(ListMember::toEdtfFragment)
            .collect(Collectors.joining(","));
        StringBuilder sb = new StringBuilder("[");
        if (earlier) sb.append("..");
        sb.append(body);
        if (later) sb.append("..");
        sb.append("]");
        return sb.toString();
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfSet s
            && s.earlier == earlier && s.later == later
            && Objects.equals(s.members, members);
    }

    @Override public int hashCode() { return Objects.hash(members, earlier, later); }
}
