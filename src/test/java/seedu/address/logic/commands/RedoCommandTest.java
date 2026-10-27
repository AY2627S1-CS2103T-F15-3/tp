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

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code RedoCommand}.
 * Redo history is prepared with {@link Model#undoAddressBook()} directly, so these tests do not depend on
 * the {@code undo} command.
 */
public class RedoCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RedoCommand().execute(null));
    }

    @Test
    public void execute_emptyHistory_failure() {
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_noUndoneChange_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undoneAdd_restoresAddedPerson() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        String addMessage = new AddCommand(newPerson).execute(model).getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        model.undoAddressBook();

        assertCommandSuccess(new RedoCommand(), model, redoMessage(addMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
        assertTrue(model.hasPerson(newPerson));
    }

    @Test
    public void execute_undoneEdit_restoresEditedContactFields() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withPhone(VALID_PHONE_BOB).withMajor("Computer Science")
                .withGraduationYear("2027").withTags("teammate", "cca").build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone(VALID_PHONE_BOB).withMajor("Computer Science").withGraduationYear("2027")
                .withTags("teammate", "cca").build()).execute(model);
        String editMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        AddressBook stateAfterEdit = new AddressBook(model.getAddressBook());
        model.undoAddressBook();

        assertCommandSuccess(new RedoCommand(), model, redoMessage(editMessage),
                new ModelManager(stateAfterEdit, new UserPrefs()));

        Person restored = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(editedPerson, restored);
        assertEquals(VALID_PHONE_BOB, restored.getPhone().value);
        assertEquals("Computer Science", restored.getMajor().value);
        assertEquals("2027", restored.getGraduationYear().value);
        assertEquals(editedPerson.getTags(), restored.getTags());
    }

    @Test
    public void execute_multipleRedos_restoresEachLaterStateInTurn() throws Exception {
        String addMessage = new AddCommand(new PersonBuilder().withName("Alex Tan").withMajor("Mathematics")
                .build()).execute(model).getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        String editMessage = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGraduationYear("2026").build()).execute(model)
                .getFeedbackToUser();
        AddressBook stateAfterEdit = new AddressBook(model.getAddressBook());
        model.undoAddressBook();
        model.undoAddressBook();

        assertCommandSuccess(new RedoCommand(), model, redoMessage(addMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
        assertCommandSuccess(new RedoCommand(), model, redoMessage(editMessage),
                new ModelManager(stateAfterEdit, new UserPrefs()));
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_newAddAfterUndo_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        model.undoAddressBook();

        new AddCommand(new PersonBuilder().withName("Bella Lim").build()).execute(model);

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_newEditAfterUndo_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        model.undoAddressBook();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_failedAddAfterUndo_redoStillAvailable() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        String addMessage = new AddCommand(newPerson).execute(model).getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        model.undoAddressBook();

        Person existingPerson = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(existingPerson), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_DISPLAYED, 1, Messages.format(existingPerson)));

        assertCommandSuccess(new RedoCommand(), model, redoMessage(addMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
    }

    @Test
    public void execute_failedEditAfterUndo_redoStillAvailable() throws Exception {
        String addMessage = new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model)
                .getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        model.undoAddressBook();

        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, new EditPersonDescriptorBuilder(firstPerson).build()),
                model, EditCommand.MESSAGE_DUPLICATE_PERSON);

        assertCommandSuccess(new RedoCommand(), model, redoMessage(addMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
    }

    @Test
    public void execute_redo_doesNotCreateDuplicateHistoryEntries() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        model.undoAddressBook();

        new RedoCommand().execute(model);

        assertFalse(model.canRedoAddressBook());
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
        // Exactly one change is recorded, so a single undo empties the undo history.
        assertTrue(model.undoAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_redo_preservesUndoAvailability() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        model.undoAddressBook();

        new RedoCommand().execute(model);

        assertTrue(model.canUndoAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_filteredList_showsAllRestoredContacts() throws Exception {
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);
        AddressBook stateAfterEdit = new AddressBook(model.getAddressBook());
        model.undoAddressBook();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new RedoCommand().execute(model);

        assertEquals(stateAfterEdit.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterUnrecordedDelete_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        model.undoAddressBook();
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undoneClear_namesClear() throws Exception {
        new ClearCommand().execute(model);
        model.undoAddressBook();

        assertCommandSuccess(new RedoCommand(), model, redoMessage(ClearCommand.MESSAGE_SUCCESS),
                new ModelManager(new AddressBook(), new UserPrefs()));
    }

    private static String redoMessage(String changeDescription) {
        return "Redid: " + changeDescription;
    }
}
