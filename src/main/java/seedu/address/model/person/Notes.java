package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a person's optional notes. An empty value means no note has been added.
 * Guarantees: immutable; is valid as declared in {@link #isValidNotes(String)}.
 */
public class Notes {

    public static final int MAX_LENGTH = 500;
    public static final String MESSAGE_CONSTRAINTS = "Notes must be a non-blank, single line of text with at most "
            + MAX_LENGTH + " characters when supplied.";
    public static final String VALIDATION_REGEX =
            "(?=.*" + StringUtil.VISIBLE_CHARACTER_REGEX + ")[^\\p{javaISOControl}\\p{Zl}\\p{Zp}]+";
    public static final Notes EMPTY = new Notes("");

    public final String value;

    /**
     * Constructs valid notes, or an empty value if no note is present.
     */
    public Notes(String notes) {
        requireNonNull(notes);
        checkArgument(isValidNotes(notes), MESSAGE_CONSTRAINTS);
        value = notes;
    }

    /**
     * Returns true if the text is valid notes or empty. Length is measured in Unicode code points.
     */
    public static boolean isValidNotes(String test) {
        requireNonNull(test);
        return test.isEmpty() || (test.codePointCount(0, test.length()) <= MAX_LENGTH
                && test.matches(VALIDATION_REGEX));
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Notes otherNotes && value.equals(otherNotes.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
