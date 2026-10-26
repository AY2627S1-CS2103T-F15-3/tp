package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullData_rejectsWholeFileWithoutChangingIt() throws IOException {
        Path filePath = testFolder.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        String validPerson = "{\"name\":\"Saved Contact\",\"address\":\"NUS\",\"tags\":[]}";
        String nullTagPerson = "{\"name\":\"Null Tag\",\"address\":\"NUS\",\"tags\":[null]}";
        for (String json : List.of("null", "{\"persons\":[null]}", "{\"persons\":[" + nullTagPerson + "]}",
                "{\"persons\":[" + validPerson + ",null]}", "{\"persons\":[null," + validPerson + "]}",
                "{\"persons\":[" + validPerson + "," + nullTagPerson + "]}", "{\"persons\":null}", "{}")) {
            FileUtil.writeToFile(filePath, json);

            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, FileUtil.readFromFile(filePath));
        }
    }

    @Test
    public void readAddressBook_nullPerson_preservesContactPositionInLoadingError() throws IOException {
        Path filePath = testFolder.resolve("null-contact.json");
        String json = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\"},null]}";
        FileUtil.writeToFile(filePath, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, IllegalValueException.class.getName() + ": Contact entry 2 is null.",
                storage::readAddressBook);
        assertEquals(json, FileUtil.readFromFile(filePath));
    }

    @Test
    public void readAddressBook_nullTag_preservesContactAndTagPositionsInLoadingError() throws IOException {
        Path filePath = testFolder.resolve("null-tag.json");
        String json = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\"},"
                + "{\"name\":\"Second Contact\",\"address\":\"NUS\",\"tags\":[\"friends\",null,\"classmates\"]}]}";
        FileUtil.writeToFile(filePath, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, IllegalValueException.class.getName()
                + ": Contact entry 2 is invalid: Tag entry 2 is null.", storage::readAddressBook);
        assertEquals(json, FileUtil.readFromFile(filePath));
    }

    @Test
    public void readAddressBook_emptyPersonsList_loadsEmptyAddressBook() throws Exception {
        Path filePath = testFolder.resolve("empty.json");
        FileUtil.writeToFile(filePath, "{\"persons\":[]}");

        assertEquals(new AddressBook(), new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
        assertEquals("{\"persons\":[]}", FileUtil.readFromFile(filePath));
    }

    @Test
    public void readAddressBook_legacyContactWithOptionalTags_loadsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("legacy.json");
        for (String tags : List.of("", ",\"tags\":null", ",\"tags\":[]")) {
            String json = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\"" + tags + "}]}";
            FileUtil.writeToFile(filePath, json);

            ReadOnlyAddressBook loaded = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();

            assertEquals(1, loaded.getPersonList().size());
            Person person = loaded.getPersonList().get(0);
            assertEquals("Saved Contact", person.getName().fullName);
            assertEquals("", person.getPhone().value);
            assertEquals("", person.getEmail().value);
            assertEquals("", person.getMajor().value);
            assertEquals("", person.getGraduationYear().value);
            assertEquals(Notes.EMPTY, person.getNotes());
            assertEquals(0, person.getTags().size());
            assertEquals(json, FileUtil.readFromFile(filePath));
        }
    }

    @Test
    public void readAndSaveAddressBook_notesUpdatedAndCleared_preservesLatestValue() throws Exception {
        Path filePath = testFolder.resolve("notes.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook addressBook = new AddressBook();
        Person person = new PersonBuilder(ALICE).withNotes("Met at \"orientation\"").build();
        addressBook.addPerson(person);

        storage.saveAddressBook(addressBook);
        assertEquals(addressBook,
                new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()));

        Person updated = person.withNotes(new Notes("Now a project teammate"));
        addressBook.setPerson(person, updated);
        storage.saveAddressBook(addressBook);
        assertEquals(addressBook,
                new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()));

        addressBook.setPerson(updated, updated.withNotes(Notes.EMPTY));
        storage.saveAddressBook(addressBook);
        assertEquals(addressBook,
                new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
