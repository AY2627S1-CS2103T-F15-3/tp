package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class LastUpdatedStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void saveAndReload_preservesDateAndUserUpdateTag() throws Exception {
        Person person = new PersonBuilder().withLastUpdated(LocalDate.of(2024, 6, 1))
                .withTags("update", "Friend").withNotes("Keep notes").withFavourite(true).build();
        AddressBook book = new AddressBook();
        book.addPerson(person);
        Path file = directory.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(book);
        assertTrue(Files.readString(file).contains("\"lastUpdated\" : \"2024-06-01\""));
        assertEquals(book, new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow()));
    }

    @Test
    public void read_legacyDate_isTodayAndBecomesStableAfterSaving() throws Exception {
        Path file = directory.resolve("legacy.json");
        Files.writeString(file, """
                {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com",
                "address":"NUS","tags":["update"]}]}
                """);
        LocalDate before = LocalDate.now();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());
        LocalDate baseline = loaded.getPersonList().get(0).getLastUpdated();
        assertTrue(baseline.equals(before) || baseline.equals(LocalDate.now()));
        storage.saveAddressBook(loaded);
        assertEquals(loaded, new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow()));
    }

    @Test
    public void read_invalidDate_rejectsInsteadOfResettingHistory() throws Exception {
        Path file = directory.resolve("invalid.json");
        for (String date : new String[]{"2025-02-30", "yesterday", ""}) {
            Files.writeString(file, """
                    {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com",
                    "address":"NUS","lastUpdated":"%s","tags":[]}]}
                    """.formatted(date));
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook());
        }
    }
}
