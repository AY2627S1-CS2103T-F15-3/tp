package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks advisory warnings for newly shared contact methods when editing existing contacts.
 */
public class EditCommandSharedDetailsTest {
    private final Person alice = new PersonBuilder().withName("Alice").withPhone("91112222")
            .withEmail("alice@example.com").build();
    private final Person bob = new PersonBuilder().withName("Bob").withPhone("93334444")
            .withEmail("bob@example.com").withNotes("Study together").withFavourite(true).build();
    private final Person carol = new PersonBuilder().withName("Carol").withPhone("95556666")
            .withEmail("carol@example.com").build();

    private Model model;

    @BeforeEach
    public void setUp() {
        model = modelWith(alice, bob, carol);
    }

    @Test
    public void execute_changedPhone_warnsWithoutRejectingOrChangingOtherContacts() throws Exception {
        CommandResult result = edit(new EditPersonDescriptorBuilder().withPhone(alice.getPhone().value).build());

        assertWarning("#1 Alice (phone)", result);
        assertEquals(new PersonBuilder(bob).withPhone(alice.getPhone().value).build(),
                model.getAddressBook().getPersonList().get(1));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertEquals(carol, model.getAddressBook().getPersonList().get(2));
    }

    @Test
    public void execute_changedEmail_matchesIgnoringCaseAndPreservesEnteredCase() throws Exception {
        CommandResult result = edit(new EditPersonDescriptorBuilder().withEmail("ALICE@EXAMPLE.COM").build());

        assertWarning("#1 Alice (email)", result);
        assertEquals("ALICE@EXAMPLE.COM", model.getAddressBook().getPersonList().get(1).getEmail().value);
    }

    @Test
    public void execute_bothMethodsChangeToSameContact_listsMatchOnceWithBothReasons() throws Exception {
        CommandResult result = edit(new EditPersonDescriptorBuilder()
                .withPhone(alice.getPhone().value).withEmail(alice.getEmail().value).build());

        assertWarning("#1 Alice (phone and email)", result);
    }

    @Test
    public void execute_phoneAndEmailMatchDifferentContacts_listsAllMatches() throws Exception {
        CommandResult result = edit(new EditPersonDescriptorBuilder()
                .withPhone(alice.getPhone().value).withEmail(carol.getEmail().value).build());

        assertWarning("#1 Alice (phone); #3 Carol (email)", result);
    }

    @Test
    public void execute_multipleContactsShareChangedPhone_listsEveryMatchingContact() throws Exception {
        Person sharedCarol = new PersonBuilder(carol).withPhone(alice.getPhone().value).build();
        model = modelWith(alice, bob, sharedCarol);

        CommandResult result = edit(new EditPersonDescriptorBuilder().withPhone(alice.getPhone().value).build());

        assertWarning("#1 Alice (phone); #3 Carol (phone)", result);
    }

    @Test
    public void execute_existingSharedDetailsWithUnchangedOrUnrelatedEdits_doesNotWarn() throws Exception {
        Person sharedBob = new PersonBuilder(bob).withPhone(alice.getPhone().value)
                .withEmail(alice.getEmail().value).build();
        List<EditPersonDescriptor> descriptors = List.of(
                new EditPersonDescriptorBuilder().withPhone(alice.getPhone().value).build(),
                new EditPersonDescriptorBuilder().withEmail(alice.getEmail().value).build(),
                new EditPersonDescriptorBuilder().withEmail("ALICE@EXAMPLE.COM").build(),
                new EditPersonDescriptorBuilder().withName("Bobby").build(),
                new EditPersonDescriptorBuilder().withAddress("NUS").build(),
                new EditPersonDescriptorBuilder().withMajor("Mathematics").build());

        for (EditPersonDescriptor descriptor : descriptors) {
            model = modelWith(alice, sharedBob, carol);

            assertOrdinaryEdit(edit(descriptor));
        }
    }

