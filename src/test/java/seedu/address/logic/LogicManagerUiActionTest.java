package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks UI-only actions and recovery after failed contact saves. */
public class LogicManagerUiActionTest {
    @TempDir
    public Path directory;

    private Model model;
    private LogicManager logic;
    private FailingAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new FailingAddressBookStorage(directory.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void help_storageCannotSave_returnsHelpWithoutAttemptingSave() throws Exception {
        CommandResult result = logic.execute("help");

        assertTrue(result.isShowHelp());
        assertEquals(0, storage.saveAttempts);
    }

    @Test
    public void exit_withoutUnsavedChanges_doesNotRequireWritableStorage() throws Exception {
        CommandResult result = logic.execute("exit");

        assertTrue(result.isExit());
        assertEquals(0, storage.saveAttempts);
    }

    @Test
    public void help_afterFailedAdd_keepsPendingContactsAndHistory() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("add n/Unsaved a/NUS"));

        assertTrue(logic.execute("help").isShowHelp());

        assertEquals(1, storage.saveAttempts);
        assertEquals("Unsaved", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertTrue(model.canUndoAddressBook());
        CommandException error = assertThrows(CommandException.class, () -> logic.execute("exit"));
        assertTrue(error.getMessage().contains(LogicManager.MESSAGE_UNSAVED_EXIT));
        assertEquals(2, storage.saveAttempts);
    }

    @Test
    public void exit_afterFailedAdd_retainsUnsavedDataUntilRetrySucceeds() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("add n/Unsaved a/NUS"));

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("exit"));
        assertTrue(error.getMessage().contains(LogicManager.MESSAGE_UNSAVED_EXIT));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(model.canUndoAddressBook());

        storage.failure = null;
        assertTrue(logic.execute("exit").isExit());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(3, storage.saveAttempts);
    }

    @Test
    public void list_afterFailedAdd_explainsPendingChangesAndCanRetrySaving() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("add n/Unsaved a/NUS"));

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("list"));
        assertTrue(error.getMessage().contains(LogicManager.MESSAGE_PENDING_UNSAVED_CHANGES));

        storage.failure = null;
        logic.execute("list");
        storage.failure = new AccessDeniedException("contacts.json");
        assertTrue(logic.execute("exit").isExit());
        assertEquals(3, storage.saveAttempts);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void undo_afterFailedAdd_restoresSavedStateAndAllowsExit() throws Exception {
        assertThrows(CommandException.class, () -> logic.execute("add n/Unsaved a/NUS"));

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertFalse(error.getMessage().contains("not been saved"));
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(2, storage.saveAttempts);
    }

    @Test
    public void exit_afterSuccessfulSave_usesLatestSavedContactState() throws Exception {
        storage.failure = null;
        logic.execute("add n/Saved a/NUS");
        storage.failure = new IOException("Disk unavailable");

        assertTrue(logic.execute("exit").isExit());

        assertEquals(1, storage.saveAttempts);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private static class FailingAddressBookStorage extends JsonAddressBookStorage {
        private IOException failure = new AccessDeniedException("contacts.json");
        private int saveAttempts;

        FailingAddressBookStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
