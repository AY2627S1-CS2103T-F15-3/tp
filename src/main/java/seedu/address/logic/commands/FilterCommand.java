package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.ContactFilterPredicate;

/**
 * Lists contacts matching every supplied major, graduation-year and tag criterion.
 */
public class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists contacts matching all supplied criteria. "
            + "Major and tag values match in full, ignoring case; years match exactly.\n"
            + "Parameters: [m/MAJOR] [c/CLASS_YEAR] [t/TAG] (at least one; each prefix at most once)\n"
            + "Example: " + COMMAND_WORD + " m/Computer Science c/2027 t/Classmate";

    private final ContactFilterPredicate predicate;

    /**
     * Creates a command that replaces the current search or filter with {@code predicate}.
     */
    public FilterCommand(ContactFilterPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW,
                model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FilterCommand otherCommand
                && predicate.equals(otherCommand.predicate));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
