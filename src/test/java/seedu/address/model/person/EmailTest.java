package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "invalid-email";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertTrue(Email.isValidEmail("")); // unspecified email
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertFalse(Email.isValidEmail("-peterjack@example.com")); // local part starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack-@example.com")); // local part ends with a hyphen
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // local part has two consecutive periods
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.c")); // top level domain has less than two chars

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
    }

    @Test
    public void consecutiveSpecialCharacters_rejectedWithConciseExplanation() {
        for (char first : "+_.-".toCharArray()) {
            for (char second : "+_.-".toCharArray()) {
                assertFalse(Email.isValidEmail("alice" + first + second + "cs2103t@example.com"));
            }
        }
        assertTrue(Email.isValidEmail("alice+cs2103t@example.com"));
        assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSTRAINTS, () -> {
            new Email("alice++cs2103t@example.com");
        });
        assertTrue(Email.MESSAGE_CONSTRAINTS.contains("cannot be consecutive"));
        assertTrue(Email.MESSAGE_CONSTRAINTS.lines().allMatch(line -> line.length() <= 75));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }

    @Test
    public void isValidEmail_longParts_validatedWithoutStackOverflow() {
        for (String email : new String[]{"a.".repeat(5000) + "a@example.com",
            "a@" + "a.".repeat(5000) + "com", "a@" + "a-".repeat(5000) + "bc",
            "a".repeat(20000) + "@example.com"}) {
            assertTrue(Email.isValidEmail(email));
            assertEquals(email, new Email(email).value);
        }
    }

    @Test
    public void isValidEmail_longInvalidParts_rejectedWithoutStackOverflow() {
        for (String email : new String[]{"a.".repeat(5000) + "@example.com",
            "a@" + "a.".repeat(5000), "a@" + "a-".repeat(5000) + "a",
            "a".repeat(20000) + "!@example.com", "a@" + "a-".repeat(5000) + "bc!"}) {
            assertFalse(Email.isValidEmail(email));
            assertThrows(IllegalArgumentException.class, () -> new Email(email));
        }
    }

    @Test
    public void isValidEmail_shortInputs_preserveExistingRegexRules() {
        String[] parts = {"", "a", "bc", "1", "A0", "a-b", "ab-c", "a-bc", "a-b-c", "a..b",
            "a.b", "a+b", "a_b", "-a", "a-", "a b", "é", "中", "a\n", "a\r", "a\u00a0"};
        for (String local : parts) {
            for (String domain : parts) {
                for (String suffix : new String[]{"", ".com", ".a-b", ".ab-c"}) {
                    String email = local + "@" + domain + suffix;
                    assertEquals(email.matches(Email.VALIDATION_REGEX), Email.isValidEmail(email), email);
                }
            }
        }
    }
}
