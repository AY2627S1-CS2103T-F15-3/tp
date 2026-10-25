package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_nullPerson_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(Collections.singletonList(null));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_PERSON, data::toModelType);
    }

    @Test
    public void toModelType_validPersonThenNull_rejectsWholeAddressBook() {
        List<JsonAdaptedPerson> persons = new ArrayList<>();
        persons.add(new JsonAdaptedPerson(new PersonBuilder().build()));
        persons.add(null);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(persons);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_PERSON, data::toModelType);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_NULL_PERSON, data::toModelType);
    }

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

}
