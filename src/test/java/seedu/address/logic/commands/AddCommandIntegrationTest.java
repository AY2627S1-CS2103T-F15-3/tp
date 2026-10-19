package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_newPerson_createsOneUndoableState() throws Exception {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Person validPerson = new PersonBuilder().build();

        new AddCommand(validPerson).execute(model);

        assertTrue(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertFalse(model.hasPerson(validPerson));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_multipleNewPersons_retainsIndependentSnapshots() throws Exception {
        AddressBook stateBeforeFirstAdd = new AddressBook(model.getAddressBook());
        Person firstPerson = new PersonBuilder().withName("Alex Tan").build();
        Person secondPerson = new PersonBuilder().withName("Bella Lim").build();

        new AddCommand(firstPerson).execute(model);
        AddressBook stateBeforeSecondAdd = new AddressBook(model.getAddressBook());
        new AddCommand(secondPerson).execute(model);
        AddressBook stateAfterSecondAdd = new AddressBook(model.getAddressBook());

        assertTrue(model.undoAddressBook());
        assertEquals(stateBeforeSecondAdd, model.getAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(stateBeforeFirstAdd, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());

        assertTrue(model.redoAddressBook());
        assertEquals(stateBeforeSecondAdd, model.getAddressBook());
        assertTrue(model.redoAddressBook());
        assertEquals(stateAfterSecondAdd, model.getAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_newPersonWithUniversityFields_fieldsPreservedThroughUndoRedo() throws Exception {
        Person validPerson = new PersonBuilder().withName("Alex Tan").withMajor("Computer Science")
                .withGraduationYear("2027").withTags("classmate").build();

        new AddCommand(validPerson).execute(model);
        model.undoAddressBook();
        model.redoAddressBook();

        Person restoredPerson = model.getAddressBook().getPersonList().stream()
                .filter(validPerson::isSamePerson).findFirst().orElseThrow();
        assertEquals(validPerson, restoredPerson);
        assertEquals("Computer Science", restoredPerson.getMajor().value);
        assertEquals("2027", restoredPerson.getGraduationYear().value);
    }

    @Test
    public void execute_newPersonAfterUndo_invalidatesRedoHistory() throws Exception {
        Person firstPerson = new PersonBuilder().withName("Alex Tan").build();
        Person secondPerson = new PersonBuilder().withName("Bella Lim").build();
        new AddCommand(firstPerson).execute(model);
        model.undoAddressBook();

        new AddCommand(secondPerson).execute(model);

        assertFalse(model.canRedoAddressBook());
        assertFalse(model.redoAddressBook());
        assertTrue(model.hasPerson(secondPerson));
        assertFalse(model.hasPerson(firstPerson));
    }

    @Test
    public void execute_duplicatePerson_doesNotCreateHistoryEntry() {
        Person personInList = model.getAddressBook().getPersonList().get(0);

        assertCommandFailure(new AddCommand(personInList), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_duplicatePersonAfterAdd_keepsExistingHistoryOnly() throws Exception {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Person validPerson = new PersonBuilder().build();
        new AddCommand(validPerson).execute(model);

        assertCommandFailure(new AddCommand(validPerson), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        assertTrue(model.undoAddressBook());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_deleteAfterAdd_addCannotBeUndone() throws Exception {
        new AddCommand(new PersonBuilder().build()).execute(model);

        // Delete is not undoable, so undoing the earlier add would silently revert the delete too.
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.undoAddressBook());
    }

}
