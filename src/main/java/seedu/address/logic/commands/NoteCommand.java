package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

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
 * Adds or overwrites the note of a contact in the displayed list.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds or overwrites a contact's note.\n"
            + "Parameters: INDEX (must be a positive integer) TEXT\n"
            + "Example: " + COMMAND_WORD + " 2 Met during CS2103T";
    public static final String MESSAGE_SUCCESS = "Note saved for %1$s.\nSaved note: %2$s";
    public static final String MESSAGE_REPLACED = "Note replaced for %1$s. Use undo to restore the previous note."
            + "\nPrevious note: %2$s\nSaved note: %3$s";
    public static final String MESSAGE_UNCHANGED = "Note unchanged for %1$s.\nSaved note: %2$s";

    private final Index index;
    private final Notes notes;

    /**
     * Creates a command to set a non-empty note for the contact at {@code index}.
     */
    public NoteCommand(Index index, Notes notes) {
        requireAllNonNull(index, notes);
        checkArgument(!notes.isEmpty(), "Notes must not be empty. Use delnote to remove a note.");
        this.index = index;
        this.notes = notes;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayedPersons.get(index.getZeroBased());
        Notes previousNotes = person.getNotes();
        if (previousNotes.equals(notes)) {
            return new CommandResult(String.format(MESSAGE_UNCHANGED, person.getName(), notes.value));
        }

        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        model.setPerson(person, person.withNotes(notes));
        String message = previousNotes.isEmpty()
                ? String.format(MESSAGE_SUCCESS, person.getName(), notes.value)
                : String.format(MESSAGE_REPLACED, person.getName(), previousNotes.value, notes.value);
        model.commitAddressBook(previousState, message);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof NoteCommand otherCommand
                && index.equals(otherCommand.index) && notes.equals(otherCommand.notes));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("notes", notes).toString();
    }
}
