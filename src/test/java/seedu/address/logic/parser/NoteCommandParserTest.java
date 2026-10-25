package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.person.Notes;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validArgs_preservesFreeFormText() {
        assertParseSuccess(parser, " 1  Met during CS2103T  ",
                new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met during CS2103T")));
        assertParseSuccess(parser, "1 Discuss n/Name, t/Friend and https://example.com; keep  spaces",
                new NoteCommand(INDEX_FIRST_PERSON,
                        new Notes("Discuss n/Name, t/Friend and https://example.com; keep  spaces")));
        assertParseSuccess(parser, "1 " + "a".repeat(Notes.MAX_LENGTH),
                new NoteCommand(INDEX_FIRST_PERSON, new Notes("a".repeat(Notes.MAX_LENGTH))));
    }

    @Test
    public void parse_unicodeSpaceSeparators_parsesIndexAndKeepsText() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met at CS2103T"));
        assertParseSuccess(parser, " 1 Met at CS2103T", expected);
        assertParseSuccess(parser, " 1 Met at CS2103T", expected);
        assertParseSuccess(parser, "　 1   Met at CS2103T",
                new NoteCommand(INDEX_FIRST_PERSON, new Notes("  Met at CS2103T")));
    }

    @Test
    public void parse_missingTextOrInvalidIndex_throwsParseException() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " ", "1", "1  ", "1 \u2003", "1\u00a0", "\u00a01\u00a0",
            "Text only", "0 Text", "-1 Text",
            "+1 Text", "1.5 Text", "2147483648 Text", "1Text"}) {
            assertParseFailure(parser, args, expected);
        }
    }

    @Test
    public void parse_whitespaceOnlyNotes_throwsParseException() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE);
        for (String whitespace : new String[]{" ", "\t", "\r\n", "\u2003", "\u3000", " \t\u2003\r\n "}) {
            assertParseFailure(parser, "1 " + whitespace, expected);
        }
    }

    @Test
    public void parse_invalidText_throwsParseException() {
        for (String text : new String[]{"\u00a0", "First\nSecond", "First\tSecond", "First\u2028Second",
            "a".repeat(Notes.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, "1 " + text, Notes.MESSAGE_CONSTRAINTS);
        }
    }
}
