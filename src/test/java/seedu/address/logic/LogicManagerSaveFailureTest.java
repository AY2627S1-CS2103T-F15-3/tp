package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/** Regression tests for saving failures after commands have changed the model. */
public class LogicManagerSaveFailureTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private FailingAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").withPhone("91234567")
                .withTags("Friend").withNotes("Met at orientation").withFavourite(true)
                .withMajor("Computing").withGraduationYear("2027")
                .withLastUpdated(LocalDate.of(2020, 1, 2)).build();
        Person bob = new PersonBuilder().withName("Bob").withTags("Friend").build();
        Person charlie = new PersonBuilder().withName("Charlie").withTags("Other").build();
        AddressBook initialData = new AddressBookBuilder().withPerson(alice).withPerson(bob)
                .withPerson(charlie).build();
        model = new ModelManager(initialData, new UserPrefs());
        addressBookStorage = new FailingAddressBookStorage(temporaryFolder.resolve("contacts.json"));
        addressBookStorage.saveAddressBook(initialData);
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_deleteIoFailure_retryDeletesOriginalContact() throws Exception {
        assertDeleteFailureCanBeRetried(new IOException("Disk unavailable"));
    }

    @Test
    public void execute_deletePermissionFailure_retryDeletesOriginalContact() throws Exception {
        assertDeleteFailureCanBeRetried(new AccessDeniedException("contacts.json"));
    }

    private void assertDeleteFailureCanBeRetried(IOException failure) throws Exception {
        logic.execute("filter t/Friend");
        logic.execute("sort name desc");
        AddressBook previousData = new AddressBook(model.getAddressBook());
        List<Person> previousView = new ArrayList<>(model.getFilteredPersonList());
        Person originalTarget = previousView.get(0);
        addressBookStorage.failure = failure;

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("delete 1"));

        String errorFormat = failure instanceof AccessDeniedException
                ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
        assertEquals(String.format(errorFormat, failure.getMessage()), error.getMessage());
        assertEquals(failure, error.getCause());
        assertEquals(previousData, model.getAddressBook());
        assertEquals(previousView, model.getFilteredPersonList());
        assertEquals(previousData, addressBookStorage.readAddressBook().orElseThrow());

        addressBookStorage.failure = null;
        logic.execute("delete 1");

        previousData.removePerson(originalTarget);
        assertEquals(previousData, model.getAddressBook());
        assertEquals(previousView.subList(1, previousView.size()), model.getFilteredPersonList());
        assertEquals(previousData, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_mutationSaveFailures_restoreEveryContactFieldAndHistory() throws Exception {
        AddressBook previousData = new AddressBook(model.getAddressBook());
        List<Person> previousView = new ArrayList<>(model.getFilteredPersonList());
        addressBookStorage.failure = new IOException("Disk unavailable");
        for (String command : List.of("add n/Delta a/NUS", "edit 1 n/Alicia", "note 1 Changed note",
                "delnote 1", "fav 2", "unfav 1", "tag 1 t/Extra", "untag 1 t/Friend", "clear")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(previousData, model.getAddressBook(), command);
            assertEquals(previousView, model.getFilteredPersonList(), command);
            assertFalse(model.canUndoAddressBook(), command);
            assertFalse(model.canRedoAddressBook(), command);
        }
    }

    @Test
    public void execute_viewSaveFailures_restoreFilterAndSort() throws Exception {
        logic.execute("filter t/Friend");
        logic.execute("sort name desc");
        AddressBook previousData = new AddressBook(model.getAddressBook());
        List<Person> previousView = new ArrayList<>(model.getFilteredPersonList());
        addressBookStorage.failure = new IOException("Disk unavailable");
        for (String command : List.of("list", "find Charlie", "filter t/Other", "sort name asc", "listfav")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(previousData, model.getAddressBook(), command);
            assertEquals(previousView, model.getFilteredPersonList(), command);
        }
    }

    @Test
    public void execute_failedAddWithUndoAndRedo_restoresBothHistoryStacks() throws Exception {
        prepareUndoAndRedo();
        AddressBook previousData = new AddressBook(model.getAddressBook());
        logic.execute("filter t/Friend");
        logic.execute("sort name desc");
        List<Person> previousView = new ArrayList<>(model.getFilteredPersonList());
        addressBookStorage.failure = new IOException("Disk unavailable");

        assertThrows(CommandException.class, () -> logic.execute("add n/Delta a/NUS"));

        assertEquals(previousData, model.getAddressBook());
        assertEquals(previousView, model.getFilteredPersonList());
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        addressBookStorage.failure = null;
        logic.execute("redo");
        assertFirstPhone("90000002");
        assertFalse(model.canRedoAddressBook());
        logic.execute("undo");
        assertFirstPhone("90000001");
        logic.execute("undo");
        assertFirstPhone("91234567");
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertFirstPhone("90000001");
        logic.execute("redo");
        assertFirstPhone("90000002");
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_failedUndo_canRetrySameHistoryStep() throws Exception {
        assertFailedHistoryCommandCanBeRetried("undo", "91234567");
    }

    @Test
    public void execute_failedRedo_canRetrySameHistoryStep() throws Exception {
        assertFailedHistoryCommandCanBeRetried("redo", "90000002");
    }

    private void assertFailedHistoryCommandCanBeRetried(String command, String expectedPhone) throws Exception {
        prepareUndoAndRedo();
        logic.execute("filter t/Friend");
        logic.execute("sort name desc");
        AddressBook previousData = new AddressBook(model.getAddressBook());
        List<Person> previousView = new ArrayList<>(model.getFilteredPersonList());
        addressBookStorage.failure = new IOException("Disk unavailable");

        assertThrows(CommandException.class, () -> logic.execute(command));

        assertEquals(previousData, model.getAddressBook());
        assertEquals(previousView, model.getFilteredPersonList());
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        addressBookStorage.failure = null;
        logic.execute(command);
        assertFirstPhone(expectedPhone);
        if (command.equals("undo")) {
            logic.execute("redo");
            assertFirstPhone("90000001");
            logic.execute("redo");
            assertFirstPhone("90000002");
        } else {
            logic.execute("undo");
            assertFirstPhone("90000001");
            logic.execute("undo");
            assertFirstPhone("91234567");
        }
    }

    @Test
    public void execute_parseAndCommandFailures_preserveHistoryAndDoNotSave() throws Exception {
        prepareUndoAndRedo();
        AddressBook previousData = new AddressBook(model.getAddressBook());
        int previousSaveCalls = addressBookStorage.saveCalls;

        assertThrows(ParseException.class, () -> logic.execute("delete invalid"));
        assertThrows(CommandException.class, () -> logic.execute("delete 999"));

        assertEquals(previousData, model.getAddressBook());
        assertEquals(previousSaveCalls, addressBookStorage.saveCalls);
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertFirstPhone("90000002");
    }

    private void prepareUndoAndRedo() throws Exception {
        logic.execute("edit 1 p/90000001");
        logic.execute("edit 1 p/90000002");
        logic.execute("undo");
    }

    private void assertFirstPhone(String expectedPhone) {
        assertEquals(expectedPhone, model.getAddressBook().getPersonList().get(0).getPhone().value);
    }

    /** Fails before writing so a test can retry the exact same command once storage recovers. */
    private static class FailingAddressBookStorage extends JsonAddressBookStorage {
        private IOException failure;
        private int saveCalls;

        FailingAddressBookStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCalls++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
