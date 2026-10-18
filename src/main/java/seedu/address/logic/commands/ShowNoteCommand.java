package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays a contact's note in the command feedback area.
 */
public class ShowNoteCommand extends Command {

    public static final String COMMAND_WORD = "shownote";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Displays the note of a contact.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 2";
    public static final String MESSAGE_SUCCESS = "Note for %1$s:\n%2$s";
    public static final String MESSAGE_NO_NOTES = "No notes have been added to this contact.";

    private final Index index;

    /**
     * Creates a command to show the note of the contact at {@code index}.
     */
    public ShowNoteCommand(Index index) {
        this.index = requireNonNull(index);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayedPersons.get(index.getZeroBased());
        if (person.getNotes().isEmpty()) {
            return new CommandResult(MESSAGE_NO_NOTES);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), person.getNotes().value));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ShowNoteCommand otherCommand && index.equals(otherCommand.index));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).toString();
    }
}
