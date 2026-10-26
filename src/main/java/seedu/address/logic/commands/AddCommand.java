package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEFAULT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADUATION_YEAR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAJOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a person to the address book. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + PREFIX_ADDRESS + "ADDRESS "
            + "[" + PREFIX_MAJOR + "MAJOR] "
            + "[" + PREFIX_GRADUATION_YEAR + "CLASS_YEAR] "
            + "[" + PREFIX_TAG + "TAG]... "
            + "[" + PREFIX_DEFAULT_TAG + "DEFAULT_INDEX]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_MAJOR + "Computer Science "
            + PREFIX_GRADUATION_YEAR + "2027 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "This person already exists in the address book (names are compared ignoring case).";
    public static final String MESSAGE_DUPLICATE_PERSON_DISPLAYED = MESSAGE_DUPLICATE_PERSON
            + "\nExisting contact at displayed index %1$d:\n%2$s";
    public static final String MESSAGE_DUPLICATE_PERSON_HIDDEN = MESSAGE_DUPLICATE_PERSON
            + "\nExisting contact is hidden by the current filter (saved index %1$d):\n%2$s"
            + "\nRun list, then sort reset to view this contact at index %1$d.";
    public static final String MESSAGE_SHARED_DETAILS_WARNING = "Warning: Shared contact details found. "
            + "The new contact was added; use undo if this was accidental.\n%1$s\n%2$s";
    public static final String MESSAGE_SHARED_DETAILS_MATCH =
            "Existing contact at displayed index %1$d (same %2$s):\n%3$s";

    private final Person toAdd;
    private final Clock clock;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        this(person, Clock.systemDefaultZone());
    }

    /**
     * Creates an add command using the supplied clock for the contact's initial update date.
     */
    public AddCommand(Person person, Clock clock) {
        toAdd = requireNonNull(person);
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(getDuplicateMessage(model));
        }

        ReadOnlyAddressBook previousState = new AddressBook(model.getAddressBook());
        Person addedPerson = toAdd.withLastUpdated(LocalDate.now(clock));
        model.addPerson(addedPerson);
        String message = String.format(MESSAGE_SUCCESS, Messages.format(addedPerson));
        String feedback = getAddFeedback(model, addedPerson, message);
        model.commitAddressBook(previousState, message);
        return new CommandResult(feedback);
    }

    /**
     * Warns about shared non-empty phone/email details using indices from the refreshed display.
     * Phone numbers match exactly; email matching ignores case for this advisory warning.
     */
    private String getAddFeedback(Model model, Person addedPerson, String successMessage) {
        List<String> matches = new ArrayList<>();
        List<Person> displayedPersons = model.getFilteredPersonList();
        for (int i = 0; i < displayedPersons.size(); i++) {
            Person existingPerson = displayedPersons.get(i);
            if (addedPerson.isSamePerson(existingPerson)) {
                continue;
            }
            List<String> sharedFields = new ArrayList<>();
            if (!addedPerson.getPhone().value.isEmpty() && addedPerson.getPhone().equals(existingPerson.getPhone())) {
                sharedFields.add("phone number");
            }
            if (!addedPerson.getEmail().value.isEmpty()
                    && addedPerson.getEmail().value.equalsIgnoreCase(existingPerson.getEmail().value)) {
                sharedFields.add("email address");
            }
            if (!sharedFields.isEmpty()) {
                matches.add(String.format(MESSAGE_SHARED_DETAILS_MATCH,
                        i + 1, String.join(" and ", sharedFields), Messages.format(existingPerson)));
            }
        }
        return matches.isEmpty() ? successMessage
                : String.format(MESSAGE_SHARED_DETAILS_WARNING, successMessage, String.join("\n", matches));
    }

    /**
     * Identifies the existing contact using its displayed index, or a labelled saved index if hidden.
     */
    private String getDuplicateMessage(Model model) {
        List<Person> savedPersons = model.getAddressBook().getPersonList();
        Person existingPerson = savedPersons.stream().filter(toAdd::isSamePerson).findFirst().orElseThrow();
        int displayedIndex = model.getFilteredPersonList().indexOf(existingPerson);
        if (displayedIndex >= 0) {
            return String.format(MESSAGE_DUPLICATE_PERSON_DISPLAYED,
                    displayedIndex + 1, Messages.format(existingPerson));
        }
        return String.format(MESSAGE_DUPLICATE_PERSON_HIDDEN,
                savedPersons.indexOf(existingPerson) + 1, Messages.format(existingPerson));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
