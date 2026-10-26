package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies adding favourites through parsing, history, favourites views and saved data.
 */
public class AddFavouriteIntegrationTest {
    @TempDir
    public Path directory;

    private final Person ordinary = new PersonBuilder().withName("Ordinary").build();
    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(ordinary);
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_addFavourite_persistsAndCreatesOneUndoableChange() throws Exception {
        LocalDate before = LocalDate.now();
        logic.execute("add n/Alice p/91234567 e/alice@example.com a/NUS m/Computing c/2027 f/ d/1");
        Person added = model.getAddressBook().getPersonList().get(1);

        assertTrue(added.isFavourite());
        assertTrue(added.getLastUpdated().equals(before) || added.getLastUpdated().equals(LocalDate.now()));
        assertEquals(new PersonBuilder().withName("Alice").withPhone("91234567").withEmail("alice@example.com")
                .withAddress("NUS").withMajor("Computing").withGraduationYear("2027").withTags("Friend")
                .withFavourite(true).withLastUpdated(added.getLastUpdated()).build(), added);
        assertEquals(List.of(ordinary, added), storage.readAddressBook().orElseThrow().getPersonList());

        logic.execute("listfav");
        assertEquals(List.of(added), model.getFilteredPersonList());
        assertEquals(List.of(ordinary, added), model.getAddressBook().getPersonList());

        Model reopened = new ModelManager(new AddressBook(storage.readAddressBook().orElseThrow()), new UserPrefs());
        new ListFavCommand().execute(reopened);
        assertEquals(List.of(added), reopened.getFilteredPersonList());

        logic.execute("undo");
        assertEquals(List.of(ordinary), model.getAddressBook().getPersonList());
        assertEquals(List.of(ordinary), storage.readAddressBook().orElseThrow().getPersonList());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());

        logic.execute("redo");
        assertEquals(List.of(ordinary, added), model.getAddressBook().getPersonList());
        assertEquals(List.of(ordinary, added), storage.readAddressBook().orElseThrow().getPersonList());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_addWithoutFlag_remainsNotFavourite() throws Exception {
        logic.execute("add n/Alice a/NUS");
        Person added = model.getAddressBook().getPersonList().get(1);

        assertFalse(added.isFavourite());
        assertEquals(List.of(ordinary, added), storage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("listfav");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_invalidFlagOrDuplicateContact_preservesDataAndHistory() {
        for (String input : List.of("add n/Alice a/NUS f/true", "add n/Alice a/NUS f/ f/")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
        }
        assertThrows(CommandException.class, () -> logic.execute("add n/ordinary a/NUS f/"));

        assertEquals(List.of(ordinary), model.getAddressBook().getPersonList());
        assertFalse(ordinary.isFavourite());
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }
}
