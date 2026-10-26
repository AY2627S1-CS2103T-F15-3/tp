package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSortComparator;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerSortTest {
    private final ModelManager model = new ModelManager();
    private final Person alice = new PersonBuilder().withName("Alice").build();
    private final Person bob = new PersonBuilder().withName("Bob").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").build();

    @Test
    public void updateSortedPersonList_changesOnlyDisplayOrder() {
        addContacts();
        AddressBook before = new AddressBook(model.getAddressBook());
        ObservableList<Person> displayed = model.getFilteredPersonList();
        assertEquals(List.of(bob, charlie, alice), displayed);
        model.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        assertEquals(List.of(alice, bob, charlie), displayed);
        model.updateSortedPersonList(new PersonSortComparator(NAME, DESC));
        assertEquals(List.of(charlie, bob, alice), displayed);
        assertSame(displayed, model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void updateSortedPersonList_preservesFilterAndSortsLaterFilterChanges() {
        addContacts();
        model.updateFilteredPersonList(person -> !person.equals(charlie));
        model.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        model.updateFilteredPersonList(person -> !person.equals(alice));
        assertEquals(List.of(bob, charlie), model.getFilteredPersonList());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
    }

    @Test
    public void sortedView_sourceMutationsKeepOrderAndNotifyObservers() {
        addContacts();
        ObservableList<Person> displayed = model.getFilteredPersonList();
        model.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        Person zara = new PersonBuilder(alice).withName("Zara").build();
        model.setPerson(alice, zara);
        assertEquals(List.of(bob, charlie, zara), displayed);
        model.deletePerson(charlie);
        assertEquals(List.of(bob, zara), displayed);
        model.addPerson(alice);
        assertEquals(List.of(alice, bob, zara), displayed);
        model.setAddressBook(new AddressBook());
        assertTrue(displayed.isEmpty());
        model.addPerson(charlie);
        model.addPerson(bob);
        assertEquals(List.of(bob, charlie), displayed);
    }

    @Test
    public void updateSortedPersonList_doesNotInvalidateRedo() {
        addContacts();
        AddressBook beforeAdd = new AddressBook(model.getAddressBook());
        Person zara = new PersonBuilder().withName("Zara").build();
        model.addPerson(zara);
        model.commitAddressBook(beforeAdd, "Added Zara");
        assertTrue(model.undoAddressBook());
        model.updateSortedPersonList(new PersonSortComparator(NAME, DESC));
        assertTrue(model.canRedoAddressBook());
        assertTrue(model.redoAddressBook());
        assertEquals(List.of(zara, charlie, bob, alice), model.getFilteredPersonList());
        assertTrue(model.undoAddressBook());
        assertEquals(List.of(charlie, bob, alice), model.getFilteredPersonList());
    }

    @Test
    public void sortedView_isUnmodifiable() {
        addContacts();
        model.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        assertThrows(UnsupportedOperationException.class, () -> model.getFilteredPersonList().remove(0));
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
    }

    @Test
    public void updateSortedPersonList_nullComparator_leavesExistingOrder() {
        addContacts();
        model.updateSortedPersonList(new PersonSortComparator(NAME, DESC));
        assertThrows(NullPointerException.class, () -> model.updateSortedPersonList(null));
        assertEquals(List.of(charlie, bob, alice), model.getFilteredPersonList());
    }

    @Test
    public void equals_differentDisplayedOrder_isDifferent() {
        addContacts();
        ModelManager other = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        model.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        assertFalse(model.equals(other));
        other.updateSortedPersonList(new PersonSortComparator(NAME, ASC));
        assertEquals(model, other);
    }

    private void addContacts() {
        model.addPerson(bob);
        model.addPerson(charlie);
        model.addPerson(alice);
    }
}
