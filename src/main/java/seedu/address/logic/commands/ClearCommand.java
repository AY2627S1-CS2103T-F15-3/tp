package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Clears all saved contacts. "
            + "No parameters are accepted.\nUsage: " + COMMAND_WORD;
    public static final String MESSAGE_CLEARED_DESCRIPTION = "All contacts have been cleared.";
    public static final String MESSAGE_SUCCESS = MESSAGE_CLEARED_DESCRIPTION + " Use undo to restore them.";
    public static final String MESSAGE_ALREADY_EMPTY = "There are no saved contacts to clear.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        model.setAddressBook(new AddressBook());
        String message = previousState.getPersonList().isEmpty() ? MESSAGE_ALREADY_EMPTY : MESSAGE_SUCCESS;
        model.commitAddressBook(previousState, MESSAGE_CLEARED_DESCRIPTION);
        return new CommandResult(message);
    }
}
