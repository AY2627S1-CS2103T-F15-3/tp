package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.UserPrefs;

public class JsonUserPrefsStorageTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonUserPrefsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readUserPrefs_nullGuiSettings_usesDefaultsWithoutChangingFile() throws Exception {
        Path path = testFolder.resolve("preferences.json");
        String json = "{\"guiSettings\":null}";
        Files.writeString(path, json);

        UserPrefs loaded = new JsonUserPrefsStorage(path).readUserPrefs(path).orElseThrow();

        assertEquals(new GuiSettings(), loaded.getGuiSettings());
        assertEquals(json, Files.readString(path));
    }

    @Test
    public void readUserPrefs_missingGuiSettings_usesDefaultsWithoutChangingFile() throws Exception {
        Path path = testFolder.resolve("preferences.json");
        Files.writeString(path, "{}");

        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(path).readUserPrefs().orElseThrow());
        assertEquals("{}", Files.readString(path));
    }

    @Test
    public void readUserPrefs_validGuiSettings_preservesSizeAndPosition() throws Exception {
        Path path = testFolder.resolve("preferences.json");
        String json = "{\"guiSettings\":{\"windowWidth\":1000,\"windowHeight\":500,"
                + "\"windowCoordinates\":{\"x\":300,\"y\":100}}}";
        Files.writeString(path, json);

        assertEquals(new GuiSettings(1000, 500, 300, 100),
                new JsonUserPrefsStorage(path).readUserPrefs().orElseThrow().getGuiSettings());
        assertEquals(json, Files.readString(path));
    }

    @Test
    public void saveUserPrefs_recoveredGuiSettings_persistsDefaults() throws Exception {
        Path path = testFolder.resolve("preferences.json");
        Files.writeString(path, "{\"guiSettings\":null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        UserPrefs recovered = storage.readUserPrefs().orElseThrow();

        storage.saveUserPrefs(recovered);

        // Inspect raw deserialization too, so read-time recovery cannot mask a null saved value.
        UserPrefs saved = JsonUtil.fromJsonString(Files.readString(path), UserPrefs.class);
        assertEquals(new GuiSettings(), saved.getGuiSettings());
        assertEquals(recovered, storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void readUserPrefs_malformedJsonOrNullRoot_stillRejected() throws Exception {
        Path path = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        for (String json : List.of("not JSON", "{\"guiSettings\":", "null")) {
            Files.writeString(path, json);
            assertThrows(DataLoadingException.class, () -> storage.readUserPrefs());
            assertEquals(json, Files.readString(path));
        }
    }

    @Test
    public void readUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readUserPrefs(null));
    }

    private Optional<UserPrefs> readUserPrefs(String userPrefsFileInTestDataFolder) throws DataLoadingException {
        Path prefsFilePath = addToTestDataPathIfNotNull(userPrefsFileInTestDataFolder);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs(prefsFilePath);
    }

    @Test
    public void readUserPrefs_missingFile_emptyResult() throws DataLoadingException {
        assertFalse(readUserPrefs("NonExistentFile.json").isPresent());
    }

    @Test
    public void readUserPrefs_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NotJsonFormatUserPrefs.json"));
    }

    private Path addToTestDataPathIfNotNull(String userPrefsFileInTestDataFolder) {
        return userPrefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(userPrefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void readUserPrefs_fileInOrder_successfullyRead() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("TypicalUserPref.json").get();
        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_valuesMissingFromFile_defaultValuesUsed() throws DataLoadingException {
        UserPrefs actual = readUserPrefs("EmptyUserPrefs.json").get();
        assertEquals(new UserPrefs(), actual);
    }

    @Test
    public void readUserPrefs_extraValuesInFile_extraValuesIgnored() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("ExtraValuesUserPref.json").get();

        assertEquals(expected, actual);
    }

    private UserPrefs getTypicalUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 300, 100));
        return userPrefs;
    }

    @Test
    public void savePrefs_nullPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(null, "SomeFile.json"));
    }

    @Test
    public void saveUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(new UserPrefs(), null));
    }

    /**
     * Saves {@code userPrefs} at the specified {@code prefsFileInTestDataFolder} filepath.
     */
    private void saveUserPrefs(UserPrefs userPrefs, String prefsFileInTestDataFolder) {
        try {
            new JsonUserPrefsStorage(addToTestDataPathIfNotNull(prefsFileInTestDataFolder))
                    .saveUserPrefs(userPrefs);
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file", ioe);
        }
    }

    @Test
    public void saveUserPrefs_allInOrder_success() throws DataLoadingException, IOException {

        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(1200, 200, 0, 2));

        Path pefsFilePath = testFolder.resolve("TempPrefs.json");
        JsonUserPrefsStorage jsonUserPrefsStorage = new JsonUserPrefsStorage(pefsFilePath);

        //Try writing when the file doesn't exist
        jsonUserPrefsStorage.saveUserPrefs(original);
        UserPrefs readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);

        //Try saving when the file exists
        original.setGuiSettings(new GuiSettings(5, 5, 5, 5));
        jsonUserPrefsStorage.saveUserPrefs(original);
        readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);
    }

}
