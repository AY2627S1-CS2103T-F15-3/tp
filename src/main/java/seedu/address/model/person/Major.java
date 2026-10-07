package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's optional academic major in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidMajor(String)}.
 * An empty value represents an unspecified field.
 */
public class Major {

    public static final String MESSAGE_CONSTRAINTS = "Major must be a non-blank, single line of text when supplied.";

    /*
     * A supplied major must contain a non-whitespace character and no control characters.
     * Spaces and punctuation are allowed, as in "Philosophy, Politics & Economics".
     */
    public static final String VALIDATION_REGEX = "(?=.*[^\\p{javaWhitespace}])[^\\p{javaISOControl}]+";

    public static final Major EMPTY = new Major("");

    public final String value;

    /**
     * Constructs a {@code Major}.
     *
     * @param major A valid academic major, or an empty string if unspecified.
     */
    public Major(String major) {
        requireNonNull(major);
        checkArgument(isValidMajor(major), MESSAGE_CONSTRAINTS);
        value = major;
    }

    /**
     * Returns true if a given string is a valid academic major, or empty (unspecified).
     */
    public static boolean isValidMajor(String test) {
        return test.isEmpty() || test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Major otherMajor)) {
            return false;
        }

        return value.equals(otherMajor.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
