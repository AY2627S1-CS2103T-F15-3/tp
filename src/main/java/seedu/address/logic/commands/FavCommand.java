package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Marks a person in the displayed list as a favourite.
 */
public class FavCommand extends Command {
    public static final String COMMAND_WORD = "fav";
    public static final String MESSAGE_USAGE = "fav: Marks the person at the displayed index as a favourite.\n"
            + "Parameters: INDEX (must be a positive integer)\nExample: fav 1";
    public static final String MESSAGE_SUCCESS = "Added to favourites: %1$s";
    public static final String MESSAGE_ALREADY_FAVOURITE = "This person is already a favourite.";

    private final Index index;

    /**
     * Creates a command for the given displayed person index.
     */
    public FavCommand(Index index) {
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
        if (person.isFavourite()) {
            throw new CommandException(MESSAGE_ALREADY_FAVOURITE);
        }
        Person favourite = new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getMajor(), person.getGraduationYear(), person.getTags(), true);
        model.setPerson(person, favourite);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(favourite)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FavCommand otherCommand && index.equals(otherCommand.index));
    }
}
