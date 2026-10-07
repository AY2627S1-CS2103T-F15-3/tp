package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks that command parsing, contact updates and persistence preserve university fields together.
 */
public class UniversityFieldsIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void addEditAndReload_universityFieldsAndTagsPreserved() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        logic.execute("add n/Alex Tan p/91234567 e/alex@example.com a/Kent Ridge"
                + " m/Computer Science c/2027 t/Classmate t/Friend t/Friend");
        Person added = model.getFilteredPersonList().get(0);
        assertEquals(new Major("Computer Science"), added.getMajor());
        assertEquals(new GraduationYear("2027"), added.getGraduationYear());
        assertEquals(Set.of(new Tag("Classmate"), new Tag("Friend")), added.getTags());
        assertEquals(added, storage.readAddressBook().orElseThrow().getPersonList().get(0));

        // Existing edits must not erase the new fields, including when tags are replaced.
        logic.execute("edit 1 p/98765432 t/Teammate");
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals("98765432", edited.getPhone().value);
        assertEquals(added.getMajor(), edited.getMajor());
        assertEquals(added.getGraduationYear(), edited.getGraduationYear());
        assertEquals(Set.of(new Tag("Teammate")), edited.getTags());
        assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().get(0));

        // A different major/year does not bypass the existing duplicate-name rule.
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, ()
            -> logic.execute("add n/Alex Tan p/91234567 e/alex@example.com a/Kent Ridge m/Business c/2028"));
        assertEquals(1, model.getFilteredPersonList().size());
    }
}
