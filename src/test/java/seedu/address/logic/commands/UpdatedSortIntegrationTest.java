package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

/** Checks date sorting through parsing, filtering, displayed indices, history and persistence. */
public class UpdatedSortIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withTags("Friend")
            .withLastUpdated(LocalDate.of(2021, 1, 1)).withNotes("Met at orientation").withFavourite(true).build();
    private final Person bob = new PersonBuilder().withName("Bob").withTags("Classmate")
            .withLastUpdated(LocalDate.of(2018, 1, 1)).build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withTags("Classmate")
            .withLastUpdated(LocalDate.of(2018, 1, 1)).build();
    private final Person dana = new PersonBuilder().withName("Dana").withTags("Classmate")
            .withLastUpdated(LocalDate.of(2019, 1, 1)).build();

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
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }

    @Test
    public void execute_updatedDirections_preserveContactDataAndSavedOrder() throws Exception {
        AddressBook originalData = new AddressBook(model.getAddressBook());
        String originalJson = Files.readString(storage.getAddressBookFilePath());

        assertEquals("Sorted contacts by updated (asc).",
                logic.execute("sort updated asc").getFeedbackToUser());
        assertEquals(List.of(bob, charlie, dana, alice), model.getFilteredPersonList());
        assertEquals("Sorted contacts by updated (desc).",
                logic.execute("SORT UpDaTeD DeSc").getFeedbackToUser());
        assertEquals(List.of(alice, dana, bob, charlie), model.getFilteredPersonList());

        assertEquals(originalData, model.getAddressBook());
        assertEquals(originalData, storage.readAddressBook().orElseThrow());
        assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_updatedSort_retainsFiltersAndAppliesToLaterViews() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated desc");
        assertEquals(List.of(dana, bob, charlie), model.getFilteredPersonList());
        logic.execute("find Alice Bob");
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        logic.execute("listfav");
        assertEquals(List.of(alice), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(alice, dana, bob, charlie), model.getFilteredPersonList());
        assertSavedData();
    }

    @Test
    public void execute_delete_targetsDisplayedIndexUnderDateSortAndFilter() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated desc");
        logic.execute("delete 2");

        assertEquals(List.of(dana, charlie), model.getFilteredPersonList());
        assertEquals(List.of(charlie, dana, alice), model.getAddressBook().getPersonList());
        assertSavedData();
    }

    @Test
    public void execute_editUndoRedo_restoreDatesAndReorderWithoutChangingSavedOrder() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated asc");
        assertEquals(List.of(bob, charlie, dana), model.getFilteredPersonList());
        logic.execute("edit 1 p/90000001");
        Person editedBob = model.getAddressBook().getPersonList().get(2);
        assertEquals(new PersonBuilder(bob).withPhone("90000001")
                .withLastUpdated(editedBob.getLastUpdated()).build(), editedBob);
        assertTrue(editedBob.getLastUpdated().isAfter(alice.getLastUpdated()));
        assertEquals(List.of(charlie, dana, alice, editedBob), model.getFilteredPersonList());
        assertEquals(List.of(charlie, dana, editedBob, alice), model.getAddressBook().getPersonList());
        assertSavedData();

        logic.execute("undo");
        assertEquals(List.of(bob, charlie, dana, alice), model.getFilteredPersonList());
        assertEquals(List.of(charlie, dana, bob, alice), model.getAddressBook().getPersonList());
        assertEquals(bob.getLastUpdated(), model.getAddressBook().getPersonList().get(2).getLastUpdated());
        assertSavedData();
        logic.execute("sort updated desc");
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(List.of(editedBob, alice, dana, charlie), model.getFilteredPersonList());
        assertEquals(editedBob.getLastUpdated(), model.getFilteredPersonList().get(0).getLastUpdated());
        assertSavedData();
        logic.execute("undo");
        assertEquals(List.of(alice, dana, bob, charlie), model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
        assertSavedData();
    }

    @Test
    public void execute_invalidUpdatedSort_preservesFilterOrderAndRedoHistory() throws Exception {
        logic.execute("edit 1 p/90000001");
        logic.execute("undo");
        logic.execute("filter t/Classmate");
        logic.execute("sort updated desc");
        AddressBook previousData = new AddressBook(model.getAddressBook());
        for (String command : List.of("sort updated", "sort updated newest", "sort updated asc desc",
                "sort lastUpdated asc", "sort updated asc 1")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(dana, bob, charlie), model.getFilteredPersonList());
            assertEquals(previousData, model.getAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertFalse(model.canUndoAddressBook());
        }
        assertSavedData();
    }

    @Test
    public void execute_emptyAndSingletonUpdatedSort_succeedsWithoutHistoryChanges() throws Exception {
        logic.execute("find NoSuchContact");
        logic.execute("sort updated asc");
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("find Alice");
        logic.execute("sort updated desc");
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
        assertSavedData();
    }

    @Test
    public void execute_reloadingData_preservesDatesAndRestoresSavedOrder() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated asc");
        Model reopened = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());

        assertEquals(List.of(charlie, dana, bob, alice), reopened.getFilteredPersonList());
        assertFalse(reopened.canUndoAddressBook());
        assertFalse(reopened.canRedoAddressBook());
    }

    private void assertSavedData() throws Exception {
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
