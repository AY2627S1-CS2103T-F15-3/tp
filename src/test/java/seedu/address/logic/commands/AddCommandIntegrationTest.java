package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
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
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_DISPLAYED, 1, Messages.format(personInList)));
    }

    @Test
    public void execute_duplicateInSortedFilteredView_reportsExistingDetailsAtDisplayedIndex() {
        new SortCommand(SortField.NAME, SortDirection.DESC).execute(model);
        model.updateFilteredPersonList(person -> person.equals(ALICE) || person.equals(BENSON));
        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
        Person duplicate = new PersonBuilder(ALICE).withName("alice pauline")
                .withPhone("88888888").withAddress("Different address").build();

        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON
                + "\nExisting contact at displayed index 2:\n" + Messages.format(ALICE));

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_hiddenDuplicate_reportsSavedIndexAndInstructionsWithoutChangingView() {
        new SortCommand(SortField.NAME, SortDirection.DESC).execute(model);
        model.updateFilteredPersonList(person -> person.equals(BENSON));

        assertCommandFailure(new AddCommand(ALICE), model, AddCommand.MESSAGE_DUPLICATE_PERSON
                + "\nExisting contact is hidden by the current filter (saved index 1):\n" + Messages.format(ALICE)
                + "\nRun list, then sort reset to view this contact at index 1.");

        new ListCommand().execute(model);
        assertEquals(ALICE, model.getFilteredPersonList().get(model.getFilteredPersonList().size() - 1));
        new SortResetCommand().execute(model);
        assertEquals(ALICE, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_duplicateWithEmptyView_reportsExistingSavedContact() {
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new AddCommand(ALICE), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_HIDDEN, 1, Messages.format(ALICE)));

        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_duplicateThroughLogic_preservesDataFileAndHistory(@TempDir Path directory) throws Exception {
        assertHiddenDuplicatePreservesDataFileAndHistory("find Benson", directory);
    }

    @Test
    public void execute_duplicateAfterFilter_preservesDataFileAndHistory(@TempDir Path directory) throws Exception {
        assertHiddenDuplicatePreservesDataFileAndHistory("filter t/owesMoney", directory);
    }

    @Test
    public void execute_duplicateAfterListFav_preservesDataFileAndHistory(@TempDir Path directory) throws Exception {
        assertHiddenDuplicatePreservesDataFileAndHistory("listfav", directory);
    }

    @Test
    public void execute_sharedPhone_addsContactAndIdentifiesExistingMatch() throws Exception {
        Person toAdd = new PersonBuilder().withName("Shared Phone").withPhone(ALICE.getPhone().value)
                .withEmail("new@example.com").build();

        CommandResult result = new AddCommand(toAdd).execute(model);

        assertWarning(toAdd, "#1 Alice Pauline (phone)", result);
        assertTrue(model.hasPerson(toAdd));
    }

    @Test
    public void execute_sharedEmailIgnoringCase_addsContactAndIdentifiesExistingMatch() throws Exception {
        Person toAdd = new PersonBuilder().withName("Shared Email").withPhone("99998888")
                .withEmail("ALICE@EXAMPLE.COM").build();

        CommandResult result = new AddCommand(toAdd).execute(model);

        assertWarning(toAdd, "#1 Alice Pauline (email)", result);
        assertEquals("ALICE@EXAMPLE.COM", model.getAddressBook().getPersonList().getLast().getEmail().value);
    }

    @Test
    public void execute_bothFieldsShared_listsExistingContactOnceWithBothReasons() throws Exception {
        Person toAdd = new PersonBuilder(ALICE).withName("Another Alice").build();

        CommandResult result = new AddCommand(toAdd).execute(model);

        assertWarning(toAdd, "#1 Alice Pauline (phone and email)", result);
    }

    @Test
    public void execute_phoneAndEmailMatchDifferentContacts_listsEveryMatch() throws Exception {
        Person toAdd = new PersonBuilder().withName("Shared Details").withPhone(ALICE.getPhone().value)
                .withEmail(BENSON.getEmail().value).build();

        CommandResult result = new AddCommand(toAdd).execute(model);

        assertWarning(toAdd, "#1 Alice Pauline (phone); #2 Benson Meier (email)", result);
    }

    @Test
    public void execute_blankOrDifferentContactDetails_doesNotWarn() throws Exception {
        // Each row contains the existing phone/email followed by the new phone/email.
        for (String[] details : new String[][]{{"", "", "", ""},
            {"", "one@example.com", "", "two@example.com"}, {"1111", "", "2222", ""},
            {"", "", "3333", "new@example.com"}, {"4444", "old@example.com", "", ""},
            {"94351253", "alice@example.com", "943512530", "alice+new@example.com"}}) {
            Person existing = new PersonBuilder().withName("Existing").withPhone(details[0])
                    .withEmail(details[1]).build();
            Person toAdd = new PersonBuilder().withName("Added").withPhone(details[2]).withEmail(details[3]).build();
            AddressBook addressBook = new AddressBook();
            addressBook.addPerson(existing);
            Model separateModel = new ModelManager(addressBook, new UserPrefs());

            CommandResult result = new AddCommand(toAdd).execute(separateModel);

            assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(toAdd)), result.getFeedbackToUser());
            assertFalse(result.isWarning());
            assertEquals(List.of(existing, toAdd), separateModel.getAddressBook().getPersonList());
        }
    }

    @Test
    public void execute_warningAfterFilteredSortedAdd_usesRefreshedIndicesAndPersists(@TempDir Path directory)
            throws Exception {
        Person zara = new PersonBuilder().withName("Zara").withPhone("12341234")
                .withEmail("shared@example.com").build();
        Person charlie = new PersonBuilder().withName("Charlie").withPhone("99990000")
                .withEmail("charlie@example.com").build();
        Person amy = new PersonBuilder().withName("Amy").withPhone("12341234")
                .withEmail("shared@example.com").withAddress("NUS").build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(zara, charlie));
        model = new ModelManager(addressBook, new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("sort name asc");
        logic.execute("find Charlie");
        assertEquals(List.of(charlie), model.getFilteredPersonList());

        CommandResult result = logic.execute("add n/Amy p/12341234 e/shared@example.com a/NUS");

        assertWarning(amy, "#3 Zara (phone and email)", result);
        assertEquals(List.of(amy, charlie, zara), model.getFilteredPersonList());
        assertEquals(List.of(zara, charlie, amy), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        String successMessage = String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(amy));
        assertEquals(successMessage, model.getUndoDescription().orElseThrow());

        assertEquals("Undid: " + successMessage, logic.execute("undo").getFeedbackToUser());
        assertEquals(addressBook, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals("Redid: " + successMessage, logic.execute("redo").getFeedbackToUser());
        assertEquals(List.of(amy, charlie, zara), model.getFilteredPersonList());
        assertFalse(model.canRedoAddressBook());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(zara, charlie, amy), reloaded.getFilteredPersonList());
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

        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_DISPLAYED, 1, Messages.format(personInList)));

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_duplicatePersonAfterAdd_keepsExistingHistoryOnly() throws Exception {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Person validPerson = new PersonBuilder().build();
        new AddCommand(validPerson).execute(model);

        String expectedMessage = String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_DISPLAYED,
                model.getFilteredPersonList().size(), Messages.format(validPerson));
        assertCommandFailure(new AddCommand(validPerson), model, expectedMessage);

        assertTrue(model.undoAddressBook());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_deleteAfterAdd_undoesDeleteThenAdd() throws Exception {
        AddressBook originalState = new AddressBook(model.getAddressBook());
        Person newPerson = new PersonBuilder().build();
        new AddCommand(newPerson).execute(model);
        AddressBook stateAfterAdd = new AddressBook(model.getAddressBook());

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        // Delete is undoable, so each undo reverts one change, most recent first.
        assertTrue(model.undoAddressBook());
        assertEquals(stateAfterAdd, model.getAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(originalState, model.getAddressBook());
        assertFalse(model.hasPerson(newPerson));
    }

    private static void assertWarning(Person addedPerson, String matches, CommandResult result) {
        String success = String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(addedPerson));
        String advisory = "Warning - duplicate details detected: " + matches;
        assertEquals(success + "\n" + advisory, result.getFeedbackToUser());
        assertTrue(result.isWarning());
    }

    /**
     * Checks hidden-duplicate feedback and preservation through a parsed view command and persisted history.
     */
    private void assertHiddenDuplicatePreservesDataFileAndHistory(String viewCommand, Path directory) throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/HistoryFirstAdd a/NUS");
        logic.execute("add n/HistorySecondAdd a/NUS");
        AddressBook afterSecondAdd = new AddressBook(model.getAddressBook());
        logic.execute("undo");
        logic.execute(viewCommand);
        logic.execute("sort name desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String statusBefore = model.viewStatusProperty().get();
        String savedBefore = FileUtil.readFromFile(storage.getAddressBookFilePath());
        String undoDescription = model.getUndoDescription().orElseThrow();
        String redoDescription = model.getRedoDescription().orElseThrow();
        String expectedMessage = String.format(AddCommand.MESSAGE_DUPLICATE_PERSON_HIDDEN, 1, Messages.format(ALICE));

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute(
                "add n/alice pauline p/88888888 e/new@example.com a/Different address m/Law c/2029"));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(statusBefore, model.viewStatusProperty().get());
        assertEquals(savedBefore, FileUtil.readFromFile(storage.getAddressBookFilePath()));
        assertEquals(undoDescription, model.getUndoDescription().orElseThrow());
        assertEquals(redoDescription, model.getRedoDescription().orElseThrow());
        logic.execute("redo");
        assertEquals(afterSecondAdd, model.getAddressBook());
        assertEquals(afterSecondAdd, storage.readAddressBook().orElseThrow());
    }

}
