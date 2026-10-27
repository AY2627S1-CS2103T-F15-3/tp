package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Adds tags to a person in the displayed list without removing existing tags.
 */
public class TagCommand extends Command {
    public static final String COMMAND_WORD = "tag";
    public static final String MESSAGE_USAGE = "tag: Adds tags to the person at the displayed index. "
            + "Existing tags are preserved.\n"
            + "Parameters: INDEX [d/DEFAULT_INDEX]... [t/TAG]... (at least one tag is required)\n"
            + "Example: tag 1 d/1 t/NOC";
    public static final String MESSAGE_SUCCESS = "Tagged person: %1$s";
    public static final String MESSAGE_ALL_PRESENT = "No tags added: %1$s already has the tag(s): %2$s.";
    public static final String MESSAGE_SOME_PRESENT = "Already present, so not added again: %1$s.";

    private final Index index;
    private final Set<Tag> tags;

    /**
     * Creates a command to add the given tags to the person at {@code index}.
     */
    public TagCommand(Index index, Set<Tag> tags) {
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
        // Taken from the person's tags, so feedback shows the stored capitalization.
        Set<Tag> presentTags = new HashSet<>(person.getTags());
        presentTags.retainAll(tags);
        if (presentTags.size() == tags.size()) {
            return new CommandResult(String.format(MESSAGE_ALL_PRESENT, person.getName().fullName,
                    formatTagNames(presentTags)));
        }
        Set<Tag> updatedTags = new HashSet<>(person.getTags());
        updatedTags.addAll(tags);
        Person updatedPerson = new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), person.getMajor(), person.getGraduationYear(), updatedTags,
                person.getNotes(), person.isFavourite(), person.getLastUpdated());
        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        model.setPerson(person, updatedPerson);
        String message = String.format(MESSAGE_SUCCESS, Messages.format(updatedPerson));
        model.commitAddressBook(previousState, message);
        // The history description omits the already-present note, which only concerns this submission.
        return new CommandResult(presentTags.isEmpty() ? message
                : message + "\n" + String.format(MESSAGE_SOME_PRESENT, formatTagNames(presentTags)));
    }

    /** Returns the tag names sorted and separated by commas, for feedback messages. */
    static String formatTagNames(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof TagCommand otherCommand
                && index.equals(otherCommand.index) && tags.equals(otherCommand.tags));
    }
}
