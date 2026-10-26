package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;

/**
 * Checks single and multiple displayed indices, whitespace separators and invalid batch syntax.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_multipleIndices_preservesInputOrder() {
        assertParseSuccess(parser, "1 3 5", new DeleteCommand(INDEX_FIRST_PERSON,
                Index.fromOneBased(3), Index.fromOneBased(5)));
        assertParseSuccess(parser, "5 1 3", new DeleteCommand(Index.fromOneBased(5),
                INDEX_FIRST_PERSON, Index.fromOneBased(3)));
    }

    @Test
    public void parse_multipleWhitespaceSeparators_returnsDeleteCommand() {
        DeleteCommand expected = new DeleteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), Index.fromOneBased(5));
        for (String args : new String[]{"  1   3  5  ", "\t1\t3\n5\r", "\u00A01\u20073\u202F5\u3000"}) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " ", "\u00A0", "a", "0", "-1", "+1", "1.5", "2147483648",
            "1 0", "1 -2", "1 +2", "1 2.5", "1 2147483648", "1 3 invalid", "1,3,5", "1, 3", "1-5", "i/1 i/3"}) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
