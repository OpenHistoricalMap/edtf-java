package io.github.openhistoricalmap.edtf.parser;

import io.github.openhistoricalmap.edtf.EdtfParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private mutable cursor over the input string. Supports the
 * kind of local look-ahead and backtracking a hand-written
 * recursive-descent parser needs, plus farthest-error tracking so
 * higher layers can emit helpful PEG-style messages.
 */
final class Cursor {

    private final String input;
    private int pos;

    /** Farthest byte offset the parser has reached while failing. */
    private int farthestPos;

    /** Token kinds expected at {@link #farthestPos}. */
    private final List<String> farthestExpected = new ArrayList<>();

    Cursor(String input) {
        this.input = input == null ? "" : input;
        this.pos = 0;
        this.farthestPos = 0;
    }

    String input() { return input; }

    int pos() { return pos; }

    int length() { return input.length(); }

    boolean atEnd() { return pos >= input.length(); }

    int remaining() { return input.length() - pos; }

    char peek() { return atEnd() ? '\0' : input.charAt(pos); }

    char peek(int offset) {
        int idx = pos + offset;
        return (idx >= 0 && idx < input.length()) ? input.charAt(idx) : '\0';
    }

    /** Consume {@code c} if present; otherwise record expectation and return false. */
    boolean accept(char c) {
        if (!atEnd() && input.charAt(pos) == c) {
            pos++;
            return true;
        }
        expect("'" + c + "'");
        return false;
    }

    /** Consume {@code s} if present; otherwise record expectation and return false. */
    boolean accept(String s) {
        if (pos + s.length() <= input.length() && input.startsWith(s, pos)) {
            pos += s.length();
            return true;
        }
        expect("'" + s + "'");
        return false;
    }

    /** Consume {@code c} or throw a parse error. */
    void require(char c) {
        if (!accept(c)) {
            throw error("'" + c + "'");
        }
    }

    /** Consume exactly {@code n} ASCII digits and return them, or null on failure. */
    String digits(int n) {
        if (pos + n > input.length()) {
            expect(n + " digits");
            return null;
        }
        for (int i = 0; i < n; i++) {
            char ch = input.charAt(pos + i);
            if (ch < '0' || ch > '9') {
                expect(n + " digits");
                return null;
            }
        }
        String out = input.substring(pos, pos + n);
        pos += n;
        return out;
    }

    /** Consume as many digits as possible (at least {@code min}), up to {@code max}. */
    String digitsBetween(int min, int max) {
        int count = 0;
        while (count < max && pos + count < input.length()) {
            char ch = input.charAt(pos + count);
            if (ch < '0' || ch > '9') break;
            count++;
        }
        if (count < min) {
            expect("between " + min + " and " + max + " digits");
            return null;
        }
        String out = input.substring(pos, pos + count);
        pos += count;
        return out;
    }

    /** Snapshot the current position. */
    int mark() { return pos; }

    /** Rewind to a previous {@link #mark()}. */
    void reset(int mark) {
        if (mark < 0 || mark > input.length()) {
            throw new IllegalArgumentException("invalid mark: " + mark);
        }
        pos = mark;
    }

    /** Record that {@code token} was expected at the current position. */
    void expect(String token) {
        if (pos > farthestPos) {
            farthestPos = pos;
            farthestExpected.clear();
        }
        if (pos == farthestPos && !farthestExpected.contains(token)) {
            farthestExpected.add(token);
        }
    }

    /** Build a parse exception with current positional context. */
    EdtfParseException error(String expected) {
        expect(expected);
        return new EdtfParseException(
            "expected " + expected + " at position " + pos,
            input,
            pos,
            List.of(expected)
        );
    }

    /** Build a parse exception using the farthest-reached position. */
    EdtfParseException farthestError() {
        String msg = farthestExpected.isEmpty()
            ? "parse failed at position " + farthestPos
            : "expected " + String.join(" or ", farthestExpected)
              + " at position " + farthestPos;
        return new EdtfParseException(msg, input, farthestPos, farthestExpected);
    }
}
