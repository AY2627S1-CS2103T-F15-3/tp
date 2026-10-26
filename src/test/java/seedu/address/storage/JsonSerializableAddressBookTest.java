package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.Major;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path UNICODE_SPACES_FILE = TEST_DATA_FOLDER.resolve("unicodeSpacesAddressBook.json");

    @Test
    public void toModelType_caseOnlyDuplicateNames_rejectedWithoutChangingSource() {
        Person original = new PersonBuilder().withName("John Doe").build();
        Person duplicate = new PersonBuilder(original).withName("john doe").withPhone("98765432").build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedPerson(original), new JsonAdaptedPerson(duplicate)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
        assertEquals("John Doe", original.getName().fullName);
        assertEquals("john doe", duplicate.getName().fullName);
        // Conversion remains invalid; no automatic deletion or renaming is performed.
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_fileSavedBeforeSpaceNormalisation_loadsAsCommandInputWould() throws Exception {
        // Files saved before #141 can contain Unicode spaces that commands now convert to ASCII spaces.
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(UNICODE_SPACES_FILE,
                JsonSerializableAddressBook.class).get();
        Person loaded = dataFromFile.toModelType().getPersonList().get(0);

        assertEquals("Blk 456, Clementi Ave 3", loaded.getAddress().value);
        assertEquals("Computer Science", loaded.getMajor().value);
        assertEquals("Met\u00a0at\u2003orientation", loaded.getNotes().value);
        assertTrue(new ContactFilterPredicate(Optional.of(new Major("Computer Science")), Optional.empty(),
                Optional.empty()).test(loaded));
    }

}
