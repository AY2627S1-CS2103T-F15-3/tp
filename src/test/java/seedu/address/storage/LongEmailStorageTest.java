package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;

public class LongEmailStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_longEmail_preservesFullValueAcrossAddEditAndReload() throws Exception {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        StorageManager storage = new StorageManager(contacts,
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        String localEmail = "a.".repeat(5000) + "a@example.com";
        logic.execute("add n/Long Email a/NUS e/" + localEmail);
        assertEquals(localEmail, model.getFilteredPersonList().get(0).getEmail().value);
        assertEquals(model.getAddressBook(), new AddressBook(contacts.readAddressBook().orElseThrow()));

        String domainEmail = "a@" + "a.".repeat(5000) + "com";
        logic.execute("edit 1 e/" + domainEmail);
        assertEquals(domainEmail, contacts.readAddressBook().orElseThrow().getPersonList().get(0).getEmail().value);
        assertEquals(model.getAddressBook(), new AddressBook(contacts.readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_invalidLongEmail_preservesExistingContacts() throws Exception {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        StorageManager storage = new StorageManager(contacts,
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/Existing Contact a/NUS e/test@example.com");
        AddressBook original = new AddressBook(model.getAddressBook());
        String invalidEmail = "a.".repeat(5000) + "@example.com";
        assertThrows(ParseException.class, () -> logic.execute("add n/Invalid Email a/NUS e/" + invalidEmail));
        assertThrows(ParseException.class, () -> logic.execute("edit 1 e/" + invalidEmail));
        assertEquals(original, model.getAddressBook());
        assertEquals(original, new AddressBook(contacts.readAddressBook().orElseThrow()));
    }
}
