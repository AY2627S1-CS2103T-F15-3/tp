package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Exercises real contact names through command parsing, history, search and JSON storage.
 */
public class ContactNameIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void add_realNames_surviveUndoRedoSearchAndReload() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        int fileIndex = 0;
        for (String name : List.of("Anne-Marie Tan", "Sean O'Connor", "D\u2019Arcy Tan",
                "\u5f20\u4f1f", "Jos\u00e9 Garc\u00eda", "Jose\u0301 Tan")) {
            Model model = new ModelManager();
            parser.parseCommand("add n/" + name + " a/NUS").execute(model);
            Person added = model.getFilteredPersonList().get(0);
            assertEquals(name, added.getName().fullName);
            assertTrue(model.undoAddressBook());
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertTrue(model.redoAddressBook());

            parser.parseCommand("find " + name).execute(model);
            assertEquals(List.of(added), model.getFilteredPersonList());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("names" + fileIndex++));
            storage.saveAddressBook(model.getAddressBook());
            assertEquals(List.of(added), storage.readAddressBook().orElseThrow().getPersonList());
        }
    }

    @Test
    public void edit_realName_supportsRenameAndCaseInsensitiveDuplicateCheck() throws Exception {
        Model model = new ModelManager();
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("add n/Anne a/NUS").execute(model);
        parser.parseCommand("edit 1 n/Anne-Marie O'Connor").execute(model);
        Person renamed = model.getFilteredPersonList().get(0);
        assertEquals("Anne-Marie O'Connor", renamed.getName().fullName);

        assertThrows(CommandException.class, () ->
                parser.parseCommand("add n/anne-marie o'connor a/SMU").execute(model));
        assertEquals(List.of(renamed), model.getAddressBook().getPersonList());
        assertTrue(model.undoAddressBook());
        assertEquals("Anne", model.getAddressBook().getPersonList().get(0).getName().fullName);
    }
}
