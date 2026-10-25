package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Notes;

/**
 * Parses a displayed index followed by free-form note text.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // Note arguments are not normalised by AddressBookParser, so split on Unicode whitespace here.
        String[] parts = args.strip().split("\\p{javaWhitespace}+", 2);
        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE), e);
        }

        String text = parts[1].strip();
        if (!Notes.isValidNotes(text)) {
            throw new ParseException(Notes.MESSAGE_CONSTRAINTS);
        }
        return new NoteCommand(index, new Notes(text));
    }
}
