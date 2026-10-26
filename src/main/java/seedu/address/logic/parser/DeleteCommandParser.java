package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses one or more whitespace-separated displayed indices and returns a DeleteCommand for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        try {
            String[] tokens = StringUtil.stripWhitespace(args).split(StringUtil.WHITESPACE_REGEX + "+");
            Index[] indices = new Index[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                indices[i] = ParserUtil.parseIndex(tokens[i]);
            }
            return new DeleteCommand(indices);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

}
