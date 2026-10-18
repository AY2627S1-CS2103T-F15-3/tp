package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
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
 * Checks filtering through the normal parser, model and storage path, including displayed indices.
 */
public class FilterCommandIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Mathematics")
            .withGraduationYear("2027").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate", "NOC").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").build();
    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(charlie);
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_delete_usesFilteredIndexAndPersistsRemainingContacts() throws Exception {
        logic.execute("filter m/Computer Science");
        logic.execute("delete 1");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        assertEquals(List.of(alice, charlie), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_edit_usesFilteredIndexAndKeepsExistingResetBehaviour() throws Exception {
        logic.execute("filter m/Computer Science");
        logic.execute("edit 1 m/Mathematics");
        Person editedBob = new PersonBuilder(bob).withMajor("Mathematics").build();
        assertEquals(List.of(alice, editedBob, charlie), model.getFilteredPersonList());
        logic.execute("filter m/Computer Science");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_tagAndUntag_preservesFilterAndRenumbersRemainingResults() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("untag 1 t/Classmate");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        logic.execute("tag 1 t/NOC");
        Person taggedCharlie = new PersonBuilder(charlie).withTags("Classmate", "NOC").build();
        assertEquals(List.of(taggedCharlie), model.getFilteredPersonList());
        logic.execute("untag 1 t/Classmate");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(3, model.getAddressBook().getPersonList().size());
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_listFindAndFilter_replaceThePreviousView() throws Exception {
        logic.execute("find Alice");
        logic.execute("filter m/Computer Science");
        assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
        logic.execute("filter t/Friend");
        assertEquals(List.of(alice), model.getFilteredPersonList());
        logic.execute("find Charlie");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
    }

    @Test
    public void execute_add_preservesExistingShowAllBehaviour() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("add n/David p/94567890 e/david@example.com a/Kent Ridge m/Mathematics");
        assertEquals(4, model.getFilteredPersonList().size());
        logic.execute("filter t/Classmate");
        assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidInput_preservesViewContactsAndSavedData() throws Exception {
        logic.execute("filter t/Classmate");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : new String[]{"filter", "filter m/", "filter c/2100", "filter t/",
            "filter t/Classmate t/Friend", "filter n/Alice", "filter m/Computer Science x/value"}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertEquals(before, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
        assertThrows(CommandException.class, () -> logic.execute("delete 3"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_filter_doesNotConsumeUndoOrRedoHistory() throws Exception {
        logic.execute("add n/David p/94567890 e/david@example.com a/Kent Ridge");
        logic.execute("filter m/Mathematics");
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(List.of(alice, bob, charlie), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
        logic.execute("filter t/Classmate");
        assertTrue(model.canRedoAddressBook());
    }

    @Test
    public void execute_reloadingData_doesNotPersistTheFilter() throws Exception {
        logic.execute("filter m/Computer Science c/2027 t/classmate");
        assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(List.of(alice, bob, charlie), reloaded.getFilteredPersonList());
    }
}
