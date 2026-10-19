package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code UndoCommand}.
 */
public class UndoCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UndoCommand().execute(null));
    }

    @Test
    public void execute_emptyHistory_failure() {
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_afterAdd_restoresContactsBeforeAdd() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        new AddCommand(newPerson).execute(model);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
        assertFalse(model.hasPerson(newPerson));
    }

    @Test
    public void execute_afterEdit_restoresAllContactFields() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withUniversityFields = new PersonBuilder(original).withMajor("Mathematics")
                .withGraduationYear("2026").withTags("classmate", "cca").build();
        model.setPerson(original, withUniversityFields);
        AddressBook stateBeforeEdit = new AddressBook(model.getAddressBook());

        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withMajor("Computer Science")
                .withGraduationYear("2027").withTags("teammate").build()).execute(model);

        Model expectedModel = new ModelManager(stateBeforeEdit, new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        Person restored = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(withUniversityFields, restored);
        assertEquals("Mathematics", restored.getMajor().value);
        assertEquals("2026", restored.getGraduationYear().value);
        assertEquals(withUniversityFields.getTags(), restored.getTags());
    }

    @Test
    public void execute_multipleUndos_restoresEachEarlierStateInTurn() throws Exception {
        AddressBook originalState = new AddressBook(model.getAddressBook());
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);

        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS,
                new ModelManager(stateAfterAdd, new UserPrefs()));
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS,
                new ModelManager(originalState, new UserPrefs()));
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterFailedAdd_failure() {
        Person existingPerson = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(existingPerson), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterFailedEdit_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, new EditPersonDescriptorBuilder(firstPerson).build()),
                model, EditCommand.MESSAGE_DUPLICATE_PERSON);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_failedCommandAfterAdd_undoesAddOnly() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        new AddCommand(newPerson).execute(model);
        assertCommandFailure(new AddCommand(newPerson), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undo_doesNotRecordAnotherChange() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);

        new UndoCommand().execute(model);

        // Had undo recorded a change, there would be a state to undo to.
        assertFalse(model.canUndoAddressBook());
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undo_preservesRedoAvailability() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        new AddCommand(newPerson).execute(model);

        new UndoCommand().execute(model);

        assertTrue(model.canRedoAddressBook());
        assertTrue(model.redoAddressBook());
        assertTrue(model.hasPerson(newPerson));
    }

    @Test
    public void execute_filteredList_showsAllRestoredContacts() throws Exception {
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new UndoCommand().execute(model);

        assertEquals(getTypicalAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterUnrecordedDelete_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }
}
