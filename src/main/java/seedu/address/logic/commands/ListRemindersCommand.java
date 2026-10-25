package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.ReminderPredicate;
import seedu.address.model.tag.SystemTag;

/**
 * Lists contacts with one or either automatic reminder, independently of custom tags.
 */
public class ListRemindersCommand extends Command {
    public static final String COMMAND_WORD = "listreminders";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists contacts with automatic reminders.\n"
            + "Parameters: [update|incomplete] (at most one; case-insensitive)\n"
            + "Omit the parameter to show contacts with either reminder. Example: " + COMMAND_WORD + " incomplete";
    public static final String MESSAGE_SUCCESS = "Listed %1$d contact(s) with matching automatic reminders.";
    public static final String MESSAGE_EMPTY = "No contacts with matching automatic reminders found.";

    private final Set<SystemTag> reminders;
    private final Clock clock;

    /**
     * Creates a command matching either automatic reminder using the computer's local date.
     */
    public ListRemindersCommand() {
        this(Set.of(SystemTag.UPDATE, SystemTag.INCOMPLETE));
    }

    /**
     * Creates a command matching any of the supplied automatic reminders.
     */
    public ListRemindersCommand(Set<SystemTag> reminders) {
        this(reminders, Clock.systemDefaultZone());
    }

    /**
     * Creates a command that evaluates reminders on the supplied clock's date when executed.
     */
    public ListRemindersCommand(Set<SystemTag> reminders, Clock clock) {
        this.reminders = Set.copyOf(requireNonNull(reminders));
        checkArgument(!this.reminders.isEmpty(), "At least one automatic reminder must be selected.");
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(new ReminderPredicate(reminders, LocalDate.now(clock)));
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_EMPTY : String.format(MESSAGE_SUCCESS, count));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ListRemindersCommand otherCommand
                && reminders.equals(otherCommand.reminders));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("reminders", reminders).toString();
    }
}
