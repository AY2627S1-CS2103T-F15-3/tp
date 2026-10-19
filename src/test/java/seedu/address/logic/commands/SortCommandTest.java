package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.MAJOR;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.model.person.PersonSortComparator.SortField.YEAR;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {
    private final ModelManager model = new ModelManager();
    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Zoology")
            .withGraduationYear("2028").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Business")
            .withGraduationYear("2027").build();

    @Test
    public void execute_allSortOptions_ordersContactsAndReportsOptions() {
        model.addPerson(bob);
        model.addPerson(alice);
        assertEquals("Sorted contacts by name (asc).", new SortCommand(NAME, ASC).execute(model).getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        assertEquals("Sorted contacts by name (desc).", new SortCommand(NAME, DESC).execute(model).getFeedbackToUser());
        assertEquals(List.of(bob, alice), model.getFilteredPersonList());
        for (var field : List.of(MAJOR, YEAR)) {
            new SortCommand(field, ASC).execute(model);
            assertEquals(List.of(bob, alice), model.getFilteredPersonList());
            new SortCommand(field, DESC).execute(model);
            assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_preservesFilterContactsAndRedoHistory() {
        model.addPerson(bob);
        AddressBook beforeAdd = new AddressBook(model.getAddressBook());
        model.addPerson(alice);
        model.commitAddressBook(beforeAdd);
        model.undoAddressBook();
        model.updateFilteredPersonList(person -> false);
        AddressBook beforeSort = new AddressBook(model.getAddressBook());
        new SortCommand(NAME, ASC).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(beforeSort, model.getAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertFalse(model.canUndoAddressBook());
        model.redoAddressBook();
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_emptySingletonAndRepeatedSort_succeedsWithoutDataChanges() {
        SortCommand command = new SortCommand(NAME, ASC);
        command.execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        model.addPerson(alice);
        command.execute(model);
        command.execute(model);
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null, ASC));
        assertThrows(NullPointerException.class, () -> new SortCommand(NAME, null));
        assertThrows(NullPointerException.class, () -> new SortCommand(NAME, ASC).execute(null));
    }

    @Test
    public void equals_comparesFieldAndDirection() {
        SortCommand command = new SortCommand(NAME, ASC);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new SortCommand(NAME, ASC)));
        assertFalse(command.equals(new SortCommand(NAME, DESC)));
        assertFalse(command.equals(new SortCommand(MAJOR, ASC)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ListCommand()));
    }

    @Test
    public void toStringMethod_containsSortOptions() {
        assertEquals(SortCommand.class.getCanonicalName() + "{field=NAME, direction=DESC}",
                new SortCommand(NAME, DESC).toString());
    }
}
