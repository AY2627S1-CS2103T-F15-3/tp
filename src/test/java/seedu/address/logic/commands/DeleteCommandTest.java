package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_noIndices_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                "At least one target index is required.", () -> new DeleteCommand());
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_adjacentIndicesInReverseOrder_deletesOriginalContacts() {
        List<Person> original = List.copyOf(model.getFilteredPersonList());
        DeleteCommand command = new DeleteCommand(Index.fromOneBased(3), INDEX_SECOND_PERSON, INDEX_FIRST_PERSON);
        String expectedMessage = "Deleted 3 contacts:\n3. " + Messages.format(original.get(2))
                + "\n2. " + Messages.format(original.get(1)) + "\n1. " + Messages.format(original.get(0));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        original.subList(0, 3).forEach(expectedModel::deletePerson);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_repeatedIndex_rejectsWholeBatch() {
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, Index.fromOneBased(1));

        assertCommandFailure(command, model,
                "Index 1 is repeated. Specify each displayed index only once. No contacts were deleted.");
    }

    @Test
    public void execute_validThenInvalidIndex_rejectsWholeBatch() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, invalidIndex);

        assertCommandFailure(command, model, String.format(DeleteCommand.MESSAGE_INVALID_INDEX,
                invalidIndex.getOneBased(), model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_validIndexWithAcademicFields_successMessageShowsAcademicFields() {
        Person personToDelete = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("91234567")
                .withEmail("alex@example.com")
                .withAddress("10 Kent Ridge Road")
                .withMajor("Computer Science")
                .withGraduationYear("2027")
                .withTags("classmate")
                .build();
        Model academicModel = new ModelManager();
        academicModel.addPerson(personToDelete);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedDeletedPerson = "Alex Tan"
                + "; Phone: 91234567"
                + "; Email: alex@example.com"
                + "; Address: 10 Kent Ridge Road"
                + "; Major: Computer Science"
                + "; Class year: 2027"
                + "; Tags: [classmate]";
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, expectedDeletedPerson);

        Model expectedModel = new ModelManager(academicModel.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, academicModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_INVALID_INDEX,
                outOfBoundIndex.getOneBased(), model.getFilteredPersonList().size());
        assertCommandFailure(deleteCommand, model, expectedMessage);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model,
                "Index 2 is outside the current displayed list. The only valid index is 1.");
    }

    @Test
    public void execute_emptyAddressBook_suggestsAddingContact() {
        Model emptyModel = new ModelManager();

        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), emptyModel, DeleteCommand.MESSAGE_NO_CONTACTS);
    }

    @Test
    public void execute_emptyFilteredList_suggestsListingSavedContacts() {
        showNoPerson(model);

        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), model,
                DeleteCommand.MESSAGE_NO_DISPLAYED_CONTACTS);
    }

    @Test
    public void execute_emptyAddressBookAfterUndo_preservesRedoHistory() throws Exception {
        Model emptyModel = new ModelManager();
        new AddCommand(new PersonBuilder().withName("HistoryOnly").build()).execute(emptyModel);
        AddressBook afterAdd = new AddressBook(emptyModel.getAddressBook());
        new UndoCommand().execute(emptyModel);
        assertTrue(emptyModel.getFilteredPersonList().isEmpty());
        assertFalse(emptyModel.canUndoAddressBook());
        assertTrue(emptyModel.canRedoAddressBook());

        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), emptyModel, DeleteCommand.MESSAGE_NO_CONTACTS);

        assertFalse(emptyModel.canUndoAddressBook());
        assertTrue(emptyModel.canRedoAddressBook());
        new RedoCommand().execute(emptyModel);
        assertEquals(afterAdd, emptyModel.getAddressBook());
        assertEquals(afterAdd.getPersonList(), emptyModel.getFilteredPersonList());
    }

    @Test
    public void execute_emptyFilteredList_preservesUndoAndRedoHistory() throws Exception {
        new AddCommand(new PersonBuilder().withName("HistoryFirst").build()).execute(model);
        AddressBook beforeSecondAdd = new AddressBook(model.getAddressBook());
        new AddCommand(new PersonBuilder().withName("HistorySecond").build()).execute(model);
        AddressBook afterSecondAdd = new AddressBook(model.getAddressBook());
        new UndoCommand().execute(model);
        showNoPerson(model);
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());

        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), model,
                DeleteCommand.MESSAGE_NO_DISPLAYED_CONTACTS);

        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        new RedoCommand().execute(model);
        assertEquals(afterSecondAdd, model.getAddressBook());
        assertEquals(afterSecondAdd.getPersonList(), model.getFilteredPersonList());
        new UndoCommand().execute(model);
        assertEquals(beforeSecondAdd, model.getAddressBook());
    }

    @Test
    public void execute_largeInvalidIndex_reportsSuppliedIndexAndDisplayedRange() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DeleteCommand(Index.fromOneBased(Integer.MAX_VALUE)), model,
                "Index 2147483647 is outside the current displayed list. The only valid index is 1.");
    }

    @Test
    public void execute_invalidIndex_preservesUndoAndRedoHistory() throws Exception {
        new AddCommand(new PersonBuilder().withName("HistoryFirst").build()).execute(model);
        AddressBook beforeSecondAdd = new AddressBook(model.getAddressBook());
        new AddCommand(new PersonBuilder().withName("HistorySecond").build()).execute(model);
        AddressBook afterSecondAdd = new AddressBook(model.getAddressBook());
        assertTrue(model.undoAddressBook());
        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DeleteCommand(INDEX_SECOND_PERSON), model,
                "Index 2 is outside the current displayed list. The only valid index is 1.");

        assertTrue(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertTrue(model.redoAddressBook());
        assertEquals(afterSecondAdd, model.getAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(beforeSecondAdd, model.getAddressBook());
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));

        // multiple indices retain their order because feedback follows the input order
        DeleteCommand deleteBothCommand = new DeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);
        assertEquals(deleteBothCommand, new DeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
        assertFalse(deleteBothCommand.equals(deleteFirstCommand));
        assertFalse(deleteBothCommand.equals(new DeleteCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON)));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndices=" + List.of(targetIndex) + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
