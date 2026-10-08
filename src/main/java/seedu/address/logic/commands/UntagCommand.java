package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Removes specified tags from a person in the displayed list.
 */
public class UntagCommand extends Command {
    public static final String COMMAND_WORD = "untag";
    public static final String MESSAGE_USAGE = "untag: Removes tags from the person at the displayed index. "
            + "Other tags are preserved.\n"
            + "Parameters: INDEX t/TAG [t/TAG]...\n"
            + "Example: untag 1 t/NOC";
    public static final String MESSAGE_SUCCESS = "Untagged person: %1$s";

    private final Index index;
    private final Set<Tag> tags;

    /**
     * Creates a command to remove the given tags from the person at {@code index}.
     */
    public UntagCommand(Index index, Set<Tag> tags) {
        requireAllNonNull(index, tags);
        this.index = index;
        this.tags = Set.copyOf(tags);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayedPersons.get(index.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(person.getTags());
        updatedTags.removeAll(tags);
        Person updatedPerson = new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), person.getMajor(), person.getGraduationYear(), updatedTags);
        model.setPerson(person, updatedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof UntagCommand otherCommand
                && index.equals(otherCommand.index) && tags.equals(otherCommand.tags));
    }
}
