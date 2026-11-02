package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {
    public static final Email EMPTY = new Email("");

    public static final String MESSAGE_CONSTRAINTS = "Email format: local-part@domain\n"
            + "Local part: letters, digits and + _ . - only.\n"
            + "Special characters cannot be consecutive or at either end.\n"
            + "Domain: labels separated by dots; letters, digits and hyphens only.\n"
            + "Each label must start and end with a letter or digit.\n"
            + "The final label must have at least 2 characters.";
    private static final String SPECIAL_CHARACTERS = "+_.-";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(" + DOMAIN_PART_REGEX + "){2,}$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a string is empty (unspecified) or a valid email.
     */
    public static boolean isValidEmail(String test) {
        if (test.isEmpty()) {
            return true;
        }
        int at = test.indexOf('@');
        if (at < 0 || at != test.lastIndexOf('@') || !isValidPart(test, 0, at, SPECIAL_CHARACTERS)) {
            return false;
        }
        int labelStart = at + 1;
        for (int i = labelStart; i < test.length(); i++) {
            if (test.charAt(i) == '.') {
                if (!isValidPart(test, labelStart, i, "-")) {
                    return false;
                }
                labelStart = i + 1;
            }
        }
        if (!isValidPart(test, labelStart, test.length(), "-")) {
            return false;
        }
        // Preserve the existing final-label rule: two adjacent alphanumeric characters are required.
        // This is equivalent to the repeated DOMAIN_PART_REGEX, including its hyphen restrictions.
        for (int i = labelStart + 1; i < test.length(); i++) {
            if (isAsciiAlphanumeric(test.charAt(i - 1)) && isAsciiAlphanumeric(test.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks a non-empty part without recursive regex matching or allocating substrings.
     * Separators must occur singly between ASCII letters or digits, as in the original regex.
     */
    private static boolean isValidPart(String text, int start, int end, String separators) {
        boolean previousAlphanumeric = false;
        for (int i = start; i < end; i++) {
            char character = text.charAt(i);
            if (isAsciiAlphanumeric(character)) {
                previousAlphanumeric = true;
            } else if (separators.indexOf(character) >= 0 && previousAlphanumeric) {
                previousAlphanumeric = false;
            } else {
                return false;
            }
        }
        return previousAlphanumeric;
    }

    private static boolean isAsciiAlphanumeric(char character) {
        return character >= 'a' && character <= 'z'
                || character >= 'A' && character <= 'Z'
                || character >= '0' && character <= '9';
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
