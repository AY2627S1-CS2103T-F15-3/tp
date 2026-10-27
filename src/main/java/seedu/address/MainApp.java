package seedu.address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.4";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "unimate.json");
    private static final String BACKUP_SUFFIX = ".bak";
    private static final String MESSAGE_DATA_FILE_NOT_LOADED = "Warning: the data file %1$s could not be loaded, "
            + "so UniMate has started with an empty contact list.\nReason: %2$s\n%3$s\n"
            + "Your next command will save over %1$s. To repair the file instead, close UniMate now "
            + "without running a command, fix the file, then restart.";

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    /** A warning to show the user once the window opens, such as a data file that could not be loaded. */
    protected Optional<String> startupWarning = Optional.empty();

    @Override
    public void init() throws Exception {
        logger.info("===============================[ Initializing UniMate ]=============================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath(), startupWarning);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample UniMate contact list will be used instead if {@code storage}'s data file is not found,
     * or an empty contact list will be used instead if errors occur when reading {@code storage}'s data file.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with sample UniMate contacts.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty UniMate contact list.");
            initialData = new AddressBook();
            startupWarning = Optional.of(describeLoadingFailure(storage.getAddressBookFilePath(), e));
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Backs up the unreadable data file, since the next command saves over it, and returns a warning for the user.
     */
    private String describeLoadingFailure(Path dataFile, DataLoadingException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        String reason = String.valueOf(cause.getMessage()).lines().findFirst().orElse("");
        Path backupFile = dataFile.resolveSibling(dataFile.getFileName() + BACKUP_SUFFIX);
        String backupNote;
        try {
            Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            backupNote = "A copy of the unreadable file has been saved as " + backupFile + ".";
        } catch (IOException ioe) {
            logger.warning("Failed to back up data file: " + StringUtil.getDetails(ioe));
            backupNote = "UniMate could not make a backup copy of it.";
        }
        return String.format(MESSAGE_DATA_FILE_NOT_LOADED, dataFile, reason, backupNote);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UniMate " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================== [ Stopping UniMate ] ===============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