    @Test
    public void execute_tagOnlyEditWithExistingSharedDetails_reportsTagChangesWithoutWarning() throws Exception {
        Person sharedBob = new PersonBuilder(bob).withPhone(alice.getPhone().value)
                .withEmail(alice.getEmail().value).withTags("Keep", "Old").build();
        model = modelWith(alice, sharedBob, carol);

        CommandResult result = edit(new EditPersonDescriptorBuilder().withTags("Keep", "Project").build());

        Person edited = new PersonBuilder(sharedBob).withTags("Keep", "Project").build();
        String description = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertEquals(description + "\nTags replaced. Removed: Old. Added: Project.", result.getFeedbackToUser());
        assertFalse(result.isWarning());
        assertEquals(edited, model.getAddressBook().getPersonList().get(1));
        assertEquals(description, model.getUndoDescription().orElseThrow());
    }

    @Test
    public void execute_sharedPhoneAndClearedTags_keepsRemovalSummaryBeforeWarning() throws Exception {
        model = modelWith(alice, new PersonBuilder(bob).withTags("Old", "Keep").build(), carol);

        CommandResult result = edit(new EditPersonDescriptorBuilder()
                .withPhone(alice.getPhone().value).withTags().build());

        Person edited = new PersonBuilder(bob).withPhone(alice.getPhone().value).withTags().build();
        String description = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertEquals(description + "\nTags replaced. Removed: Keep, Old. Added: None."
                + "\nWarning - duplicate details detected: #1 Alice (phone)", result.getFeedbackToUser());
        assertTrue(result.isWarning());
        assertEquals(edited, model.getAddressBook().getPersonList().get(1));
        assertEquals(description, model.getUndoDescription().orElseThrow());
    }

