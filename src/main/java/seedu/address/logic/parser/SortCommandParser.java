package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;

/**
 * Parses exactly one supported sort field and one explicit sort direction.
 */
public class SortCommandParser implements Parser<SortCommand> {
    @Override
    public SortCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] tokens = args.strip().split("\\s+");
        if (tokens.length != 2) {
            throw invalidFormat();
        }
        SortField field = switch (tokens[0]) {
            case "name" -> SortField.NAME;
            case "major" -> SortField.MAJOR;
            case "year" -> SortField.YEAR;
            default -> throw invalidFormat();
        };
        SortDirection direction = switch (tokens[1]) {
            case "asc" -> SortDirection.ASC;
            case "desc" -> SortDirection.DESC;
            default -> throw invalidFormat();
        };
        return new SortCommand(field, direction);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
    }
}
