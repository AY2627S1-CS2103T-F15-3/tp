package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;
import java.util.Set;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ListRemindersCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.SystemTag;

/**
 * Parses zero or one automatic-reminder criterion.
 */
public class ListRemindersCommandParser implements Parser<ListRemindersCommand> {
    @Override
    public ListRemindersCommand parse(String args) throws ParseException {
        String criterion = StringUtil.normalizeWhitespace(requireNonNull(args)).trim().toLowerCase(Locale.ROOT);
        return switch (criterion) {
            case "" -> new ListRemindersCommand();
            case "update" -> new ListRemindersCommand(Set.of(SystemTag.UPDATE));
            case "incomplete" -> new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE));
            default -> throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListRemindersCommand.MESSAGE_USAGE));
        };
    }
}
