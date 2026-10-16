package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class AddressBookHistoryTest {

    private AddressBook addressBook;
    private AddressBookHistory history;

    @BeforeEach
    public void setUp() {
        addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        history = new AddressBookHistory(addressBook);
    }

    /**
     * Adds {@code person} to the tracked address book and records the change.
     */
    private void addAndCommit(Person person) {
        AddressBook previousState = new AddressBook(addressBook);
        addressBook.addPerson(person);
        history.commit(previousState);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddressBookHistory(null));
    }

    @Test
    public void constructor_newHistory_hasNoUndoOrRedoStates() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void commit_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.commit(null));
    }

    @Test
    public void commit_dataChanged_createsOneUndoableState() {
        addAndCommit(BENSON);

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());

        assertTrue(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
        assertFalse(history.canUndo());
    }

    @Test
    public void commit_dataUnchanged_doesNotCreateUndoableState() {
        history.commit(new AddressBook(addressBook));

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void commit_previousStateModifiedAfterCommit_snapshotUnaffected() {
        AddressBook previousState = new AddressBook(addressBook);
        addressBook.addPerson(BENSON);
        history.commit(previousState);

        // Mutating the object passed in must not change the stored snapshot.
        previousState.addPerson(CARL);

        assertTrue(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
    }

    @Test
    public void undoRedo_multipleCommits_restoresEachIndependentSnapshot() {
        AddressBook stateZero = new AddressBook(addressBook);
        addAndCommit(BENSON);
        AddressBook stateOne = new AddressBook(addressBook);
        addAndCommit(CARL);
        AddressBook stateTwo = new AddressBook(addressBook);

        assertTrue(history.undo());
        assertEquals(stateOne, addressBook);
        assertTrue(history.undo());
        assertEquals(stateZero, addressBook);
        assertFalse(history.canUndo());

        assertTrue(history.redo());
        assertEquals(stateOne, addressBook);
        assertTrue(history.redo());
        assertEquals(stateTwo, addressBook);
        assertFalse(history.canRedo());
    }

    @Test
    public void undo_restoresPreviousStateInPlace() {
        addAndCommit(BENSON);
        ObservableList<Person> personList = addressBook.getPersonList();

        assertTrue(history.undo());

        assertTrue(personList == addressBook.getPersonList());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    public void redo_restoresUndoneState() {
        addAndCommit(BENSON);
        history.undo();

        assertTrue(history.redo());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(), addressBook);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void undoRedo_restoringState_doesNotCreateAdditionalStates() {
        addAndCommit(BENSON);

        history.undo();
        history.redo();
        history.undo();

        // Exactly one undo entry existed, so after the final undo nothing further can be undone.
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
        assertFalse(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
    }

    @Test
    public void commit_afterUndo_clearsRedoStates() {
        addAndCommit(BENSON);
        history.undo();

        addAndCommit(CARL);

        assertFalse(history.canRedo());
        assertFalse(history.redo());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).withPerson(CARL).build(), addressBook);

        assertTrue(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
        assertFalse(history.canUndo());
    }

    @Test
    public void undo_emptyHistory_returnsFalseAndLeavesDataUnchanged() {
        assertFalse(history.undo());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void redo_emptyHistory_returnsFalseAndLeavesDataUnchanged() {
        assertFalse(history.redo());

        assertEquals(new AddressBookBuilder().withPerson(ALICE).build(), addressBook);
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void undo_afterUnrecordedChange_discardsStaleHistory() {
        addAndCommit(BENSON);
        addressBook.removePerson(ALICE); // e.g. a delete, which is not recorded

        assertFalse(history.canUndo());
        assertFalse(history.undo());

        // The unrecorded change is not silently reverted.
        assertEquals(new AddressBookBuilder().withPerson(BENSON).build(), addressBook);
        assertFalse(history.canRedo());
    }

    @Test
    public void redo_afterUnrecordedChange_discardsStaleHistory() {
        addAndCommit(BENSON);
        history.undo();
        Person editedAlice = new PersonBuilder(ALICE).withTags("unrecorded").build();
        addressBook.setPerson(ALICE, editedAlice); // e.g. a tag, which is not recorded

        assertFalse(history.canRedo());
        assertFalse(history.redo());

        assertEquals(new AddressBookBuilder().withPerson(editedAlice).build(), addressBook);
        assertFalse(history.canUndo());
    }

    @Test
    public void commit_afterUnrecordedChange_discardsStaleUndoStates() {
        addAndCommit(BENSON);
        addressBook.removePerson(ALICE); // unrecorded change

        addAndCommit(CARL);

        assertTrue(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(BENSON).build(), addressBook);
        // The snapshot from before the unrecorded change is gone.
        assertFalse(history.canUndo());
        assertFalse(history.undo());
        assertEquals(new AddressBookBuilder().withPerson(BENSON).build(), addressBook);
    }

}
