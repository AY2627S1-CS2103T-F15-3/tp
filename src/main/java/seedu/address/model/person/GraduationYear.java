package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's optional graduation year in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidGraduationYear(String)}.
 * An empty value represents an unspecified field.
 */
public class GraduationYear {

    public static final String MESSAGE_CONSTRAINTS = "Class year must be four digits from 2000 to 2099 when supplied.";

    /*
     * Class of 20XX is represented by exactly four digits from 2000 to 2099.
     */
    public static final String VALIDATION_REGEX = "20[0-9]{2}";

    public static final GraduationYear EMPTY = new GraduationYear("");

    public final String value;

    /**
     * Constructs a {@code GraduationYear}.
     *
     * @param graduationYear A valid graduation year, or an empty string if unspecified.
     */
    public GraduationYear(String graduationYear) {
        requireNonNull(graduationYear);
        checkArgument(isValidGraduationYear(graduationYear), MESSAGE_CONSTRAINTS);
        value = graduationYear;
    }

    /**
     * Returns true if a given string is a valid graduation year, or empty (unspecified).
     */
    public static boolean isValidGraduationYear(String test) {
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
        if (!(other instanceof GraduationYear otherGraduationYear)) {
            return false;
        }

        return value.equals(otherGraduationYear.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
