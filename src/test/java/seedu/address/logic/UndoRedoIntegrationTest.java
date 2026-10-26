package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FavCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that data-changing commands, {@code undo} and {@code redo} work together through command parsing,
 * the model's history and persistence.
 */
public class UndoRedoIntegrationTest {
    private static final String ADD_ALEX = "add n/Alex Tan p/91234567 e/alex@example.com a/Kent Ridge Hall"
            + " m/Computer Science c/2027 t/Friend t/CCA";
    private static final Person ALEX = new PersonBuilder().withName("Alex Tan").withPhone("91234567")
            .withEmail("alex@example.com").withAddress("Kent Ridge Hall").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Friend", "CCA").build();

    private static final String ADD_BETH = "add n/Beth Lim p/92345678 e/beth@example.com a/Pasir Panjang"
            + " m/Business c/2026";
    private static final Person BETH = new PersonBuilder().withName("Beth Lim").withPhone("92345678")
            .withEmail("beth@example.com").withAddress("Pasir Panjang").withMajor("Business")
            .withGraduationYear("2026").build();

    private static final String ADD_CHEN = "add n/Chen Wei p/93456789 e/chen@example.com a/Tampines"
            + " m/Mathematics c/2028 t/Teammate";
    private static final Person CHEN = new PersonBuilder().withName("Chen Wei").withPhone("93456789")
            .withEmail("chen@example.com").withAddress("Tampines").withMajor("Mathematics")
            .withGraduationYear("2028").withTags("Teammate").build();

