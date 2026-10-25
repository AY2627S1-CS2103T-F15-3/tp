package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks sorting through parsing, displayed indices, data changes, history and JSON storage.
 */
public class SortCommandIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Zoology")
            .withGraduationYear("2028").withTags("Friend").withNotes("Met at orientation").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Business")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withMajor("business")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person dana = new PersonBuilder().withName("Dana").withTags("Classmate").build();

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(charlie);
        model.addPerson(dana);
        model.addPerson(bob);
        model.addPerson(alice);
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_allSortOptions_preservesSavedOrderAndUsesAscendingNameTies() throws Exception {
        assertSort("name asc", List.of(alice, bob, charlie, dana));
        assertSort("name desc", List.of(dana, charlie, bob, alice));
        assertSort("major asc", List.of(bob, charlie, alice, dana));
        assertSort("major desc", List.of(alice, bob, charlie, dana));
        assertSort("year asc", List.of(bob, charlie, alice, dana));
        assertSort("year desc", List.of(alice, bob, charlie, dana));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_filtersFindAndList_shareTheActiveSortOrder() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort name desc");
        assertEquals(List.of(dana, charlie, bob), model.getFilteredPersonList());
        logic.execute("filter m/Business");
        assertEquals(List.of(charlie, bob), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(dana, charlie, bob, alice), model.getFilteredPersonList());
        logic.execute("find Business");
        assertEquals(List.of(charlie, bob), model.getFilteredPersonList());
        assertSavedData();
    }

    @Test
    public void execute_numberedTagUntagAndDelete_targetSortedFilteredContacts() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort name desc");
        logic.execute("tag 2 t/Mentor");
        Person taggedCharlie = new PersonBuilder(charlie).withTags("Classmate", "Mentor").build();
        assertEquals(List.of(dana, taggedCharlie, bob), model.getFilteredPersonList());
        logic.execute("untag 1 t/Classmate");
        assertEquals(List.of(taggedCharlie, bob), model.getFilteredPersonList());
        logic.execute("delete 2");
        assertEquals(List.of(taggedCharlie), model.getFilteredPersonList());
        assertFalse(model.getAddressBook().getPersonList().contains(bob));
        logic.execute("list");
        assertEquals(List.of(new PersonBuilder(dana).withTags().build(), taggedCharlie, alice),
                model.getFilteredPersonList());
        assertSavedData();
    }

    @Test
    public void execute_numberedEdit_reordersContactAndUndoRedoKeepNotes() throws Exception {
        logic.execute("sort name desc");
        logic.execute("edit 4 n/Zara c/2029");
        Person editedAlice = new PersonBuilder(alice).withName("Zara").withGraduationYear("2029").build();
        assertEquals(List.of(editedAlice, dana, charlie, bob), model.getFilteredPersonList());
        assertSavedData();
        logic.execute("undo");
        assertEquals(List.of(dana, charlie, bob, alice), model.getFilteredPersonList());
        assertSavedData();
        logic.execute("redo");
        assertEquals(List.of(editedAlice, dana, charlie, bob), model.getFilteredPersonList());
        assertEquals(alice.getNotes(), model.getFilteredPersonList().get(0).getNotes());
        assertSavedData();
    }

    @Test
    public void execute_academicEditAndClearingField_reordersAndKeepsUnspecifiedLast() throws Exception {
        logic.execute("sort year asc");
        logic.execute("edit 1 c/2029");
        Person editedBob = new PersonBuilder(bob).withGraduationYear("2029").build();
        assertEquals(List.of(charlie, alice, editedBob, dana), model.getFilteredPersonList());
        logic.execute("edit 1 c/");
        Person clearedCharlie = new PersonBuilder(charlie).withGraduationYear("").build();
        assertEquals(List.of(alice, editedBob, clearedCharlie, dana), model.getFilteredPersonList());
        logic.execute("sort year desc");
        assertEquals(List.of(editedBob, alice, clearedCharlie, dana), model.getFilteredPersonList());
        assertSavedData();
    }

    @Test
    public void execute_addUndoRedo_usesActiveSortWithoutAddingSortHistory() throws Exception {
        logic.execute("sort name desc");
        logic.execute("add n/Eve p/91234567 e/eve@example.com a/Kent Ridge m/Business c/2026");
        Person eve = new PersonBuilder().withName("Eve").withPhone("91234567").withEmail("eve@example.com")
                .withAddress("Kent Ridge").withMajor("Business").withGraduationYear("2026").build();
        assertEquals(List.of(eve, dana, charlie, bob, alice), model.getFilteredPersonList());
        logic.execute("undo");
        logic.execute("sort year asc");
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(List.of(eve, bob, charlie, alice, dana), model.getFilteredPersonList());
        logic.execute("undo");
        assertEquals(List.of(bob, charlie, alice, dana), model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
        assertSavedData();
    }

    @Test
    public void execute_invalidSort_preservesFilterOrderDataAndRedo() throws Exception {
        logic.execute("add n/Eve p/91234567 e/eve@example.com a/Kent Ridge");
        logic.execute("undo");
        logic.execute("filter t/Classmate");
        logic.execute("sort name desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        for (String command : List.of("sort", "sort name", "sort phone asc", "sort year ascending",
                "sort major asc desc", "SORT PHONE ASC")) {
            assertThrows(ParseException.class, expectedMessage, () -> logic.execute(command));
            assertEquals(List.of(dana, charlie, bob), model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertFalse(model.canUndoAddressBook());
            assertSavedData();
        }
        logic.execute("redo");
        assertEquals(5, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidDisplayedIndex_doesNotChangeSortedFilter() throws Exception {
        logic.execute("filter m/Business");
        logic.execute("sort name desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : List.of("delete 3", "edit 3 p/91234567", "tag 3 t/Mentor", "untag 3 t/Classmate")) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(List.of(charlie, bob), model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertSavedData();
        }
    }

    @Test
    public void execute_emptyResultsAndClear_keepSortForLaterContacts() throws Exception {
        logic.execute("filter m/Mathematics");
        logic.execute("sort name desc");
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("clear");
        logic.execute("add n/Aaron p/91234567 e/aaron@example.com a/Kent Ridge");
        logic.execute("add n/Beth p/92345678 e/beth@example.com a/Clementi");
        assertEquals(List.of("Beth", "Aaron"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertSavedData();
    }

    @Test
    public void execute_reloadingData_restoresSavedOrderWithoutSortOrFilter() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort name desc");
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(List.of(charlie, dana, bob, alice), reloaded.getFilteredPersonList());
        assertFalse(reloaded.canUndoAddressBook());
    }

    private void assertSort(String arguments, List<Person> expected) throws Exception {
        String[] tokens = arguments.split(" ");
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, tokens[0], tokens[1]),
                logic.execute("sort " + arguments).getFeedbackToUser());
        assertEquals(expected, model.getFilteredPersonList());
        assertEquals(List.of(charlie, dana, bob, alice), model.getAddressBook().getPersonList());
        assertSavedData();
    }

    private void assertSavedData() throws Exception {
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
