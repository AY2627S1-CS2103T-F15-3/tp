package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ListFavCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();
    private final Person ordinary = new PersonBuilder().withName("Alice").build();
    private final Person first = new PersonBuilder().withName("Bob").withFavourite(true)
            .withMajor("Computing").withGraduationYear("2027").withTags("Friend", "NOC").build();
    private final Person second = new PersonBuilder().withName("Charlie").withFavourite(true).build();

    @Test
    public void execute_oneFavourite_preservesAllData() throws Exception {
        model.addPerson(ordinary);
        model.addPerson(first);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(String.format(ListFavCommand.MESSAGE_SUCCESS, 1),
                parser.parseCommand("listfav").execute(model).getFeedbackToUser());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_multipleFavourites_preservesOrderAndListRestoresEveryone() throws Exception {
        model.addPerson(second);
        model.addPerson(ordinary);
        model.addPerson(first);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(String.format(ListFavCommand.MESSAGE_SUCCESS, 2),
                parser.parseCommand("listfav").execute(model).getFeedbackToUser());
        assertEquals(List.of(second, first), model.getFilteredPersonList());
        parser.parseCommand("list").execute(model);
        assertEquals(List.of(second, ordinary, first), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_noFavourites_showsEmptyListAndMessage() throws Exception {
        assertEquals(ListFavCommand.MESSAGE_EMPTY, parser.parseCommand("listfav").execute(model).getFeedbackToUser());
        model.addPerson(ordinary);
        assertEquals(ListFavCommand.MESSAGE_EMPTY, parser.parseCommand("listfav").execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(ordinary), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_unfav_usesFilteredIndexAndRemovesPersonFromView() throws Exception {
        model.addPerson(ordinary);
        model.addPerson(first);
        model.addPerson(second);
        parser.parseCommand("listfav").execute(model);
        CommandException duplicate = assertThrows(CommandException.class, () -> parser.parseCommand("fav 1")
                .execute(model));
        assertEquals(FavCommand.MESSAGE_ALREADY_FAVOURITE, duplicate.getMessage());
        parser.parseCommand("unfav 1").execute(model);
        assertEquals(List.of(second), model.getFilteredPersonList());
        Person unfavourited = new PersonBuilder(first).withFavourite(false).build();
        assertEquals(List.of(ordinary, unfavourited, second), model.getAddressBook().getPersonList());
        parser.parseCommand("unfav 1").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertFalse(model.getAddressBook().getPersonList().get(2).isFavourite());
        parser.parseCommand("list").execute(model);
        parser.parseCommand("fav 2").execute(model);
        parser.parseCommand("listfav").execute(model);
        assertEquals(List.of(first), model.getFilteredPersonList());
    }

    @Test
    public void execute_searchAndTagCommands_keepExistingBehaviour() throws Exception {
        model.addPerson(ordinary);
        model.addPerson(first);
        model.addPerson(second);
        parser.parseCommand("find Charlie").execute(model);
        parser.parseCommand("listfav").execute(model);
        assertEquals(List.of(first, second), model.getFilteredPersonList());
        parser.parseCommand("tag 1 t/CCA").execute(model);
        parser.parseCommand("untag 1 t/CCA").execute(model);
        assertEquals(List.of(first, second), model.getFilteredPersonList());
        parser.parseCommand("edit 1 p/98765432").execute(model);
        assertEquals(3, model.getFilteredPersonList().size());
        parser.parseCommand("listfav").execute(model);
        assertEquals(new PersonBuilder(first).withPhone("98765432").build(), model.getFilteredPersonList().get(0));
        parser.parseCommand("find Alice").execute(model);
        assertEquals(List.of(ordinary), model.getFilteredPersonList());
    }

    @Test
    public void parse_extraArguments_rejectsWithoutChangingView() throws Exception {
        model.addPerson(ordinary);
        for (String input : new String[]{"listfav 1", "listfav t/NOC", "listfav extra"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, ListFavCommand.MESSAGE_USAGE),
                    error.getMessage());
            assertEquals(List.of(ordinary), model.getFilteredPersonList());
        }
        assertInstanceOf(ListFavCommand.class, parser.parseCommand("  listfav  "));
    }
}
