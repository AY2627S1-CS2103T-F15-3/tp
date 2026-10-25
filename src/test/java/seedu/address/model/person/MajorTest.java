package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class MajorTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Major(null));
    }

    @Test
    public void constructor_invalidMajor_throwsIllegalArgumentException() {
        String invalidMajor = " ";
        assertThrows(IllegalArgumentException.class, () -> new Major(invalidMajor));
        assertThrows(IllegalArgumentException.class, () -> new Major("Computer\nScience"));
    }

    @Test
    public void isValidMajor() {
        // null major
        assertThrows(NullPointerException.class, () -> Major.isValidMajor(null));

        // invalid major
        assertFalse(Major.isValidMajor("   ")); // spaces only
        assertFalse(Major.isValidMajor("Computer\tScience")); // tab character
        assertFalse(Major.isValidMajor("Computer\rScience")); // carriage return
        assertFalse(Major.isValidMajor("\u2003")); // Unicode whitespace only
        assertFalse(Major.isValidMajor("Computer\u0085Science")); // Unicode control character
        assertFalse(Major.isValidMajor(" ")); // no-break space only
        assertFalse(Major.isValidMajor("​")); // zero-width space only
        assertFalse(Major.isValidMajor("﻿")); // byte order mark only
        assertFalse(Major.isValidMajor("\u200d\u200c")); // zero width joiner and non-joiner only
        assertFalse(Major.isValidMajor("⁠​")); // several invisible characters
        assertFalse(Major.isValidMajor(" ​ ")); // Unicode spaces and invisible characters

        // valid major
        assertTrue(Major.isValidMajor("")); // unspecified optional field
        assertTrue(Major.isValidMajor("Computer Science")); // words separated by spaces
        assertTrue(Major.isValidMajor("Philosophy, Politics & Economics")); // punctuation
        assertTrue(Major.isValidMajor("Computing (Information Systems)")); // parentheses
        assertTrue(Major.isValidMajor("Computer​Science")); // invisible character within visible text
        assertTrue(Major.isValidMajor("Computer Science")); // Unicode space within visible text
        assertTrue(Major.isValidMajor("\u0645\u06cc\u200c\u0634\u0648\u062f")); // zero width non-joiner
        assertTrue(Major.isValidMajor("\u0915\u094d\u200d\u0937")); // zero width joiner
        assertTrue(Major.isValidMajor("\ud83d\udc68\u200d\ud83d\udcbb")); // emoji sequence
    }

    @Test
    public void equals() {
        Major major = new Major("Computer Science");

        // same values -> returns true
        assertTrue(major.equals(new Major("Computer Science")));

        // same object -> returns true
        assertTrue(major.equals(major));

        // null -> returns false
        assertFalse(major.equals(null));

        // different types -> returns false
        assertFalse(major.equals("Computer Science"));

        // different values -> returns false
        assertFalse(major.equals(new Major("Business")));

        // unspecified fields -> returns true
        assertTrue(Major.EMPTY.equals(new Major("")));
    }

    @Test
    public void hashCode_sameValue_returnsSameHashCode() {
        Major major = new Major("Computer Science");
        assertEquals(major.hashCode(), new Major("Computer Science").hashCode());
    }

    @Test
    public void toStringMethod() {
        Major major = new Major("Computer Science");
        assertEquals("Computer Science", major.toString());
    }
}
