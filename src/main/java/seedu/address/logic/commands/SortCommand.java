package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonSortComparator;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;

/**
 * Sorts the displayed contacts without changing the active filter or contact data.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts contacts while preserving the current filter.\n"
            + "Parameters: FIELD DIRECTION (name, major or year; asc or desc)\n"
            + "Unspecified academic values stay last. Example: " + COMMAND_WORD + " year desc";
    public static final String MESSAGE_SUCCESS = "Sorted contacts by %s (%s).";

    private final SortField field;
    private final SortDirection direction;

    /**
     * Creates a command sorting by {@code field} in {@code direction}.
     */
    public SortCommand(SortField field, SortDirection direction) {
        this.field = requireNonNull(field);
        this.direction = requireNonNull(direction);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateSortedPersonList(new PersonSortComparator(field, direction));
        return new CommandResult(String.format(MESSAGE_SUCCESS, field.name().toLowerCase(Locale.ROOT),
                direction.name().toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof SortCommand otherCommand
                && field == otherCommand.field && direction == otherCommand.direction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("field", field).add("direction", direction).toString();
    }
}
