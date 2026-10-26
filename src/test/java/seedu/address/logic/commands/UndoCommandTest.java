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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
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
        String addMessage = new AddCommand(newPerson).execute(model).getFeedbackToUser();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, undoMessage(addMessage), expectedModel);
        assertFalse(model.hasPerson(newPerson));
    }

    @Test
    public void execute_afterEdit_restoresAllContactFields() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withUniversityFields = new PersonBuilder(original).withMajor("Mathematics")
                .withGraduationYear("2026").withTags("classmate", "cca").build();
        model.setPerson(original, withUniversityFields);
        AddressBook stateBeforeEdit = new AddressBook(model.getAddressBook());

        String editMessage = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withMajor("Computer Science").withGraduationYear("2027").withTags("teammate").build())
                .execute(model).getFeedbackToUser();

        Model expectedModel = new ModelManager(stateBeforeEdit, new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, undoMessage(editMessage), expectedModel);

        Person restored = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(withUniversityFields, restored);
        assertEquals("Mathematics", restored.getMajor().value);
        assertEquals("2026", restored.getGraduationYear().value);
        assertEquals(withUniversityFields.getTags(), restored.getTags());
    }

    @Test
    public void execute_multipleUndos_restoresEachEarlierStateInTurn() throws Exception {
        AddressBook originalState = new AddressBook(model.getAddressBook());
        String addMessage = new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model)
                .getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        String editMessage = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model)
                .getFeedbackToUser();

        assertCommandSuccess(new UndoCommand(), model, undoMessage(editMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
        assertCommandSuccess(new UndoCommand(), model, undoMessage(addMessage),
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
        String addMessage = new AddCommand(newPerson).execute(model).getFeedbackToUser();
        assertCommandFailure(new AddCommand(newPerson), model, AddCommand.MESSAGE_DUPLICATE_PERSON);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, undoMessage(addMessage), expectedModel);
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
    public void execute_afterUnrecordedChange_failure() throws Exception {
        new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model);
        // A change made directly through the model is not recorded, so the earlier history is stale.
        model.deletePerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterEachDataChangingCommand_undoAndRedoRestoreStates() throws Exception {
        Set<Tag> newTag = Set.of(new Tag("cca"));
        Set<Tag> existingTag = Set.of(new Tag("friends"));
        List<Command> commands = List.of(new DeleteCommand(INDEX_FIRST_PERSON), new ClearCommand(),
                new TagCommand(INDEX_FIRST_PERSON, newTag), new UntagCommand(INDEX_FIRST_PERSON, existingTag),
                new FavCommand(INDEX_FIRST_PERSON), new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met in class")));

        for (Command command : commands) {
            model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            AddressBook stateBefore = new AddressBook(model.getAddressBook());
            String commandMessage = command.execute(model).getFeedbackToUser();
            AddressBook stateAfter = new AddressBook(model.getAddressBook());

            assertCommandSuccess(new UndoCommand(), model, undoMessage(commandMessage),
                    new ModelManager(stateBefore, new UserPrefs()));
            assertCommandSuccess(new RedoCommand(), model, redoMessage(commandMessage),
                    new ModelManager(stateAfter, new UserPrefs()));
        }
    }

    @Test
    public void execute_afterUnfav_restoresFavourite() throws Exception {
        new FavCommand(INDEX_FIRST_PERSON).execute(model);
        AddressBook favouriteState = new AddressBook(model.getAddressBook());
        String unfavMessage = new UnfavCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();
        AddressBook unfavouriteState = new AddressBook(model.getAddressBook());

        assertCommandSuccess(new UndoCommand(), model, undoMessage(unfavMessage),
                new ModelManager(favouriteState, new UserPrefs()));
        assertTrue(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).isFavourite());
        assertCommandSuccess(new RedoCommand(), model, redoMessage(unfavMessage),
                new ModelManager(unfavouriteState, new UserPrefs()));
    }

    @Test
    public void execute_editThenFav_undoesEachInTurn() throws Exception {
        // Scenario from issue #134: undo after fav also reaches the earlier edit.
        AddressBook originalState = new AddressBook(model.getAddressBook());
        String editMessage = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model)
                .getFeedbackToUser();
        AddressBook stateAfterEdit = new AddressBook(model.getAddressBook());
        String favMessage = new FavCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();

        assertCommandSuccess(new UndoCommand(), model, undoMessage(favMessage),
                new ModelManager(stateAfterEdit, new UserPrefs()));
        assertCommandSuccess(new UndoCommand(), model, undoMessage(editMessage),
                new ModelManager(originalState, new UserPrefs()));
    }

    @Test
    public void execute_afterNoOpChange_undoesEarlierChange() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").withTags("Friend").build();
        String addMessage = new AddCommand(newPerson).execute(model).getFeedbackToUser();
        // Tagging with a tag the person already has changes nothing, so no undo step is recorded.
        Index alexIndex = Index.fromZeroBased(model.getFilteredPersonList().indexOf(newPerson));
        new TagCommand(alexIndex, newPerson.getTags()).execute(model);

        assertCommandSuccess(new UndoCommand(), model, undoMessage(addMessage),
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_clearOnEmptyAddressBook_recordsNoChange() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        new ClearCommand().execute(model);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_clearOnEmptyAddressBookAfterUndo_preservesRedo() throws Exception {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        String addMessage = new AddCommand(new PersonBuilder().withName("Alex Tan").build()).execute(model)
                .getFeedbackToUser();
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());
        new UndoCommand().execute(model);

        // Clearing the already-empty list changes nothing, so the undone add can still be redone.
        new ClearCommand().execute(model);

        assertCommandSuccess(new RedoCommand(), model, redoMessage(addMessage),
                new ModelManager(stateAfterAdd, new UserPrefs()));
    }

    @Test
    public void execute_favThenTag_namesEachUndoneChange() throws Exception {
        // Steps from issue #165: the message did not say whether the tag or the favourite was reversed.
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new FavCommand(INDEX_FIRST_PERSON).execute(model);
        Person favouritedPerson = findByName(firstPerson);
        AddressBook stateAfterFav = new AddressBook(model.getAddressBook());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        new TagCommand(INDEX_SECOND_PERSON, Set.of(new Tag("Friend"))).execute(model);
        Person taggedPerson = findByName(secondPerson);

        assertCommandSuccess(new UndoCommand(), model,
                "Undid: " + String.format(TagCommand.MESSAGE_SUCCESS, Messages.format(taggedPerson)),
                new ModelManager(stateAfterFav, new UserPrefs()));
        assertCommandSuccess(new UndoCommand(), model,
                "Undid: " + String.format(FavCommand.MESSAGE_SUCCESS, Messages.format(favouritedPerson)),
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
        assertCommandSuccess(new RedoCommand(), model,
                "Redid: " + String.format(FavCommand.MESSAGE_SUCCESS, Messages.format(favouritedPerson)),
                new ModelManager(stateAfterFav, new UserPrefs()));
    }

    @Test
    public void execute_afterDelete_namesDeletedPerson() throws Exception {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        String expectedMessage = "Undid: "
                + String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete));
        assertCommandSuccess(new UndoCommand(), model, expectedMessage,
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    /**
     * Returns the contact in the model with the same name as {@code person}.
     */
    private Person findByName(Person person) {
        return model.getAddressBook().getPersonList().stream()
                .filter(p -> p.getName().equals(person.getName())).findFirst().orElseThrow();
    }

    private static String undoMessage(String changeDescription) {
        return String.format(UndoCommand.MESSAGE_SUCCESS, changeDescription);
    }

    private static String redoMessage(String changeDescription) {
        return String.format(RedoCommand.MESSAGE_SUCCESS, changeDescription);
    }
}