    // Changes every editable field of ALICE, the first contact in the typical address book.
    private static final String EDIT_ALICE = "edit 1 n/Alice Tan p/81234567 e/alice.tan@example.com"
            + " a/Utown Residence m/Economics c/2026 t/Classmate";
    private static final Person EDITED_ALICE = new PersonBuilder(ALICE).withName("Alice Tan")
            .withPhone("81234567").withEmail("alice.tan@example.com").withAddress("Utown Residence")
            .withMajor("Economics").withGraduationYear("2026").withTags("Classmate").build();

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;
    private final List<String> mixedChangeFeedback = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        // Saves the starting contacts so that every check can compare the model with the data file.
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void addThenUndo_removesAddedContact() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void addUndoRedo_restoresAddedContact() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(typicalPersonsWith(ALEX));
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void editTagsUndoRedo_feedbackExcludesTagSummary() throws Exception {
        Person edited = new PersonBuilder(ALICE).withTags("Project").build();
        List<Person> editedContacts = replace(getTypicalPersons(), ALICE, edited);
        String description = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));

        assertCommandSuccess("edit 1 t/Project",
                description + "\nTags replaced. Removed: friends. Added: Project.", editedContacts);
        assertEquals(description, model.getUndoDescription().orElseThrow());
        assertUndoSuccess(description, getTypicalPersons());
        assertEquals(description, model.getRedoDescription().orElseThrow());
        assertRedoSuccess(description, editedContacts);

        // Clearing tags uses the same concise history description, without the removal summary.
        Person cleared = new PersonBuilder(edited).withTags().build();
        List<Person> clearedContacts = replace(editedContacts, edited, cleared);
        String clearDescription = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(cleared));
        assertCommandSuccess("edit 1 t/",
                clearDescription + "\nTags replaced. Removed: Project. Added: None.", clearedContacts);
        assertEquals(clearDescription, model.getUndoDescription().orElseThrow());
        assertUndoSuccess(clearDescription, editedContacts);
        assertEquals(clearDescription, model.getRedoDescription().orElseThrow());
        assertRedoSuccess(clearDescription, clearedContacts);
    }

    @Test
    public void editThenUndo_restoresOriginalContact() throws Exception {
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, replace(getTypicalPersons(), ALICE, EDITED_ALICE));
        assertUndoSuccess(getTypicalPersons());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void editUndoRedo_restoresEditedContact() throws Exception {
        List<Person> afterEdit = replace(getTypicalPersons(), ALICE, EDITED_ALICE);
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, afterEdit);
        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(afterEdit);
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void addEditUndoUndoRedoRedo_correctContactsAfterEveryStep() throws Exception {
        List<Person> afterAdd = typicalPersonsWith(ALEX);
        Person editedAlex = new PersonBuilder(ALEX).withPhone("98887777").withGraduationYear("2028")
                .withTags("Mentor").build();
        List<Person> afterEdit = typicalPersonsWith(editedAlex);

        assertAddSuccess(ADD_ALEX, ALEX, afterAdd);
        assertEditSuccess("edit 8 p/98887777 c/2028 t/Mentor", editedAlex, afterEdit);
        assertUndoSuccess(afterAdd);
        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(afterAdd);
        assertRedoSuccess(afterEdit);
    }

    @Test
    public void multipleAddsThenUndos_removesContactsInReverseOrder() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertAddSuccess(ADD_BETH, BETH, typicalPersonsWith(ALEX, BETH));
        assertAddSuccess(ADD_CHEN, CHEN, typicalPersonsWith(ALEX, BETH, CHEN));

        assertUndoSuccess(typicalPersonsWith(ALEX, BETH));
        assertUndoSuccess(typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void multipleUndosThenRedos_reappliesChangesInOriginalOrder() throws Exception {
        List<Person> afterAddAlex = typicalPersonsWith(ALEX);
        List<Person> afterEditAlice = replace(afterAddAlex, ALICE, EDITED_ALICE);
        List<Person> afterAddBeth = new ArrayList<>(afterEditAlice);
        afterAddBeth.add(BETH);

        assertAddSuccess(ADD_ALEX, ALEX, afterAddAlex);
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, afterEditAlice);
        assertAddSuccess(ADD_BETH, BETH, afterAddBeth);
        assertUndoSuccess(afterEditAlice);
        assertUndoSuccess(afterAddAlex);
        assertUndoSuccess(getTypicalPersons());

        assertRedoSuccess(afterAddAlex);
        assertRedoSuccess(afterEditAlice);
        assertRedoSuccess(afterAddBeth);
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void newAddAfterUndo_redoUnavailable() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertAddSuccess(ADD_BETH, BETH, typicalPersonsWith(BETH));

        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
        // The new add replaced the undone one as the latest change.
        assertUndoSuccess(getTypicalPersons());
    }

    @Test
    public void newEditAfterUndo_redoUnavailable() throws Exception {
        List<Person> afterEdit = replace(getTypicalPersons(), ALICE, EDITED_ALICE);
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, afterEdit);

        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
        assertUndoSuccess(getTypicalPersons());
    }

    @Test
    public void undoRedoUndo_movesConsistentlyBetweenStates() throws Exception {
        List<Person> afterEdit = replace(getTypicalPersons(), ALICE, EDITED_ALICE);
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, afterEdit);

        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(afterEdit);
        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(afterEdit);

        assertTrue(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void failedAddAfterUndo_redoStillAvailable() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());

        // Rejected while executing: a contact with this name already exists.
        assertCommandFailure("add n/Alice Pauline p/99990000 e/other@example.com a/Clementi",
                CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON);
        // Rejected while parsing: the phone, email and address are missing.
        assertCommandFailure("add n/Beth Lim", ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        assertRedoSuccess(typicalPersonsWith(ALEX));
    }

    @Test
    public void failedEditAfterUndo_redoStillAvailable() throws Exception {
        List<Person> afterEdit = replace(getTypicalPersons(), ALICE, EDITED_ALICE);
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, afterEdit);
        assertUndoSuccess(getTypicalPersons());

        assertCommandFailure("edit 2 n/Alice Pauline", CommandException.class,
                EditCommand.MESSAGE_DUPLICATE_PERSON);
        assertCommandFailure("edit 99 p/91234567", CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure("edit 1", ParseException.class, EditCommand.MESSAGE_NOT_EDITED);

        assertRedoSuccess(afterEdit);
    }

    @Test
    public void undoWithEmptyHistory_failsAndLeavesContactsUnchanged() throws Exception {
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, UndoCommand.MESSAGE_FAILURE);
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void redoWithEmptyHistory_failsAndLeavesContactsUnchanged() throws Exception {
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);

        // A change that has not been undone cannot be redone either.
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
        assertUndoSuccess(getTypicalPersons());
    }

    @Test
    public void undoRedoWithArguments_rejectedWithoutChangingHistory() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertAddSuccess(ADD_BETH, BETH, typicalPersonsWith(ALEX, BETH));
        assertUndoSuccess(typicalPersonsWith(ALEX));

        assertCommandFailure("undo 1", ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE));
        assertCommandFailure("redo 1", ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE));

        // Exactly one change is still undoable and exactly one is still redoable.
        assertRedoSuccess(typicalPersonsWith(ALEX, BETH));
        assertUndoSuccess(typicalPersonsWith(ALEX));
        assertUndoSuccess(getTypicalPersons());
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void undoRedo_restoresEveryContactField() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        Person editedAlex = new PersonBuilder().withName("Alexander Tan").withPhone("87654321")
                .withEmail("alexander@example.com").withAddress("Bukit Timah").withMajor("Data Science")
                .withGraduationYear("2029").withTags("Mentor").build();
        assertEditSuccess("edit 8 n/Alexander Tan p/87654321 e/alexander@example.com a/Bukit Timah"
                + " m/Data Science c/2029 t/Mentor", editedAlex, typicalPersonsWith(editedAlex));

        assertUndoSuccess(typicalPersonsWith(ALEX));
        Person restored = model.getFilteredPersonList().get(7);
        assertEquals(new Name("Alex Tan"), restored.getName());
        assertEquals(new Phone("91234567"), restored.getPhone());
        assertEquals(new Email("alex@example.com"), restored.getEmail());
        assertEquals(new Address("Kent Ridge Hall"), restored.getAddress());
        assertEquals(new Major("Computer Science"), restored.getMajor());
        assertEquals(new GraduationYear("2027"), restored.getGraduationYear());
        assertEquals(Set.of(new Tag("Friend"), new Tag("CCA")), restored.getTags());

        assertRedoSuccess(typicalPersonsWith(editedAlex));
        Person redone = model.getFilteredPersonList().get(7);
        assertEquals(new Name("Alexander Tan"), redone.getName());
        assertEquals(new Phone("87654321"), redone.getPhone());
        assertEquals(new Email("alexander@example.com"), redone.getEmail());
        assertEquals(new Address("Bukit Timah"), redone.getAddress());
        assertEquals(new Major("Data Science"), redone.getMajor());
        assertEquals(new GraduationYear("2029"), redone.getGraduationYear());
        assertEquals(Set.of(new Tag("Mentor")), redone.getTags());
    }

    @Test
    public void undoRedo_savedDataMatchesModelAndHistoryIsNotSaved() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));
        assertEditSuccess(EDIT_ALICE, EDITED_ALICE, replace(typicalPersonsWith(ALEX), ALICE, EDITED_ALICE));
        assertUndoSuccess(typicalPersonsWith(ALEX));

        // Reopening the saved data restores the undone state, but starts with no undo or redo history.
        Model reopenedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(new AddressBook(model.getAddressBook()), reopenedModel.getAddressBook());
        model = reopenedModel;
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, UndoCommand.MESSAGE_FAILURE);
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void nonModifyingCommands_leaveHistoryUnchanged() throws Exception {
        assertAddSuccess(ADD_ALEX, ALEX, typicalPersonsWith(ALEX));

        CommandResult listResult = logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(ListCommand.MESSAGE_SUCCESS + "\n" + String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW,
                typicalPersonsWith(ALEX).size()), listResult.getFeedbackToUser());
        logic.execute("tags");
        logic.execute("help");
        CommandResult findResult = logic.execute("find Meier");
        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 2), findResult.getFeedbackToUser());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
        assertContacts(typicalPersonsWith(ALEX));

        // Undo still reverts the add rather than any of the commands above, and shows every contact again.
        assertUndoSuccess(getTypicalPersons());
        logic.execute("find Meier");
        logic.execute(ListCommand.COMMAND_WORD);
        assertRedoSuccess(typicalPersonsWith(ALEX));
    }

    @Test
    public void editInFilteredList_undoRestoresContactAndShowsAll() throws Exception {
        logic.execute("find Meier");
        Person editedDaniel = new PersonBuilder(DANIEL).withPhone("81112222").build();

        // Index 2 refers to the second contact in the filtered list, not in the full list.
        assertEditSuccess("edit 2 p/81112222", editedDaniel, replace(getTypicalPersons(), DANIEL, editedDaniel));
        assertUndoSuccess(getTypicalPersons());
        assertRedoSuccess(replace(getTypicalPersons(), DANIEL, editedDaniel));
    }

    @Test
    public void editThenFav_undoRevertsFavThenEdit() throws Exception {
        // Steps from issue #134: undo after fav used to fail.
        logic.execute("clear");
        logic.execute("add n/Alice a/NUS p/91234567");
        List<Person> afterAdd = currentContacts();
        logic.execute("edit 1 p/92345678");
        List<Person> afterEdit = currentContacts();
        logic.execute("fav 1");
        List<Person> afterFav = currentContacts();

        assertUndoSuccess(afterEdit);
        assertFalse(model.getFilteredPersonList().get(0).isFavourite());
        assertEquals("92345678", model.getFilteredPersonList().get(0).getPhone().value);

        assertUndoSuccess(afterAdd);
        assertEquals("91234567", model.getFilteredPersonList().get(0).getPhone().value);

        assertRedoSuccess(afterEdit);
        assertRedoSuccess(afterFav);
        assertTrue(model.getFilteredPersonList().get(0).isFavourite());
    }

    @Test
    public void favAlreadyFavouriteAfterUndo_redoStaysAvailable() throws Exception {
        logic.execute("fav 1");
        List<Person> afterFav = currentContacts();
        logic.execute("edit 1 p/92345678");
        List<Person> afterEdit = currentContacts();
        assertUndoSuccess(afterFav);

        // The rejected fav changes nothing, so the undone edit can still be redone.
        assertCommandFailure("fav 1", CommandException.class,
                String.format(FavCommand.MESSAGE_ALREADY_FAVOURITE, afterFav.get(0).getName().fullName));
        assertRedoSuccess(afterEdit);
    }

    @Test
    public void tagThenUntagAfterUndo_redoStaysUnavailable() throws Exception {
        // Steps from issue #135: reversing the tag change used to make the discarded redo available again.
        logic.execute("clear");
        logic.execute("add n/Alice a/NUS p/91234567");
        logic.execute("edit 1 p/92345678");
        logic.execute("undo");
        List<Person> afterUndo = currentContacts();

        logic.execute("tag 1 t/Project");
        List<Person> afterTag = currentContacts();
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);

        logic.execute("untag 1 t/Project");
        assertEquals(afterUndo, currentContacts());
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
        assertEquals("91234567", model.getFilteredPersonList().get(0).getPhone().value);

        // The tag and untag are recorded in the history instead, so undo reverts them in turn.
        assertUndoSuccess(afterTag);
        assertUndoSuccess(afterUndo);
    }

    @Test
    public void mixedFavTagAddEdit_undoAllThenRedoAll_correctContactsAfterEveryStep() throws Exception {
        List<List<Person>> states = runMixedChanges();

        // Undo walks back through every change, and then the clear, to the start of the history,
        // naming each change as it is reversed.
        for (int i = states.size() - 2; i >= 0; i--) {
            assertUndoSuccess(mixedChangeFeedback.get(i + 1), states.get(i));
        }
        assertUndoSuccess(mixedChangeFeedback.get(0), getTypicalPersons());
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, UndoCommand.MESSAGE_FAILURE);

        // Redo reapplies the clear and every change in the original order, then fails at the latest change.
        for (int i = 0; i < states.size(); i++) {
            assertRedoSuccess(mixedChangeFeedback.get(i), states.get(i));
        }
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void mixedFavTagAddEdit_interleavedUndoRedo_correctContactsAfterEveryStep() throws Exception {
        List<List<Person>> states = runMixedChanges();
        int last = states.size() - 1;

        assertUndoSuccess(states.get(last - 1)); // reverts edit
        assertUndoSuccess(states.get(last - 2)); // reverts unfav
        assertTrue(model.getAddressBook().getPersonList().get(0).isFavourite());
        assertRedoSuccess(states.get(last - 1)); // reapplies unfav
        assertFalse(model.getAddressBook().getPersonList().get(0).isFavourite());
        assertUndoSuccess(states.get(last - 2)); // reverts unfav again
        assertUndoSuccess(states.get(last - 3)); // reverts untag
        assertEquals(Set.of(new Tag("Project")), model.getAddressBook().getPersonList().get(0).getTags());

        // A new fav, tag or untag after an undo discards the undone changes.
        logic.execute("tag 2 t/Club");
        List<Person> afterNewTag = currentContacts();
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
        assertUndoSuccess(states.get(last - 3));
        assertRedoSuccess(afterNewTag);
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void favThenTag_undoAndRedoNameEachChange() throws Exception {
        // Steps from issue #165: undo did not say whether the tag or the favourite was reversed.
        String favFeedback = logic.execute("fav 1").getFeedbackToUser();
        List<Person> afterFav = currentContacts();
        String tagFeedback = logic.execute("tag 2 t/Friend").getFeedbackToUser();
        List<Person> afterTag = currentContacts();

        assertUndoSuccess(tagFeedback, afterFav);
        assertUndoSuccess(favFeedback, getTypicalPersons());
        assertRedoSuccess(favFeedback, afterFav);
        assertRedoSuccess(tagFeedback, afterTag);
    }

    @Test
    public void deleteThenUndo_namesDeletedContact() throws Exception {
        String deleteFeedback = logic.execute("delete 1").getFeedbackToUser();
        List<Person> afterDelete = currentContacts();

        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(ALICE)),
                deleteFeedback);
        assertUndoSuccess(deleteFeedback, getTypicalPersons());
        assertRedoSuccess(deleteFeedback, afterDelete);
    }

    @Test
    public void unfavInFavouritesList_undoRestoresFavouriteAndShowsAll() throws Exception {
        logic.execute("fav 1");
        List<Person> afterFav = currentContacts();
        logic.execute("listfav");
        assertEquals(List.of(afterFav.get(0)), model.getFilteredPersonList());

        // The contact is no longer a favourite, so it disappears from the favourites list straight away.
        logic.execute("unfav 1");
        List<Person> afterUnfav = currentContacts();
        assertTrue(model.getFilteredPersonList().isEmpty());

        // Undo restores the favourite and shows every contact, not just the favourites.
        assertUndoSuccess(afterFav);
        assertTrue(model.getFilteredPersonList().get(0).isFavourite());
        assertRedoSuccess(afterUnfav);
        assertFalse(model.getFilteredPersonList().get(0).isFavourite());
    }

    /**
     * Runs a mix of add, edit, fav, unfav, tag and untag commands from an empty contact list,
     * and returns the contacts before the first command and after each one.
     * The feedback of the clear and of each command is kept in {@code mixedChangeFeedback}.
     */
    private List<List<Person>> runMixedChanges() throws Exception {
        mixedChangeFeedback.clear();
        mixedChangeFeedback.add(logic.execute("clear").getFeedbackToUser());
        List<List<Person>> states = new ArrayList<>();
        states.add(currentContacts());
        for (String command : List.of("add n/Alice a/NUS p/91234567", "tag 1 t/Project", "edit 1 p/92345678",
                "fav 1", "add n/Bob a/SMU p/93456789", "untag 1 t/Project", "unfav 1", "edit 2 a/NTU")) {
            mixedChangeFeedback.add(logic.execute(command).getFeedbackToUser());
            List<Person> afterCommand = currentContacts();
            assertNotEquals(states.get(states.size() - 1), afterCommand, command + " should change the contacts");
            states.add(afterCommand);
        }
        return states;
    }

    private List<Person> currentContacts() {
        return List.copyOf(model.getAddressBook().getPersonList());
    }

    /**
     * Executes an add command and checks its feedback and the resulting contacts.
     */
    private void assertAddSuccess(String command, Person added, List<Person> expectedContacts) throws Exception {
        assertCommandSuccess(command, String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(added)),
                expectedContacts);
    }

    /**
     * Executes an edit command and checks its feedback and the resulting contacts.
     */
    private void assertEditSuccess(String command, Person edited, List<Person> expectedContacts) throws Exception {
        String feedback = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited));
        if (command.contains(" t/")) {
            Person original = model.getFilteredPersonList().get(Integer.parseInt(command.split(" ")[1]) - 1);
            String removed = original.getTags().stream().filter(tag -> !edited.getTags().contains(tag))
                    .map(tag -> tag.tagName).sorted().collect(java.util.stream.Collectors.joining(", "));
            String added = edited.getTags().stream().filter(tag -> !original.getTags().contains(tag))
                    .map(tag -> tag.tagName).sorted().collect(java.util.stream.Collectors.joining(", "));
            feedback += "\nTags replaced. Removed: " + (removed.isEmpty() ? "None" : removed)
                    + ". Added: " + (added.isEmpty() ? "None" : added) + ".";
        }
        assertCommandSuccess(command, feedback, expectedContacts);
        assertEquals(expectedContacts, model.getFilteredPersonList());
    }

    /**
     * Executes {@code undo} and checks that it names the most recent recorded change,
     * and that {@code expectedContacts} are restored and all displayed.
     */
    private void assertUndoSuccess(List<Person> expectedContacts) throws Exception {
        assertUndoSuccess(model.getUndoDescription().orElseThrow(), expectedContacts);
    }

    /**
     * Executes {@code undo} and checks that it names the change described by {@code expectedDescription},
     * and that {@code expectedContacts} are restored and all displayed.
     */
    private void assertUndoSuccess(String expectedDescription, List<Person> expectedContacts) throws Exception {
        assertCommandSuccess(UndoCommand.COMMAND_WORD, String.format(UndoCommand.MESSAGE_SUCCESS, expectedDescription),
                expectedContacts);
        assertEquals(expectedContacts, model.getFilteredPersonList());
    }

    /**
     * Executes {@code redo} and checks that it names the most recently undone change,
     * and that {@code expectedContacts} are restored and all displayed.
     */
    private void assertRedoSuccess(List<Person> expectedContacts) throws Exception {
        assertRedoSuccess(model.getRedoDescription().orElseThrow(), expectedContacts);
    }

    /**
     * Executes {@code redo} and checks that it names the change described by {@code expectedDescription},
     * and that {@code expectedContacts} are restored and all displayed.
     */
    private void assertRedoSuccess(String expectedDescription, List<Person> expectedContacts) throws Exception {
        assertCommandSuccess(RedoCommand.COMMAND_WORD, String.format(RedoCommand.MESSAGE_SUCCESS, expectedDescription),
                expectedContacts);
        assertEquals(expectedContacts, model.getFilteredPersonList());
    }

    private void assertCommandSuccess(String command, String expectedMessage, List<Person> expectedContacts)
            throws Exception {
        CommandResult result = logic.execute(command);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertContacts(expectedContacts);
    }

    /**
     * Executes a command that is expected to fail, and checks that the contacts, the saved data and
     * the availability of undo and redo are unchanged.
     */
    private void assertCommandFailure(String command, Class<? extends Throwable> expectedException,
            String expectedMessage) throws Exception {
        List<Person> contactsBefore = List.copyOf(model.getAddressBook().getPersonList());
        boolean couldUndo = model.canUndoAddressBook();
        boolean couldRedo = model.canRedoAddressBook();

        assertThrows(expectedException, expectedMessage, () -> logic.execute(command));

        assertContacts(contactsBefore);
        assertEquals(couldUndo, model.canUndoAddressBook());
        assertEquals(couldRedo, model.canRedoAddressBook());
    }

    /**
     * Checks that both the model and the saved data hold exactly {@code expectedContacts}, in order.
     */
    private void assertContacts(List<Person> expectedContacts) throws Exception {
        assertEquals(expectedContacts, model.getAddressBook().getPersonList());
        assertEquals(expectedContacts, addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    private static List<Person> typicalPersonsWith(Person... added) {
        List<Person> persons = getTypicalPersons();
        persons.addAll(Arrays.asList(added));
        return persons;
    }

    private static List<Person> replace(List<Person> persons, Person target, Person replacement) {
        List<Person> replaced = new ArrayList<>(persons);
        replaced.set(replaced.indexOf(target), replacement);
        return replaced;
    }
}
