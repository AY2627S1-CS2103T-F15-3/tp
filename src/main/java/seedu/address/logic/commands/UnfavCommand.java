package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Removes favourite status from a person in the displayed list.
 */
public class UnfavCommand extends Command {
    public static final String COMMAND_WORD = "unfav";
    public static final String MESSAGE_USAGE = "unfav: Removes favourite status "
            + "from the person at the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer)\nExample: unfav 1";
    public static final String MESSAGE_SUCCESS = "Removed from favourites: %1$s";
    public static final String MESSAGE_NOT_FAVOURITE = "This person is not a favourite.";

    private final Index index;

    /**
     * Creates a command for the given displayed person index.
     */
    public UnfavCommand(Index index) {
        this.index = requireNonNull(index);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayed.get(index.getZeroBased());
        if (!person.isFavourite()) {
            throw new CommandException(MESSAGE_NOT_FAVOURITE);
        }
        Person updatedPerson = new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getMajor(), person.getGraduationYear(), person.getTags(), person.getNotes(), false);
        model.setPerson(person, updatedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof UnfavCommand otherCommand && index.equals(otherCommand.index));
    }
}