    @Test
    public void execute_sharedPhoneAndUnchangedTags_keepsNoneSummaryBeforeWarning() throws Exception {
        Person taggedBob = new PersonBuilder(bob).withTags("Keep").build();
        model = modelWith(alice, taggedBob, carol);

        CommandResult result = edit(new EditPersonDescriptorBuilder()
                .withPhone(alice.getPhone().value).withTags("Keep").build());

        Person edited = new PersonBuilder(taggedBob).withPhone(alice.getPhone().value).build();
        String description = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertEquals(description + "\nTags replaced. Removed: None. Added: None."
                + "\nWarning - duplicate details detected: #1 Alice (phone)", result.getFeedbackToUser());
        assertTrue(result.isWarning());
        assertEquals(edited, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void execute_onlyOneMethodChanges_doesNotReportOtherUnchangedSharedMethod() throws Exception {
        Person sharedBob = new PersonBuilder(bob).withPhone(alice.getPhone().value)
                .withEmail(alice.getEmail().value).build();
        model = modelWith(alice, sharedBob, carol);

        CommandResult phoneResult = edit(new EditPersonDescriptorBuilder().withPhone(carol.getPhone().value).build());

        assertWarning("#3 Carol (phone)", phoneResult);

        model = modelWith(alice, sharedBob, carol);
        CommandResult emailResult = edit(new EditPersonDescriptorBuilder().withEmail(carol.getEmail().value).build());

        assertWarning("#3 Carol (email)", emailResult);
    }

    @Test
    public void execute_clearedOrDistinctContactMethods_doesNotWarnAboutBlanksOrItself() throws Exception {
        Person empty = new PersonBuilder().withName("No Details").withPhone("").withEmail("").build();
        List<EditPersonDescriptor> descriptors = List.of(
                new EditPersonDescriptorBuilder().withPhone("").build(),
                new EditPersonDescriptorBuilder().withEmail("").build(),
                new EditPersonDescriptorBuilder().withPhone("").withEmail("").build(),
                new EditPersonDescriptorBuilder().withPhone("911122220").withEmail("alice+new@example.com").build());

        for (EditPersonDescriptor descriptor : descriptors) {
            model = modelWith(alice, bob, carol, empty);

            assertOrdinaryEdit(edit(descriptor));
        }
    }

    @Test
    public void execute_duplicateNameWithSharedDetails_stillRejectsWithoutChangingDataOrHistory() {
        EditCommand command = new EditCommand(INDEX_SECOND_PERSON, new EditPersonDescriptorBuilder()
                .withName(alice.getName().fullName).withPhone(alice.getPhone().value)
                .withEmail(alice.getEmail().value).build());

        assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_filteredSortedEdit_combinesTagSummaryAndWarningAndPersistsOneUndoStep(@TempDir Path directory)
            throws Exception {
        Person original = new PersonBuilder(bob).withTags("Old").build();
        model = modelWith(alice, original, carol);
        AddressBook before = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("sort name asc");
        logic.execute("find Bob");
        assertEquals(List.of(original), model.getFilteredPersonList());

        CommandResult result = logic.execute("edit 1 n/Aaron p/91112222 e/carol@example.com t/Project");

        Person edited = new PersonBuilder(bob).withName("Aaron").withPhone(alice.getPhone().value)
                .withEmail(carol.getEmail().value).withTags("Project").build();
        String description = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertEquals(description + "\nTags replaced. Removed: Old. Added: Project."
                + "\nWarning - duplicate details detected: #2 Alice (phone); #3 Carol (email)",
                result.getFeedbackToUser());
        assertTrue(result.isWarning());
        assertEquals(description, result.getLogSummary());
        assertEquals(List.of(edited, alice, carol), model.getFilteredPersonList());
        assertEquals(List.of(alice, edited, carol), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(description, model.getUndoDescription().orElseThrow());

        CommandResult undo = logic.execute("undo");
        assertEquals("Undid: " + description, undo.getFeedbackToUser());
        assertFalse(undo.isWarning());
        assertEquals(before, model.getAddressBook());
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertFalse(model.canUndoAddressBook());

        CommandResult redo = logic.execute("redo");
        assertEquals("Redid: " + description, redo.getFeedbackToUser());
        assertFalse(redo.isWarning());
        assertEquals(List.of(edited, alice, carol), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertFalse(model.canRedoAddressBook());
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(alice, edited, carol), reloaded.getAddressBook().getPersonList());
    }

    @Test
    public void execute_unchangedSharedPhoneAfterUndo_preservesHistoryAndSavedData(@TempDir Path directory)
            throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("edit 2 p/91112222");
        logic.execute("edit 2 e/carol@example.com");
        Person latestBob = model.getAddressBook().getPersonList().get(1);
        logic.execute("undo");
        String undoBefore = model.getUndoDescription().orElseThrow();
        String redoBefore = model.getRedoDescription().orElseThrow();
        AddressBook before = new AddressBook(model.getAddressBook());
        String fileBefore = FileUtil.readFromFile(storage.getAddressBookFilePath());

        CommandResult result = logic.execute("edit 2 p/91112222");

        assertOrdinaryEdit(result);
        assertEquals(before, model.getAddressBook());
        assertEquals(fileBefore, FileUtil.readFromFile(storage.getAddressBookFilePath()));
        assertEquals(undoBefore, model.getUndoDescription().orElseThrow());
        assertEquals(redoBefore, model.getRedoDescription().orElseThrow());
        logic.execute("redo");
        assertEquals(latestBob, model.getAddressBook().getPersonList().get(1));
    }

    private CommandResult edit(EditPersonDescriptor descriptor) throws Exception {
        return new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
    }

    private void assertOrdinaryEdit(CommandResult result) {
        Person edited = model.getAddressBook().getPersonList().get(1);
        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited)),
                result.getFeedbackToUser());
        assertFalse(result.isWarning());
    }

    private void assertWarning(String matches, CommandResult result) {
        Person edited = model.getAddressBook().getPersonList().get(1);
        String success = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        assertEquals(success + "\nWarning - duplicate details detected: " + matches, result.getFeedbackToUser());
        assertTrue(result.isWarning());
    }

    private static Model modelWith(Person... persons) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(persons));
        return new ModelManager(addressBook, new UserPrefs());
    }
}
