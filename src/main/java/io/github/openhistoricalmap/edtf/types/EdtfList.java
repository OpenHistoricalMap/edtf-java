package io.github.openhistoricalmap.edtf.types;

import io.github.openhistoricalmap.edtf.EdtfLevel;
import io.github.openhistoricalmap.edtf.EdtfTemporal;
import io.github.openhistoricalmap.edtf.EdtfType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An L2 list of alternate-date candidates, written in curly braces, as
 * in {@code {2020, 2021..2023, 2025}}.
 *
 * <p>The {@code earlier} / {@code later} flags represent the
 * {@code ..} delimiters inside the opening / closing braces,
 * indicating the true set extends before / after the listed members.
 *
 * <p>Bounds cover every member. {@link #min()} returns the smallest
 * member {@code min}; {@link #max()} returns the largest member
 * {@code max}. Empty lists are rejected at construction.
 */
public final class EdtfList implements EdtfTemporal {

    private final List<ListMember> members;
    private final boolean earlier;
    private final boolean later;

    private EdtfList(List<ListMember> members, boolean earlier, boolean later) {
        Objects.requireNonNull(members, "members");
        if (members.isEmpty() && !earlier && !later) {
            throw new IllegalArgumentException("list must have at least one member");
        }
        this.members = List.copyOf(members);
        this.earlier = earlier;
        this.later = later;
    }

    public static EdtfList of(List<ListMember> members) {
        return new EdtfList(members, false, false);
    }

    public static EdtfList of(List<ListMember> members, boolean earlier, boolean later) {
        return new EdtfList(members, earlier, later);
    }

    public List<ListMember> members() { return members; }
    public boolean earlier() { return earlier; }
    public boolean later() { return later; }

    @Override public EdtfType type() { return EdtfType.LIST; }

    @Override public EdtfLevel level() { return EdtfLevel.L2; }

    @Override public long min() {
        if (earlier) return Long.MIN_VALUE;
        if (members.isEmpty()) return Long.MAX_VALUE;
        return members.stream().mapToLong(EdtfList::memberMin).min().getAsLong();
    }

    @Override public long max() {
        if (later) return Long.MAX_VALUE;
        if (members.isEmpty()) return Long.MIN_VALUE;
        return members.stream().mapToLong(EdtfList::memberMax).max().getAsLong();
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
        StringBuilder sb = new StringBuilder("{");
        if (earlier) sb.append("..");
        sb.append(body);
        if (later) sb.append("..");
        sb.append("}");
        return sb.toString();
    }

    @Override public String toString() { return toEdtfString(); }

    @Override public boolean equals(Object o) {
        return o instanceof EdtfList l
            && l.earlier == earlier && l.later == later
            && Objects.equals(l.members, members);
    }

    @Override public int hashCode() { return Objects.hash(members, earlier, later); }
}
