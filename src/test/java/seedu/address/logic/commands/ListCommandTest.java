package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, expectedFeedback(expectedModel.getFilteredPersonList().size()),
                expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, expectedFeedback(expectedModel.getFilteredPersonList().size()),
                expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_reportsZeroContacts() {
        Model emptyModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel, expectedFeedback(0), new ModelManager());
        assertFalse(emptyModel.canUndoAddressBook());
    }

    @Test
    public void execute_afterSearchFilterAndFavourites_restoresEveryContact() throws Exception {
        Model contacts = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").withMajor("Computing").withFavourite(true).build();
        Person bob = new PersonBuilder().withName("Bob").withMajor("Business").build();
        contacts.addPerson(bob);
        contacts.addPerson(alice);
        AddressBook before = new AddressBook(contacts.getAddressBook());
        AddressBookParser parser = new AddressBookParser();

        for (String command : List.of("find Alice", "filter m/Computing", "listfav")) {
            parser.parseCommand(command).execute(contacts);
            assertEquals(List.of(alice), contacts.getFilteredPersonList());
            assertEquals(expectedFeedback(2), new ListCommand().execute(contacts).getFeedbackToUser());
            assertEquals(List.of(bob, alice), contacts.getFilteredPersonList());
            assertEquals(before, contacts.getAddressBook());
        }

        parser.parseCommand("find Nobody").execute(contacts);
        assertTrue(contacts.getFilteredPersonList().isEmpty());
        assertEquals(expectedFeedback(2), new ListCommand().execute(contacts).getFeedbackToUser());
        assertEquals(List.of(bob, alice), contacts.getFilteredPersonList());
    }

    @Test
    public void execute_preservesActiveSortAndStoredContactOrder() throws Exception {
        Model contacts = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        contacts.addPerson(bob);
        contacts.addPerson(alice);
        AddressBook before = new AddressBook(contacts.getAddressBook());
        new SortCommand(NAME, ASC).execute(contacts);
        new AddressBookParser().parseCommand("find Bob").execute(contacts);

        new ListCommand().execute(contacts);

        assertEquals(List.of(alice, bob), contacts.getFilteredPersonList());
        assertEquals(List.of(bob, alice), contacts.getAddressBook().getPersonList());
        assertEquals(before, contacts.getAddressBook());
    }

    @Test
    public void execute_preservesUndoAndRedoHistory() throws Exception {
        Model contacts = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        new AddCommand(alice).execute(contacts);
        new AddCommand(bob).execute(contacts);
        new UndoCommand().execute(contacts);
        AddressBook before = new AddressBook(contacts.getAddressBook());

        assertEquals(expectedFeedback(1), new ListCommand().execute(contacts).getFeedbackToUser());
        assertEquals(before, contacts.getAddressBook());
        assertTrue(contacts.canUndoAddressBook());
        assertTrue(contacts.canRedoAddressBook());
        new RedoCommand().execute(contacts);
        assertEquals(List.of(alice, bob), contacts.getFilteredPersonList());
        new ListCommand().execute(contacts);
        new UndoCommand().execute(contacts);
        assertEquals(List.of(alice), contacts.getFilteredPersonList());
    }

    @Test
    public void parse_extraArguments_preservesCurrentView() throws Exception {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        List<Person> before = List.copyOf(model.getFilteredPersonList());
        AddressBook addressBook = new AddressBook(model.getAddressBook());
        AddressBookParser parser = new AddressBookParser();

        String expectedMessage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("list all"));

        assertEquals(before, model.getFilteredPersonList());
        assertEquals(addressBook, model.getAddressBook());
    }

    private static String expectedFeedback(int count) {
        return ListCommand.MESSAGE_SUCCESS + "\n" + String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count);
    }
}
