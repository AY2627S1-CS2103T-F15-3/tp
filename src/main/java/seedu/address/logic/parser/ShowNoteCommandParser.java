package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ShowNoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed index of a contact whose note should be shown.
 */
public class ShowNoteCommandParser implements Parser<ShowNoteCommand> {

    @Override
    public ShowNoteCommand parse(String args) throws ParseException {
        try {
            return new ShowNoteCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ShowNoteCommand.MESSAGE_USAGE), e);
        }
    }
}
