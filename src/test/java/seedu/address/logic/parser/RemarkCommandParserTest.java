package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_remark_returnsCommand() {
        assertParseSuccess(parser, " 1 r/Met at orientation ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Met at orientation")));
    }

    @Test
    public void parse_emptyOrOmittedRemark_returnsClearCommand() {
        RemarkCommand clear = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", clear);
        assertParseSuccess(parser, "1", clear);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String input : new String[] {"", "0 r/note", "-1 r/note", "abc r/note", "1.5 r/note"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_duplicateRemark_throwsParseException() {
        assertParseFailure(parser, "1 r/first r/second", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
