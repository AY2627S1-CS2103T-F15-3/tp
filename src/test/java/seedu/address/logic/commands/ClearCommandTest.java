package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_ALREADY_EMPTY, expectedModel);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filteredList_clearsAllContactsAndOmitsHintFromHistory() throws Exception {
        AddressBook original = getTypicalAddressBook();
        Model model = new ModelManager(original, new UserPrefs());
        model.updateFilteredPersonList(ALICE::equals);

        CommandResult cleared = new ClearCommand().execute(model);

        assertEquals("All contacts have been cleared. Use undo to restore them.", cleared.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());

        CommandResult undone = new UndoCommand().execute(model);
        assertEquals("Undid: All contacts have been cleared.", undone.getFeedbackToUser());
        assertEquals(original, model.getAddressBook());

        CommandResult redone = new RedoCommand().execute(model);
        assertEquals("Redid: All contacts have been cleared.", redone.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

}
