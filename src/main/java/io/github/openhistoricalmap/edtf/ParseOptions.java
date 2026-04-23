package io.github.openhistoricalmap.edtf;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Constraints that narrow what {@link Edtf#parse(String, ParseOptions)}
 * will accept. Mirrors the {@code defaults.js} object from the upstream
 * edtf.js library.
 *
 * @param maxLevel            the highest {@link EdtfLevel} accepted. Inputs that
 *                            parse successfully only at a stricter level will
 *                            be rejected. Defaults to {@link EdtfLevel#L2}.
 * @param allowedTypes        when non-empty, restricts the result to one of
 *                            the listed {@link EdtfType}s. An empty set means
 *                            &quot;all types allowed&quot;. Defaults to empty.
 * @param seasonIntervals     if true, Level-1 season values may appear as
 *                            interval endpoints (an experimental Level-3
 *                            feature). Defaults to {@code false}.
 * @param seasonUncertainty   if true, season values may carry uncertainty /
 *                            approximate markers (an experimental Level-3
 *                            feature). Defaults to {@code false}.
 */
public record ParseOptions(
    EdtfLevel maxLevel,
    Set<EdtfType> allowedTypes,
    boolean seasonIntervals,
    boolean seasonUncertainty
) {

    /** Default options: L2 cap, all types allowed, no experimental season flags. */
    public static final ParseOptions DEFAULT = new ParseOptions(
        EdtfLevel.L2,
        Collections.emptySet(),
        false,
        false
    );

    /** Canonical constructor; defensively copies {@code allowedTypes}. */
    public ParseOptions {
        Objects.requireNonNull(maxLevel, "maxLevel");
        Objects.requireNonNull(allowedTypes, "allowedTypes");
        allowedTypes = allowedTypes.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(allowedTypes));
    }

    /** Returns a copy with {@code maxLevel} replaced. */
    public ParseOptions withMaxLevel(EdtfLevel newMaxLevel) {
        return new ParseOptions(newMaxLevel, allowedTypes, seasonIntervals, seasonUncertainty);
    }

    /** Returns a copy with {@code allowedTypes} replaced. */
    public ParseOptions withAllowedTypes(Set<EdtfType> newAllowedTypes) {
        return new ParseOptions(maxLevel, newAllowedTypes, seasonIntervals, seasonUncertainty);
    }

    /** Returns a copy with the {@code seasonIntervals} flag replaced. */
    public ParseOptions withSeasonIntervals(boolean v) {
        return new ParseOptions(maxLevel, allowedTypes, v, seasonUncertainty);
    }

    /** Returns a copy with the {@code seasonUncertainty} flag replaced. */
    public ParseOptions withSeasonUncertainty(boolean v) {
        return new ParseOptions(maxLevel, allowedTypes, seasonIntervals, v);
    }

    /** True when {@code type} is permitted by {@link #allowedTypes}. */
    public boolean permits(EdtfType type) {
        return allowedTypes.isEmpty() || allowedTypes.contains(type);
    }

    /** True when {@code level} is within the {@link #maxLevel} cap. */
    public boolean permits(EdtfLevel level) {
        return maxLevel.includes(level);
    }
}
