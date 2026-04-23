package io.github.openhistoricalmap.edtf;

import java.util.List;
import java.util.Objects;

/**
 * Thrown when an input cannot be parsed as a valid EDTF string, or when
 * it parses only at a level or type the caller&apos;s
 * {@link ParseOptions} forbid.
 *
 * <p>The exception carries positional context so callers can render
 * helpful error messages: the original input, the farthest byte offset
 * the parser reached before failing, and a list of token kinds that
 * would have been accepted there.
 */
public class EdtfParseException extends RuntimeException {

    private final String input;
    private final int position;
    private final List<String> expected;

    public EdtfParseException(String message, String input, int position, List<String> expected) {
        super(message);
        this.input = input;
        this.position = position;
        this.expected = expected == null ? List.of() : List.copyOf(expected);
    }

    public EdtfParseException(String message, String input, int position) {
        this(message, input, position, List.of());
    }

    public EdtfParseException(String message, String input) {
        this(message, input, -1, List.of());
    }

    /** The input string that failed to parse. */
    public String input() {
        return input;
    }

    /** Byte offset at which parsing failed, or {@code -1} if unavailable. */
    public int position() {
        return position;
    }

    /** Token kinds that would have been accepted at {@link #position()}, if known. */
    public List<String> expected() {
        return expected;
    }

    /**
     * Builds a multi-line message pinpointing the failure with a caret.
     * Useful for error logs and test assertions; this is not what
     * {@code getMessage()} returns.
     */
    public String formatted() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        if (input != null && position >= 0 && position <= input.length()) {
            sb.append(System.lineSeparator()).append(input);
            sb.append(System.lineSeparator());
            sb.append(" ".repeat(Math.max(0, position))).append('^');
        }
        if (!expected.isEmpty()) {
            sb.append(System.lineSeparator());
            sb.append("expected: ").append(String.join(", ", expected));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdtfParseException other)) return false;
        return position == other.position
            && Objects.equals(input, other.input)
            && Objects.equals(getMessage(), other.getMessage())
            && Objects.equals(expected, other.expected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, position, getMessage(), expected);
    }
}
