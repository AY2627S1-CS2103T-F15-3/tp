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
        for (String text : new String[]{" ", "\u2003", "\u00a0", "\u200b", "\ufeff", "\u2060\u200b\u00a0",
            "\u200d", "\u200c\u200d", "\ufe0f", "\u0301\ufe0f",
            "First\nSecond", "First\rSecond",
            "First\tSecond", "First\u0085Second", "First\u2028Second", "First\u2029Second",
            "a".repeat(Notes.MAX_LENGTH + 1)}) {
            assertFalse(Notes.isValidNotes(text));
            assertThrows(IllegalArgumentException.class, Notes.MESSAGE_CONSTRAINTS, () -> new Notes(text));
        }
    }

    @Test
    public void constructor_validNotes_preservesText() {
        for (String text : new String[]{"", "Met in CS2103T; project partner!", "Discuss n/ and t/ prefixes",
            "  Keep  spacing  ", "\u674e\u540c\u5b66", "Met\u200bin class", "a".repeat(Notes.MAX_LENGTH),
            "\ud83d\udcdd".repeat(Notes.MAX_LENGTH)}) {
            assertTrue(Notes.isValidNotes(text));
            assertEquals(text, new Notes(text).value);
        }
        assertFalse(Notes.isValidNotes("\ud83d\udcdd".repeat(Notes.MAX_LENGTH + 1)));
    }

    @Test
    public void constructor_formatCharactersWithinText_preservesText() {
        // Format characters (\p{Cf}) are rejected only when the notes have nothing visible.
        for (String text : new String[]{
            "\ud83d\udc68\u200d\ud83d\udc69\u200d\ud83d\udc67 family dinner", // emoji joined by zero width joiners
            "\ud83d\udc68\u200d\ud83d\udcbb", // emoji sequence only
            "\ud83c\uddf8\ud83c\uddec trip", // flag emoji
            "\u2764\ufe0f", // emoji with variation selector
            "1\ufe0f\u20e3", // keycap emoji
            "Caf\u0065\u0301", // combining accent after a letter
            "\u0645\u06cc\u200c\u062e\u0648\u0627\u0647\u0645", // Persian word with zero width non-joiner
            "\u0915\u094d\u200d\u0937"}) { // Devanagari conjunct with zero width joiner
            assertTrue(Notes.isValidNotes(text));
            assertEquals(text, new Notes(text).value);
        }
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
