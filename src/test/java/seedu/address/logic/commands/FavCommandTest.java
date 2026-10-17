package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class FavCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_validIndex_preservesAllOtherFields() throws Exception {
        Person original = new PersonBuilder().withMajor("Computing").withGraduationYear("2027")
                .withTags("NOC", "Friend").build();
        assertFalse(original.isFavourite());
        model.addPerson(original);
        CommandResult result = parser.parseCommand("fav 1").execute(model);
        Person expected = new PersonBuilder(original).withFavourite(true).build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertTrue(original.isSamePerson(expected));
        assertFalse(original.equals(expected));
        assertEquals(String.format(FavCommand.MESSAGE_SUCCESS, Messages.format(expected)), result.getFeedbackToUser());
        CommandException error = assertThrows(CommandException.class, () ->
                parser.parseCommand("fav 1").execute(model));
        assertEquals(FavCommand.MESSAGE_ALREADY_FAVOURITE, error.getMessage());
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_filteredList_targetsDisplayedPerson() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        parser.parseCommand("fav 1").execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withFavourite(true).build(), model.getFilteredPersonList().get(0));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        CommandException error = assertThrows(CommandException.class, () ->
                parser.parseCommand("fav 2").execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, error.getMessage());
    }

    @Test
    public void parse_invalidIndices_rejectsInput() {
        for (String input : new String[]{"fav", "fav 0", "fav -1", "fav +1", "fav 1.5", "fav x",
            "fav 2147483648", "fav 1 2", "fav 1 t/NOC"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, FavCommand.MESSAGE_USAGE),
                    error.getMessage());
        }
        assertThrows(CommandException.class, () -> parser.parseCommand("fav 1").execute(model));
    }

    @Test
    public void execute_existingCommands_preserveFavourite() throws Exception {
        model.addPerson(new PersonBuilder().withFavourite(true).withTags("Friend").build());
        for (String input : new String[]{"edit 1 p/91234567", "edit 1 m/Computing c/2027", "tag 1 t/NOC d/2",
            "untag 1 t/NOC", "edit 1 t/"}) {
            parser.parseCommand(input).execute(model);
            assertTrue(model.getFilteredPersonList().get(0).isFavourite(), input);
        }
        assertEquals("91234567", model.getFilteredPersonList().get(0).getPhone().value);
        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
    }
}
