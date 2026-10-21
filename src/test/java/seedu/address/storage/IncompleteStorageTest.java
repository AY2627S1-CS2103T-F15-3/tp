package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class IncompleteStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void roundTrip_emptyContactFields_preservesEveryOtherField() throws Exception {
        Person person = new PersonBuilder().withPhone("").withEmail("").withFavourite(true)
                .withLastUpdated(LocalDate.of(2024, 10, 1)).withNotes("Keep notes")
                .withMajor("Computing").withGraduationYear("2027").withTags("incomplete", "Friend").build();
        AddressBook book = new AddressBook();
        book.addPerson(person);
        Path path = directory.resolve("contacts.json");
        new JsonAddressBookStorage(path).saveAddressBook(book);
        assertEquals(book, new AddressBook(new JsonAddressBookStorage(path).readAddressBook().orElseThrow()));
        assertFalse(Files.readString(path).contains("system-tag"));
        assertTrue(person.isIncomplete());
    }

    @Test
    public void legacyMissingOrNullFields_loadAsUnspecified() throws Exception {
        Path path = directory.resolve("legacy.json");
        for (String fields : new String[]{"", ",\"phone\":null,\"email\":null", ",\"phone\":\"\",\"email\":\"\""}) {
            Files.writeString(path, """
                    {"persons":[{"name":"Alice","address":"NUS","tags":[]%s}]}
                    """.formatted(fields));
            Person loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList().get(0);
            assertTrue(loaded.isIncomplete());
            assertEquals("", loaded.getPhone().value);
            assertEquals("", loaded.getEmail().value);
        }
    }
}
