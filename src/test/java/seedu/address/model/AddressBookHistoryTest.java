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

}
