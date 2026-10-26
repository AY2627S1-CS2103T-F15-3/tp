package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * Deletes one or more contacts identified using their original displayed indices from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes contacts identified by their index numbers in the displayed contact list.\n"
            + "Parameters: INDEX [MORE_INDICES] (distinct positive integers separated by spaces)\n"
            + "Example: " + COMMAND_WORD + " 1 3 5";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_DELETE_PERSONS_SUCCESS = "Deleted %1$d contacts:\n%2$s";
    public static final String MESSAGE_DUPLICATE_INDEX = "Index %1$d is repeated. "
            + "Specify each displayed index only once. No contacts were deleted.";
    public static final String MESSAGE_INVALID_INDEX = "Index %1$d is outside the current displayed list. "
            + "Choose an index from 1 to %2$d.";
    public static final String MESSAGE_INVALID_INDEX_SINGLE_PERSON = "Index %1$d is outside "
            + "the current displayed list. The only valid index is 1.";
    public static final String MESSAGE_NO_CONTACTS = "There are no saved contacts to delete. "
            + "Add a contact before using delete.";
    public static final String MESSAGE_NO_DISPLAYED_CONTACTS = "No contacts are currently displayed to delete. "
            + "Use list to view all saved contacts.";

    private final List<Index> targetIndices;

    /**
     * Creates a command deleting the given displayed indices, preserving their input order.
     * At least one non-null index must be supplied.
     */
    public DeleteCommand(Index... targetIndices) {
        requireNonNull(targetIndices);
        checkArgument(targetIndices.length > 0, "At least one target index is required.");
        this.targetIndices = List.of(targetIndices);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = List.copyOf(model.getFilteredPersonList());

        if (lastShownList.isEmpty()) {
            throw new CommandException(model.getAddressBook().getPersonList().isEmpty()
                    ? MESSAGE_NO_CONTACTS : MESSAGE_NO_DISPLAYED_CONTACTS);
        }

        Set<Integer> seenIndices = new HashSet<>();
        List<Person> personsToDelete = new ArrayList<>();
        for (Index targetIndex : targetIndices) {
            if (!seenIndices.add(targetIndex.getOneBased())) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_INDEX, targetIndex.getOneBased()));
            }
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                if (lastShownList.size() == 1) {
                    throw new CommandException(String.format(MESSAGE_INVALID_INDEX_SINGLE_PERSON,
                            targetIndex.getOneBased()));
                }
                throw new CommandException(String.format(MESSAGE_INVALID_INDEX,
                        targetIndex.getOneBased(), lastShownList.size()));
            }
            personsToDelete.add(lastShownList.get(targetIndex.getZeroBased()));
        }

        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        personsToDelete.forEach(model::deletePerson);

        String message;
        if (personsToDelete.size() == 1) {
            message = String.format(MESSAGE_DELETE_PERSON_SUCCESS,
                    Messages.format(personsToDelete.get(0)));
        } else {
            String deletedPersons = IntStream.range(0, personsToDelete.size())
                    .mapToObj(i -> targetIndices.get(i).getOneBased() + ". "
                            + Messages.format(personsToDelete.get(i)))
                    .collect(Collectors.joining("\n"));
            message = String.format(MESSAGE_DELETE_PERSONS_SUCCESS,
                    personsToDelete.size(), deletedPersons);
        }

        model.commitAddressBook(previousState, message);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndices.equals(otherDeleteCommand.targetIndices);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndices", targetIndices)
                .toString();
    }
}
