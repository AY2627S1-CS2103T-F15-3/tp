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
 * Verifies favourite filters through parsing, displayed-index commands, history and storage.
 */
public class FavouriteFilterIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Mathematics")
            .withGraduationYear("2027").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate", "NOC").withFavourite(true).build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withMajor("Computer Science")
            .withGraduationYear("2028").withTags("Friend").withFavourite(true).build();
    private final Person dora = new PersonBuilder().withName("Dora").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").build();
    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        for (Person person : List.of(alice, bob, charlie, dora)) {
            model.addPerson(person);
        }
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_combinedCriteria_preservesSortAndContactData() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        logic.execute("sort name desc");
        logic.execute("filter f/true m/Computer Science");
        assertEquals(List.of(charlie, bob), model.getFilteredPersonList());
        logic.execute("filter m/Computer Science c/2027 t/classmate f/true");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
        assertEquals(before, new AddressBook(storage.readAddressBook().orElseThrow()));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_unfavAndTag_retainCombinedFilterAndUseRenumberedIndices() throws Exception {
        logic.execute("filter f/true m/Computer Science");
        logic.execute("unfav 1");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        logic.execute("tag 1 t/NOC");
        Person taggedCharlie = new PersonBuilder(charlie).withTags("Friend", "NOC").build();
        assertEquals(List.of(taggedCharlie), model.getFilteredPersonList());
        logic.execute("unfav 1");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(alice, new PersonBuilder(bob).withFavourite(false).build(),
                new PersonBuilder(taggedCharlie).withFavourite(false).build(), dora),
                model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_fav_removesContactFromNonFavouriteResults() throws Exception {
        logic.execute("filter f/false m/Computer Science");
        assertEquals(List.of(dora), model.getFilteredPersonList());
        logic.execute("fav 1");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(new PersonBuilder(dora).withFavourite(true).build(),
                model.getAddressBook().getPersonList().get(3));
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_delete_usesCombinedFilteredIndexAndPersistsRemainingContacts() throws Exception {
        logic.execute("filter f/true m/Computer Science c/2027 t/Classmate");
        logic.execute("delete 1");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(alice, charlie, dora), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_edit_usesCombinedFilteredIndexAndRestoresFullView() throws Exception {
        logic.execute("filter f/true m/Computer Science c/2027 t/Classmate");
        logic.execute("edit 1 m/Mathematics");
        Person editedBob = new PersonBuilder(bob).withMajor("Mathematics").build();
        assertEquals(List.of(alice, editedBob, charlie, dora), model.getFilteredPersonList());
        logic.execute("filter f/true m/Computer Science");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidFavourite_preservesViewDataAndSavedContacts() throws Exception {
        logic.execute("filter f/true m/Computer Science");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : new String[]{"filter f/", "filter f/yes", "filter f/true f/true",
            "filter f/true f/false", "filter F/true", "filter f/true x/value"}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertEquals(before, new AddressBook(storage.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void execute_favouriteFilter_doesNotConsumeUndoOrRedoHistory() throws Exception {
        logic.execute("add n/Eve a/NUS m/Mathematics");
        logic.execute("filter f/true m/Computer Science");
        assertTrue(model.canUndoAddressBook());
        logic.execute("undo");
        assertEquals(List.of(alice, bob, charlie, dora), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
        logic.execute("filter f/false m/Mathematics");
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(5, model.getFilteredPersonList().size());
        assertEquals("Eve", model.getAddressBook().getPersonList().get(4).getName().fullName);
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_reloadingContacts_preservesFavouriteStatusWithoutPersistingFilter() throws Exception {
        logic.execute("filter f/true m/Computer Science c/2027 t/Classmate");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(List.of(alice, bob, charlie, dora), reloaded.getFilteredPersonList());
    }
}
