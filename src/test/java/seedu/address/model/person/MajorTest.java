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

        // valid major
        assertTrue(Major.isValidMajor("")); // unspecified optional field
        assertTrue(Major.isValidMajor("Computer Science")); // words separated by spaces
        assertTrue(Major.isValidMajor("Philosophy, Politics & Economics")); // punctuation
        assertTrue(Major.isValidMajor("Computing (Information Systems)")); // parentheses
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
