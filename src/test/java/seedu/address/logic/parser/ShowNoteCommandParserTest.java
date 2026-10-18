package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ShowNoteCommand;

public class ShowNoteCommandParserTest {

    private final ShowNoteCommandParser parser = new ShowNoteCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, " 1 ", new ShowNoteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidIndexOrExtraText_throwsParseException() {
        for (String args : new String[]{"", " ", "0", "-1", "+1", "x", "1.5", "2147483648", "1 extra", "1 2"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ShowNoteCommand.MESSAGE_USAGE));
        }
    }
}
