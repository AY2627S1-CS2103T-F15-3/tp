package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;

/**
 * Removes a contact's note while preserving the contact and its other details.
 */
public class DeleteNoteCommand extends Command {

    public static final String COMMAND_WORD = "delnote";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes the note of a contact.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 2";
    public static final String MESSAGE_SUCCESS = "Note deleted for %1$s.";
    public static final String MESSAGE_NO_NOTES = "This contact has no note to delete.";

    private final Index index;

    /**
     * Creates a command to delete the note of the contact at {@code index}.
     */
    public DeleteNoteCommand(Index index) {
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
        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        model.setPerson(person, person.withNotes(Notes.EMPTY));
        model.commitAddressBook(previousState);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof DeleteNoteCommand otherCommand && index.equals(otherCommand.index));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).toString();
    }
}
