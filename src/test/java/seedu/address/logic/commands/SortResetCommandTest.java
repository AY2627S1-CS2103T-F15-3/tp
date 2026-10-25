package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SortResetCommandTest {
    private final ModelManager model = new ModelManager();
    private final Person bob = new PersonBuilder().withName("Bob").build();
    private final Person alice = new PersonBuilder().withName("Alice").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").build();

    @Test
    public void execute_filteredSortedList_restoresSavedOrderAndPreservesFilter() {
        model.addPerson(bob);
        model.addPerson(alice);
        model.addPerson(charlie);
        model.updateFilteredPersonList(person -> !person.equals(charlie));
        new SortCommand(NAME, ASC).execute(model);
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        AddressBook saved = new AddressBook(model.getAddressBook());

        assertEquals(SortResetCommand.MESSAGE_SUCCESS,
                new SortResetCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(bob, alice), model.getFilteredPersonList());
        assertEquals(saved, model.getAddressBook());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(bob, alice, charlie), model.getFilteredPersonList());
        model.deletePerson(alice);
        model.addPerson(alice);
        assertEquals(List.of(bob, charlie, alice), model.getFilteredPersonList());
    }

    @Test
    public void execute_preservesRedoAndRecordsNoUndoStep() {
        model.addPerson(bob);
        AddressBook beforeAdd = new AddressBook(model.getAddressBook());
        model.addPerson(alice);
        model.commitAddressBook(beforeAdd);
        assertTrue(model.undoAddressBook());
        new SortCommand(NAME, DESC).execute(model);
        new SortResetCommand().execute(model);
        assertTrue(model.canRedoAddressBook());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.redoAddressBook());
        assertEquals(List.of(bob, alice), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAndRepeatedReset_succeeds() {
        SortResetCommand command = new SortResetCommand();
        command.execute(model);
        command.execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertFalse(model.canUndoAddressBook());
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertEquals(command, new SortResetCommand());
        assertEquals(command.hashCode(), new SortResetCommand().hashCode());
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(null));
    }
}
