package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NotesTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Notes(null));
        assertThrows(NullPointerException.class, () -> Notes.isValidNotes(null));
    }

    @Test
    public void constructor_invalidNotes_throwsIllegalArgumentException() {
        for (String text : new String[]{" ", "\u2003", "\u00a0", "First\nSecond", "First\rSecond",
            "First\tSecond", "First\u0085Second", "First\u2028Second", "First\u2029Second",
            "a".repeat(Notes.MAX_LENGTH + 1)}) {
            assertFalse(Notes.isValidNotes(text));
            assertThrows(IllegalArgumentException.class, Notes.MESSAGE_CONSTRAINTS, () -> new Notes(text));
        }
    }

    @Test
    public void constructor_validNotes_preservesText() {
        for (String text : new String[]{"", "Met in CS2103T; project partner!", "Discuss n/ and t/ prefixes",
            "  Keep  spacing  ", "\u674e\u540c\u5b66", "a".repeat(Notes.MAX_LENGTH),
            "\ud83d\udcdd".repeat(Notes.MAX_LENGTH)}) {
            assertTrue(Notes.isValidNotes(text));
            assertEquals(text, new Notes(text).value);
        }
        assertFalse(Notes.isValidNotes("\ud83d\udcdd".repeat(Notes.MAX_LENGTH + 1)));
    }

    @Test
    public void isEmpty_absentAndPresentNotes_returnsExpectedValue() {
        assertTrue(Notes.EMPTY.isEmpty());
        assertTrue(new Notes("").isEmpty());
        assertFalse(new Notes("Met in class").isEmpty());
    }

    @Test
    public void equals_comparesText() {
        Notes notes = new Notes("Met in class");
        assertTrue(notes.equals(notes));
        assertTrue(notes.equals(new Notes("Met in class")));
        assertFalse(notes.equals(new Notes("Project teammate")));
        assertFalse(notes.equals(Notes.EMPTY));
        assertFalse(notes.equals(null));
        assertFalse(notes.equals("Met in class"));
        assertEquals(notes.hashCode(), new Notes("Met in class").hashCode());
        assertEquals("Met in class", notes.toString());
    }
}
