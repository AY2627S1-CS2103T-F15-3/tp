package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
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
        // Trim Unicode whitespace at the boundaries while preserving whitespace inside the note.
        String trimmedArgs = StringUtil.stripWhitespace(args);
        String[] parts = trimmedArgs.split(StringUtil.WHITESPACE_REGEX, 2);
        if (parts.length != 2 || StringUtil.stripWhitespace(parts[1]).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE), e);
        }

        String text = StringUtil.stripWhitespace(parts[1]);
        if (!Notes.isValidNotes(text)) {
            throw new ParseException(Notes.MESSAGE_CONSTRAINTS);
        }
        return new NoteCommand(index, new Notes(text));
    }
}
