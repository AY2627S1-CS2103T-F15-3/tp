package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataRecoveryRequiredException;
import seedu.address.commons.util.FileUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;

/**
 * Tests application lifecycle behaviour without opening a window or accessing the user's data files.
 */
public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    private MainApp mainApp;
    private JsonAddressBookStorage addressBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    @BeforeEach
    public void setUp() {
        mainApp = new MainApp();
        mainApp.model = new ModelManager();
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("unimate.json"));
        userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        mainApp.storage = new StorageManager(addressBookStorage, userPrefsStorage);
    }

    @Test
    public void start_validUi_startsUiOnce() {
        UiStub ui = new UiStub();
        mainApp.ui = ui;

        // The stub accepts a null stage, so no JavaFX window or toolkit is needed.
        mainApp.start(null);

        assertEquals(1, ui.startCount);
    }

    @Test
    public void initModel_invalidData_blocksListSaveAndPreservesOriginalFile() throws Exception {
        Path filePath = addressBookStorage.getAddressBookFilePath();
        String validPerson = "{\"name\":\"Saved Contact\",\"address\":\"NUS\",\"tags\":[]}";
        String nullTagPerson = "{\"name\":\"Null Tag\",\"address\":\"NUS\",\"tags\":[null]}";
        String invalidYearPerson = "{\"name\":\"Saved Contact\",\"address\":\"NUS\",\"graduationYear\":\"1999\"}";
        for (String json : List.of("not valid JSON", "{\"persons\":[" + invalidYearPerson + "]}",
                "null", "{\"persons\":[null]}", "{\"persons\":[" + nullTagPerson + "]}",
                "{\"persons\":[" + validPerson + ",null]}",
                "{\"persons\":[" + validPerson + "," + nullTagPerson + "]}")) {
            FileUtil.writeToFile(filePath, json);

            mainApp.model = initializeModel();

            assertEquals(new AddressBook(), mainApp.model.getAddressBook());
            assertEquals(0, mainApp.model.getFilteredPersonList().size());
            assertEquals(json, FileUtil.readFromFile(filePath));
            mainApp.logic = new LogicManager(mainApp.model, mainApp.storage);
            CommandException error = assertThrows(CommandException.class, () -> mainApp.logic.execute("list"));
            assertInstanceOf(DataRecoveryRequiredException.class, error.getCause());
            assertTrue(error.getMessage().contains("disabled for this session"));
            assertEquals(json, FileUtil.readFromFile(filePath));
            UiStub ui = new UiStub();
            mainApp.ui = ui;
            mainApp.start(null);
            mainApp.stop();
            assertEquals(1, ui.startCount);
            assertEquals(json, FileUtil.readFromFile(filePath));
        }
    }

    @Test
    public void initModel_invalidData_changesAndExitCannotOverwriteOriginal() throws Exception {
        Path filePath = addressBookStorage.getAddressBookFilePath();
        String invalidJson = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\","
                + "\"graduationYear\":\"1999\"}]}";
        FileUtil.writeToFile(filePath, invalidJson);
        mainApp.model = initializeModel();
        mainApp.logic = new LogicManager(mainApp.model, mainApp.storage);

        for (String command : List.of("add n/Unsaved a/NUS", "list", "exit")) {
            CommandException error = assertThrows(CommandException.class, () -> mainApp.logic.execute(command));
            assertInstanceOf(DataRecoveryRequiredException.class, error.getCause());
            assertTrue(error.getMessage().contains(LogicManager.MESSAGE_RECOVERY_UNSAVED_CHANGES));
            assertFalse(error.getMessage().contains("run list to retry"));
            assertEquals(invalidJson, FileUtil.readFromFile(filePath));
        }
        assertTrue(mainApp.logic.execute("help").isShowHelp());
        assertEquals(1, mainApp.model.getAddressBook().getPersonList().size());
        assertTrue(mainApp.model.canUndoAddressBook());

        // Undo restores the initial empty model; a clean exit still leaves the rejected file intact.
        CommandException undoError = assertThrows(CommandException.class, () -> mainApp.logic.execute("undo"));
        assertFalse(undoError.getMessage().contains(LogicManager.MESSAGE_RECOVERY_UNSAVED_CHANGES));
        assertTrue(mainApp.logic.execute("exit").isExit());
        assertEquals(invalidJson, FileUtil.readFromFile(filePath));
    }

    @Test
    public void initModel_missingFile_usesSamplesWithoutCreatingDataFile() throws Exception {
        Model initializedModel = initializeModel();

        assertEquals(SampleDataUtil.getSampleAddressBook(), initializedModel.getAddressBook());
        assertFalse(Files.exists(addressBookStorage.getAddressBookFilePath()));
        new LogicManager(initializedModel, mainApp.storage).execute("list");
        assertEquals(initializedModel.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void initModel_validEmptyAddressBook_loadsEmptyWithoutUsingSamples() throws Exception {
        Path filePath = addressBookStorage.getAddressBookFilePath();
        FileUtil.writeToFile(filePath, "{\"persons\":[]}");

        Model initializedModel = initializeModel();

        assertEquals(new AddressBook(), initializedModel.getAddressBook());
        assertEquals("{\"persons\":[]}", FileUtil.readFromFile(filePath));
        new LogicManager(initializedModel, mainApp.storage).execute("list");
        assertEquals(new AddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void initPrefs_nullRoot_recoversWithDefaultPreferences() throws Exception {
        FileUtil.writeToFile(userPrefsStorage.getUserPrefsFilePath(), "null");

        UserPrefs initializedPrefs = mainApp.initPrefs(userPrefsStorage);

        assertEquals(new UserPrefs(), initializedPrefs);
        assertEquals(initializedPrefs, userPrefsStorage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_nullGuiSettings_modelInitializesWithoutChangingContacts() throws Exception {
        FileUtil.writeToFile(userPrefsStorage.getUserPrefsFilePath(), "{\"guiSettings\":null}");
        AddressBook contacts = getTypicalAddressBook();
        addressBookStorage.saveAddressBook(contacts);
        String originalJson = FileUtil.readFromFile(addressBookStorage.getAddressBookFilePath());

        UserPrefs recovered = mainApp.initPrefs(userPrefsStorage);
        Method initializer = MainApp.class.getDeclaredMethod("initModelManager",
                Storage.class, ReadOnlyUserPrefs.class);
        initializer.setAccessible(true);
        mainApp.model = assertDoesNotThrow(() -> (Model) initializer.invoke(mainApp, mainApp.storage, recovered));

        assertEquals(new GuiSettings(), mainApp.model.getGuiSettings());
        assertEquals(contacts, mainApp.model.getAddressBook());
        assertEquals(recovered, userPrefsStorage.readUserPrefs().orElseThrow());
        assertEquals(originalJson, FileUtil.readFromFile(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void stop_updatedPreferences_savesPreferencesWithoutChangingContacts() throws Exception {
        UserPrefs originalPrefs = new UserPrefs();
        originalPrefs.setGuiSettings(new GuiSettings(640, 480, 10, 20));
        userPrefsStorage.saveUserPrefs(originalPrefs);
        AddressBook storedContacts = getTypicalAddressBook();
        addressBookStorage.saveAddressBook(storedContacts);
        mainApp.model.setGuiSettings(new GuiSettings(1000, 800, 30, 40));

        mainApp.stop();

        assertEquals(mainApp.model.getUserPrefs(), userPrefsStorage.readUserPrefs().orElseThrow());
        // The model is empty, but stopping must not overwrite the existing contacts on disk.
        assertEquals(storedContacts, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void stop_storageThrowsIoException_doesNotThrowOrChangeSavedPreferences() throws Exception {
        UserPrefs originalPrefs = new UserPrefs();
        originalPrefs.setGuiSettings(new GuiSettings(640, 480, 10, 20));
        userPrefsStorage.saveUserPrefs(originalPrefs);
        FailingUserPrefsStorage failingStorage = new FailingUserPrefsStorage(userPrefsStorage.getUserPrefsFilePath());
        mainApp.storage = new StorageManager(addressBookStorage, failingStorage);
        mainApp.model.setGuiSettings(new GuiSettings(1000, 800, 30, 40));

        assertDoesNotThrow(mainApp::stop);

        assertEquals(1, failingStorage.saveCount);
        assertEquals(mainApp.model.getUserPrefs(), failingStorage.attemptedUserPrefs);
        assertEquals(originalPrefs, userPrefsStorage.readUserPrefs().orElseThrow());
    }

    /**
     * Exercises startup model initialization using only the test's isolated storage.
     */
    private Model initializeModel() throws ReflectiveOperationException {
        Method initializer = MainApp.class.getDeclaredMethod("initModelManager",
                Storage.class, ReadOnlyUserPrefs.class);
        initializer.setAccessible(true);
        return (Model) initializer.invoke(mainApp, mainApp.storage, new UserPrefs());
    }

    /**
     * Records startup requests without creating a JavaFX window.
     */
    private static class UiStub implements Ui {
        private int startCount;

        @Override
        public void start(Stage primaryStage) {
            startCount++;
        }
    }

    /**
     * Records the attempted preferences and simulates a save failure before writing to disk.
     */
    private static class FailingUserPrefsStorage extends JsonUserPrefsStorage {
        private int saveCount;
        private ReadOnlyUserPrefs attemptedUserPrefs;

        private FailingUserPrefsStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
            saveCount++;
            attemptedUserPrefs = new UserPrefs(userPrefs);
            throw new IOException("Simulated preference save failure");
        }
    }
}
