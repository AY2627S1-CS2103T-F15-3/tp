package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Shows only favourite persons in the address book.
 */
public class ListFavCommand extends Command {
    public static final String COMMAND_WORD = "listfav";
    public static final String MESSAGE_USAGE = "listfav: Lists favourite contacts.\nUsage: listfav";
    public static final String MESSAGE_SUCCESS = "Listed %1$d favourite contact(s).";
    public static final String MESSAGE_EMPTY = "No favourite contacts found.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(Person::isFavourite);
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_EMPTY : String.format(MESSAGE_SUCCESS, count));
    }
}
