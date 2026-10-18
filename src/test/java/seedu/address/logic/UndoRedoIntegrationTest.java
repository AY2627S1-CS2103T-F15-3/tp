package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that {@code add}, {@code edit}, {@code undo} and {@code redo} work together through command parsing,
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
        assertCommandSuccess(command, String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited)),
                expectedContacts);
        assertEquals(expectedContacts, model.getFilteredPersonList());
    }

    /**
     * Executes {@code undo} and checks that {@code expectedContacts} are restored and all displayed.
     */
    private void assertUndoSuccess(List<Person> expectedContacts) throws Exception {
        assertCommandSuccess(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_SUCCESS, expectedContacts);
        assertEquals(expectedContacts, model.getFilteredPersonList());
    }

    /**
     * Executes {@code redo} and checks that {@code expectedContacts} are restored and all displayed.
     */
    private void assertRedoSuccess(List<Person> expectedContacts) throws Exception {
        assertCommandSuccess(RedoCommand.COMMAND_WORD, RedoCommand.MESSAGE_SUCCESS, expectedContacts);
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
