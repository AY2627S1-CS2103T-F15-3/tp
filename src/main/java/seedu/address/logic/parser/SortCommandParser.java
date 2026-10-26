package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortResetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;

/**
 * Parses a supported sort field and direction, or a request to restore saved order.
 */
public class SortCommandParser implements Parser<Command> {
    private static final Logger logger = LogsCenter.getLogger(SortCommandParser.class);

    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        String[] tokens = args.strip().split("\\s+");
        if (tokens.length == 1 && tokens[0].equalsIgnoreCase("reset")) {
            return new SortResetCommand();
        }
        if (tokens.length != 2) {
            throw invalidFormat("Expected one field and one direction");
        }
        SortField field = switch (tokens[0].toLowerCase(Locale.ROOT)) {
            case "name" -> SortField.NAME;
            case "major" -> SortField.MAJOR;
            case "year" -> SortField.YEAR;
            default -> throw invalidFormat("Unsupported sort field");
        };
        SortDirection direction = switch (tokens[1].toLowerCase(Locale.ROOT)) {
            case "asc" -> SortDirection.ASC;
            case "desc" -> SortDirection.DESC;
            default -> throw invalidFormat("Unsupported sort direction");
        };
        return new SortCommand(field, direction);
    }

    private static ParseException invalidFormat(String reason) {
        logger.fine("Sort arguments rejected: " + reason);
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
    }
}
