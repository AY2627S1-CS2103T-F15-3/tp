package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.FavCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed person index for a favourite command.
 */
public class FavCommandParser implements Parser<FavCommand> {
    @Override
    public FavCommand parse(String args) throws ParseException {
        try {
            return new FavCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FavCommand.MESSAGE_USAGE), e);
        }
    }
}
