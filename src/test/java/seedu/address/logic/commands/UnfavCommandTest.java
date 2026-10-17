package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UnfavCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_favourite_preservesOtherFieldsAndAllowsRefavourite() throws Exception {
        Person original = new PersonBuilder().withFavourite(true).withMajor("Computing")
                .withGraduationYear("2027").withTags("NOC", "Friend").build();
        model.addPerson(original);
        Person expected = new PersonBuilder(original).withFavourite(false).build();
        assertEquals(String.format(UnfavCommand.MESSAGE_SUCCESS, Messages.format(expected)),
                parser.parseCommand("unfav 1").execute(model).getFeedbackToUser());
        assertEquals(expected, model.getFilteredPersonList().get(0));
        CommandException error = assertThrows(CommandException.class, () -> parser.parseCommand("unfav 1")
                .execute(model));
        assertEquals(UnfavCommand.MESSAGE_NOT_FAVOURITE, error.getMessage());
        assertEquals(expected, model.getFilteredPersonList().get(0));
        parser.parseCommand("fav 1").execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").withFavourite(true).build();
        Person bob = new PersonBuilder().withName("Bob").withFavourite(true).build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        parser.parseCommand("unfav 1").execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withFavourite(false).build(), model.getFilteredPersonList().get(0));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        CommandException error = assertThrows(CommandException.class, () -> parser.parseCommand("unfav 2")
                .execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, error.getMessage());
    }

    @Test
    public void parse_invalidIndex_rejectsInput() {
        for (String input : new String[]{"unfav", "unfav 0", "unfav -1", "unfav +1", "unfav x", "unfav 1.5",
            "unfav 2147483648", "unfav 1 2", "unfav 1 t/NOC"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, UnfavCommand.MESSAGE_USAGE),
                    error.getMessage());
        }
        assertThrows(CommandException.class, () -> parser.parseCommand("unfav 1").execute(model));
    }
}
