package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed index of a contact whose note should be deleted.
 */
public class DeleteNoteCommandParser implements Parser<DeleteNoteCommand> {

    @Override
    public DeleteNoteCommand parse(String args) throws ParseException {
        try {
            return new DeleteNoteCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE), e);
        }
    }
}
