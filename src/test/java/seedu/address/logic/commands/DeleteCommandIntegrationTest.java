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

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks batch deletion through parsing, filtered and sorted indices, history and saved data.
 */
public class DeleteCommandIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withTags("Batch")
            .withMajor("Computing").withGraduationYear("2027").withNotes("Project teammate").build();
    private final Person bob = new PersonBuilder().withName("Bob").withTags("Batch").withFavourite(true).build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withTags("Batch").build();
    private final Person dana = new PersonBuilder().withName("Dana").withTags("Batch").build();
    private final Person echo = new PersonBuilder().withName("Echo").withTags("Friend").build();
    private final Person foxtrot = new PersonBuilder().withName("Foxtrot").withTags("Friend").build();
    private final List<Person> originalPersons = List.of(charlie, foxtrot, alice, echo, bob, dana);

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(originalPersons);
        model = new ModelManager(addressBook, new UserPrefs());
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_nonConsecutiveIndices_deletesOriginalContactsAndSavesResult() throws Exception {
        CommandResult result = logic.execute("delete 1 3 5");

        assertEquals("Deleted 3 contacts:\n1. " + Messages.format(charlie)
                + "\n3. " + Messages.format(alice) + "\n5. " + Messages.format(bob), result.getFeedbackToUser());
        assertEquals(List.of(foxtrot, echo, dana), model.getFilteredPersonList());
        assertSavedPersons(List.of(foxtrot, echo, dana));
    }

    @Test
    public void execute_filteredSortedBatch_preservesHiddenContactsAndCurrentView() throws Exception {
        logic.execute("filter t/Batch");
        logic.execute("sort name desc");
        assertEquals(List.of(dana, charlie, bob, alice), model.getFilteredPersonList());

        CommandResult result = logic.execute("delete 3 1");

        assertEquals("Deleted 2 contacts:\n3. " + Messages.format(bob)
                + "\n1. " + Messages.format(dana), result.getFeedbackToUser());
        assertEquals(List.of(charlie, alice), model.getFilteredPersonList());
        assertSavedPersons(List.of(charlie, foxtrot, alice, echo));
        logic.execute("list");
        assertEquals(List.of(foxtrot, echo, charlie, alice), model.getFilteredPersonList());
    }

    @Test
    public void execute_batch_isOneUndoRedoStepAndPersistsAcrossReloads() throws Exception {
        assertFalse(model.canUndoAddressBook());
        String deletionFeedback = logic.execute("delete 5 1 3").getFeedbackToUser();
        List<Person> remaining = List.of(foxtrot, echo, dana);
        assertSavedPersons(remaining);
        assertEquals(deletionFeedback, model.getUndoDescription().orElseThrow());

        assertEquals("Undid: " + deletionFeedback, logic.execute("undo").getFeedbackToUser());

        assertSavedPersons(originalPersons);
        assertEquals(originalPersons, model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
        assertEquals(deletionFeedback, model.getRedoDescription().orElseThrow());
        assertEquals("Redid: " + deletionFeedback, logic.execute("redo").getFeedbackToUser());
        assertSavedPersons(remaining);
        assertEquals(remaining, model.getFilteredPersonList());
        assertFalse(model.canRedoAddressBook());
        assertEquals(deletionFeedback, model.getUndoDescription().orElseThrow());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(remaining, reloaded.getFilteredPersonList());
        assertFalse(reloaded.canUndoAddressBook());
    }

    @Test
    public void execute_rejectedBatches_preserveContactsViewSavedFileAndHistory() throws Exception {
        logic.execute("add n/HistoryFirst a/NUS");
        logic.execute("add n/HistorySecond a/NUS");
        AddressBook afterSecondAdd = new AddressBook(model.getAddressBook());
        logic.execute("undo");
        logic.execute("find Alice Bob Charlie");
        logic.execute("sort name desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String savedBefore = FileUtil.readFromFile(storage.getAddressBookFilePath());

        String expectedRangeError = "Index 4 is outside the current displayed list. "
                + "No contacts were deleted. Choose an index from 1 to 3.";
        for (String command : List.of("delete 1 3 4", "delete 4 1 3")) {
            assertThrows(CommandException.class, expectedRangeError, () -> logic.execute(command));
            assertUnchangedAfterRejection(before, displayedBefore, savedBefore);
        }
        String expectedDuplicateError = String.format(DeleteCommand.MESSAGE_DUPLICATE_INDEX, 1);
        for (String command : List.of("delete 1 1", "delete 1 01")) {
            assertThrows(CommandException.class, expectedDuplicateError, () -> logic.execute(command));
            assertUnchangedAfterRejection(before, displayedBefore, savedBefore);
        }
        String expectedUsage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        for (String command : List.of("delete 1 0", "delete 1 -2", "delete 1 2147483648", "delete 1 invalid",
                "delete 1,3", "delete 1-3")) {
            assertThrows(ParseException.class, expectedUsage, () -> logic.execute(command));
            assertUnchangedAfterRejection(before, displayedBefore, savedBefore);
        }

        logic.execute("redo");
        assertEquals(afterSecondAdd, model.getAddressBook());
        assertEquals(afterSecondAdd, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_allDisplayedContacts_keepsHiddenContactsAndCanUndo() throws Exception {
        logic.execute("filter t/Batch");
        logic.execute("sort name desc");

        logic.execute("delete 4 2 3 1");

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertSavedPersons(List.of(foxtrot, echo));
        assertThrows(CommandException.class,
                DeleteCommand.MESSAGE_NO_DISPLAYED_CONTACTS, () -> logic.execute("delete 1 2"));
        logic.execute("undo");
        assertSavedPersons(originalPersons);
        assertEquals(List.of(foxtrot, echo, dana, charlie, bob, alice), model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_allSavedContacts_canUndoAndRedoWholeBatch() throws Exception {
        logic.execute("delete 6 1 5 2 4 3");

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertSavedPersons(List.of());
        assertThrows(CommandException.class, DeleteCommand.MESSAGE_NO_CONTACTS, () -> logic.execute("delete 1 2"));
        logic.execute("undo");
        assertSavedPersons(originalPersons);
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertSavedPersons(List.of());
        assertFalse(model.canRedoAddressBook());
    }

    private void assertUnchangedAfterRejection(AddressBook before, List<Person> displayedBefore,
            String savedBefore) throws Exception {
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(savedBefore, FileUtil.readFromFile(storage.getAddressBookFilePath()));
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
    }

    private void assertSavedPersons(List<Person> expected) throws Exception {
        assertEquals(expected, model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
