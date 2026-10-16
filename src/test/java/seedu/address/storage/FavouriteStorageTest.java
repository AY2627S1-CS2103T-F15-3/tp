package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class FavouriteStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void execute_unfav_savesFalseAndReloadsWithoutLosingOtherFields() throws Exception {
        Person original = new PersonBuilder().withFavourite(true).withTags("Friend")
                .withMajor("Computing").withGraduationYear("2027").build();
        Model model = new ModelManager();
        model.addPerson(original);
        Path file = directory.resolve("unfav.json");
        Storage storage = new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("unfav 1");
        assertTrue(Files.readString(file).contains("\"favourite\" : false"));
        assertEquals(new PersonBuilder(original).withFavourite(false).build(),
                new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().get(0));
        logic.execute("fav 1");
        assertEquals(original, new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().get(0));
    }

    @Test
    public void saveAndReload_favouriteAndOrdinaryContacts_preservesState() throws Exception {
        Person favourite = new PersonBuilder().withFavourite(true).withMajor("Computing")
                .withGraduationYear("2027").withTags("NOC").build();
        Person ordinary = new PersonBuilder().withName("Bob").build();
        AddressBook book = new AddressBook();
        book.addPerson(favourite);
        book.addPerson(ordinary);
        Path file = directory.resolve("contacts.json");
        new JsonAddressBookStorage(file).saveAddressBook(book);
        assertTrue(Files.readString(file).contains("\"favourite\" : true"));
        assertEquals(book, new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow()));
    }

    @Test
    public void read_legacyJsonWithoutFavourite_defaultsToFalse() throws Exception {
        Path file = directory.resolve("legacy.json");
        Files.writeString(file, """
                {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com",
                "address":"NUS","tags":["Friend"]}]}
                """);
        Person person = new JsonAddressBookStorage(file).readAddressBook().orElseThrow().getPersonList().get(0);
        assertFalse(person.isFavourite());
        assertEquals(new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("alice@example.com").withAddress("NUS").withTags("Friend").build(), person);
    }
}
