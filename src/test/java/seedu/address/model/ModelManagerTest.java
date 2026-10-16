package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    /**
     * Adds {@code person} to {@code modelManager} and records the change, as an undoable command would.
     */
    private void addPersonAndCommit(Person person) {
        AddressBook previousState = new AddressBook(modelManager.getAddressBook());
        modelManager.addPerson(person);
        modelManager.commitAddressBook(previousState);
    }

    @Test
    public void constructor_newModel_hasNoUndoOrRedoHistory() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());

        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void commitAddressBook_nullPreviousState_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.commitAddressBook(null));
    }

    @Test
    public void commitAddressBook_dataChanged_createsOneUndoableState() {
        addPersonAndCommit(ALICE);

        assertTrue(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
    }

    @Test
    public void commitAddressBook_dataUnchanged_doesNotCreateUndoableState() {
        modelManager.commitAddressBook(new AddressBook(modelManager.getAddressBook()));

        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void commitAddressBook_laterModelChanges_snapshotsUnaffected() {
        addPersonAndCommit(ALICE);
        addPersonAndCommit(BENSON);
        Person editedAlice = new PersonBuilder(ALICE).withMajor("Mathematics").build();
        AddressBook previousState = new AddressBook(modelManager.getAddressBook());
        modelManager.setPerson(ALICE, editedAlice);
        modelManager.commitAddressBook(previousState);

        assertTrue(modelManager.undoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                modelManager.getAddressBook());
        assertTrue(modelManager.undoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
        assertTrue(modelManager.undoAddressBook());
        assertEquals(new AddressBook(), modelManager.getAddressBook());
        assertFalse(modelManager.canUndoAddressBook());
    }

    @Test
    public void undoAddressBook_afterCommit_restoresPreviousContacts() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());
        ReadOnlyAddressBook addressBookReference = modelManager.getAddressBook();
        ObservableList<Person> filteredListReference = modelManager.getFilteredPersonList();
        addPersonAndCommit(BENSON);

        assertTrue(modelManager.undoAddressBook());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        // The same objects are updated, so UI observers remain attached.
        assertSame(addressBookReference, modelManager.getAddressBook());
        assertSame(filteredListReference, modelManager.getFilteredPersonList());
        assertFalse(modelManager.canUndoAddressBook());
        assertTrue(modelManager.canRedoAddressBook());
    }

    @Test
    public void redoAddressBook_afterUndo_restoresSubsequentContacts() {
        addPersonAndCommit(ALICE);
        modelManager.undoAddressBook();

        assertTrue(modelManager.redoAddressBook());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertTrue(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void commitAddressBook_afterUndo_invalidatesRedoHistory() {
        addPersonAndCommit(ALICE);
        modelManager.undoAddressBook();

        addPersonAndCommit(BENSON);

        assertFalse(modelManager.canRedoAddressBook());
        assertFalse(modelManager.redoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(BENSON).build(), modelManager.getAddressBook());
    }

    @Test
    public void undoRedoAddressBook_emptyHistory_returnsFalseAndLeavesContactsUnchanged() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());

        assertFalse(modelManager.undoAddressBook());
        assertFalse(modelManager.redoAddressBook());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void undoRedoAddressBook_restoringState_doesNotCreateAdditionalHistory() {
        addPersonAndCommit(ALICE);

        modelManager.undoAddressBook();
        modelManager.redoAddressBook();
        modelManager.undoAddressBook();

        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.undoAddressBook());
        assertTrue(modelManager.redoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), modelManager.getAddressBook());
    }

    @Test
    public void undoAddressBook_afterUnrecordedDelete_doesNotRevertDelete() {
        addPersonAndCommit(ALICE);
        addPersonAndCommit(BENSON);
        modelManager.deletePerson(ALICE);

        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.undoAddressBook());
        assertEquals(new AddressBookBuilder().withPerson(BENSON).build(), modelManager.getAddressBook());
    }

    @Test
    public void redoAddressBook_afterUnrecordedClear_doesNotRevertClear() {
        addPersonAndCommit(ALICE);
        addPersonAndCommit(BENSON);
        modelManager.undoAddressBook();
        modelManager.setAddressBook(new AddressBook());

        assertFalse(modelManager.canRedoAddressBook());
        assertFalse(modelManager.redoAddressBook());
        assertFalse(modelManager.canUndoAddressBook());
        assertEquals(new AddressBook(), modelManager.getAddressBook());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
