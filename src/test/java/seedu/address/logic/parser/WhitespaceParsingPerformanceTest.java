package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Notes;
import seedu.address.model.person.PersonContainsKeywordsPredicate;

/** Checks responsiveness for long whitespace runs and preservation of command and note syntax. */
public class WhitespaceParsingPerformanceTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private static final String LONG_SPACES = " ".repeat(50_000);

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_longWhitespaceAfterCommand_returnsSameFindWithinTimeout() {
        FindCommand expected = new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice")));
        assertTimeoutPreemptively(TIMEOUT, () ->
                assertEquals(expected, parser.parseCommand("find " + LONG_SPACES + "Alice")));
    }

    @Test
    public void parseCommand_longWhitespaceBetweenKeywords_preservesBothKeywordsWithinTimeout() {
        FindCommand expected = new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertTimeoutPreemptively(TIMEOUT, () ->
                assertEquals(expected, parser.parseCommand("find Alice" + LONG_SPACES + "Bob")));
    }

    @Test
    public void parseCommand_longUnicodeWhitespace_preservesSearchWithinTimeout() {
        FindCommand expected = new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice", "Bob")));
        String input = "\u0001\u00a0find Alice" + "\u2003".repeat(50_000) + "Bob\u00a0\u0001";
        assertTimeoutPreemptively(TIMEOUT, () -> assertEquals(expected, parser.parseCommand(input)));
    }

    @Test
    public void stripWhitespace_longInternalRun_preservesTextWithinTimeout() {
        String text = "Alice" + LONG_SPACES + "Bob";
        assertTimeoutPreemptively(TIMEOUT, () -> assertEquals(text, StringUtil.stripWhitespace(text)));
    }

    @Test
    public void stripWhitespace_longBoundaryRuns_removedWithinTimeout() {
        String boundary = LONG_SPACES + "\u00a0\u2003";
        assertTimeoutPreemptively(TIMEOUT, () -> {
            assertEquals("Alice", StringUtil.stripWhitespace(boundary + "Alice" + boundary));
            assertEquals("", StringUtil.stripWhitespace(boundary));
        });
    }

    @Test
    public void stripWhitespace_supplementaryCharactersAndInternalSpaces_preserved() {
        String text = "\ud83d\ude42Alice\u00a0\u2003Bob\ud83d\ude42";
        assertEquals(text, StringUtil.stripWhitespace("\u3000" + text + "\u202f"));
        assertEquals(text, StringUtil.stripWhitespaceAndControls("\u0000\u3000" + text + "\u202f\u0001"));
        assertEquals("\u200bAlice\u200b", StringUtil.stripWhitespaceAndControls("\u200bAlice\u200b"));
    }

    @Test
    public void parseCommand_mixedControlAndUnicodeBoundaries_preservesLegacyTrimming() throws Exception {
        for (char control = 0; control <= 0x20; control++) {
            String boundary = control + "\u00a0" + control + "\u2003";
            assertTrue(parser.parseCommand(boundary + "list" + boundary) instanceof ListCommand);
        }
        assertEquals("", StringUtil.stripWhitespaceAndControls("\u0000\u00a0\u0001"));
        assertEquals("", StringUtil.stripWhitespaceAndControls(""));
        assertThrows(NullPointerException.class, () -> StringUtil.stripWhitespaceAndControls(null));
    }

    @Test
    public void parseCommand_noteWhitespace_keepsInternalTextAndRejectsControlCharacters() throws Exception {
        String text = "Met" + " ".repeat(50) + "at\u00a0the event\ud83d\ude42";
        assertEquals(new NoteCommand(Index.fromOneBased(1), new Notes(text)),
                parser.parseCommand("\u0001\u00a0NOTE 1 \u2003" + text + "\u202f\u0001"));
        assertEquals("\u0000Alice\u0001", StringUtil.stripWhitespace("\u0000Alice\u0001"));
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("note 1 \u0001Hello"));
        assertEquals(Notes.MESSAGE_CONSTRAINTS, error.getMessage());
    }
}
