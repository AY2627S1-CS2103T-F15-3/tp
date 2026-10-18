package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UnfavCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed person index for an unfavourite command.
 */
public class UnfavCommandParser implements Parser<UnfavCommand> {
    @Override
    public UnfavCommand parse(String args) throws ParseException {
        try {
            return new UnfavCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnfavCommand.MESSAGE_USAGE), e);
        }
    }
}
