package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
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
