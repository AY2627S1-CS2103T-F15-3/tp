package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests note commands from parsing through model updates, history and persistence.
 */
public class NotesCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withTags("Teammate").build();
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = createModel();

    private Model createModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(alice);
        addressBook.addPerson(bob);
        return new ModelManager(addressBook, new UserPrefs());
    }

    private CommandResult execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    @Test
    public void execute_addShowOverwriteDelete_changesOnlyNotes() throws Exception {
        assertEquals(ShowNoteCommand.MESSAGE_NO_NOTES, execute("shownote 1").getFeedbackToUser());
        assertEquals("Note saved for Alice.\nSaved note: Met during CS2103T",
                execute("note 1 Met during CS2103T").getFeedbackToUser());
        assertEquals(alice.withNotes(new Notes("Met during CS2103T")), model.getFilteredPersonList().get(0));
        assertEquals("Note for Alice:\nMet during CS2103T", execute("shownote 1").getFeedbackToUser());

        assertEquals("Note replaced for Alice. Use undo to restore the previous note."
                + "\nPrevious note: Met during CS2103T\nSaved note: Project teammate",
                execute("note 1 Project teammate").getFeedbackToUser());
        assertEquals("Note for Alice:\nProject teammate", execute("shownote 1").getFeedbackToUser());
        assertEquals(alice.withNotes(new Notes("Project teammate")), model.getFilteredPersonList().get(0));
        assertEquals("Note deleted for Alice.", execute("delnote 1").getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        assertEquals(ShowNoteCommand.MESSAGE_NO_NOTES, execute("shownote 1").getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        assertEquals("Note saved for Bob.\nSaved note: Met at orientation",
                execute("note 1 Met at orientation").getFeedbackToUser());
        assertEquals(List.of(bob.withNotes(new Notes("Met at orientation"))), model.getFilteredPersonList());
        assertEquals("Note for Bob:\nMet at orientation", execute("shownote 1").getFeedbackToUser());
        assertEquals("Note replaced for Bob. Use undo to restore the previous note."
                + "\nPrevious note: Met at orientation\nSaved note: Met at hackathon",
                execute("note 1 Met at hackathon").getFeedbackToUser());
        assertEquals(List.of(bob.withNotes(new Notes("Met at hackathon"))), model.getFilteredPersonList());
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        execute("delnote 1");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_replacementFeedback_includesCompleteNotes() throws Exception {
        String previousText = "a".repeat(Notes.MAX_LENGTH);
        String savedText = "b".repeat(Notes.MAX_LENGTH);
        assertEquals("Note saved for Alice.\nSaved note: " + previousText,
                execute("note 1 " + previousText).getFeedbackToUser());

        assertEquals("Note replaced for Alice. Use undo to restore the previous note."
                + "\nPrevious note: " + previousText + "\nSaved note: " + savedText,
                execute("note 1 " + savedText).getFeedbackToUser());
        assertEquals(new Notes(savedText), model.getFilteredPersonList().get(0).getNotes());
    }

    @Test
    public void execute_replacementFeedback_preservesUnicodeAndLiteralText() throws Exception {
        String previousText = "Met\u00A0at CS2103T; n/Alice 100%";
        String savedText = "t/Friend https://example.com/\u03B1  keep  spaces";
        execute("note 1 " + previousText);

        assertEquals("Note replaced for Alice. Use undo to restore the previous note."
                + "\nPrevious note: " + previousText + "\nSaved note: " + savedText,
                execute("note 1 " + savedText).getFeedbackToUser());
        assertEquals(new Notes(savedText), model.getFilteredPersonList().get(0).getNotes());
    }

    @Test
    public void execute_invalidDisplayedIndices_leavesDataAndHistoryUnchanged() {
        for (String input : new String[]{"note 3 Text", "shownote 3", "delnote 3"}) {
            assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> execute(input));
        }
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        for (String input : new String[]{"note 2 Text", "shownote 2", "delnote 2"}) {
            assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> execute(input));
        }
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());

        model.updateFilteredPersonList(person -> false);
        for (String input : new String[]{"note 1 Text", "shownote 1", "delnote 1"}) {
            assertThrows(CommandException.class, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> execute(input));
        }
    }

    @Test
    public void execute_invalidInput_preservesExistingNoteAndHistory() throws Exception {
        execute("note 1 Original note");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : new String[]{"note", "note 1", "note 1   ", "note x Text", "note 0 Text",
            "note 1 " + "a".repeat(Notes.MAX_LENGTH + 1), "note 1 First\tSecond", "note 1 First\nSecond",
            "shownote", "shownote 0", "shownote 1 extra", "delnote", "delnote 0", "delnote 1 extra"}) {
            assertThrows(ParseException.class, () -> execute(input));
            assertEquals(before, model.getAddressBook());
        }
        assertTrue(model.undoAddressBook());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_noteChanges_canBeUndoneAndRedone() throws Exception {
        execute("note 1 First note");
        execute("note 1 Second note");
        execute("delnote 1");
        for (String text : new String[]{"Second note", "First note", ""}) {
            assertTrue(model.undoAddressBook());
            assertEquals(alice.withNotes(new Notes(text)), model.getFilteredPersonList().get(0));
        }
        assertFalse(model.canUndoAddressBook());
        for (String text : new String[]{"First note", "Second note", ""}) {
            assertTrue(model.redoAddressBook());
            assertEquals(alice.withNotes(new Notes(text)), model.getFilteredPersonList().get(0));
        }
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_unchangedNote_skipsSnapshotsAndModelUpdates() throws Exception {
        Person existing = alice.withNotes(new Notes("Existing note"));
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(existing);
        Model unchangedModel = new ModelManager(addressBook, new UserPrefs()) {
            @Override
            public ReadOnlyAddressBook getAddressBook() {
                throw new AssertionError("An unchanged note must not request a snapshot.");
            }

            @Override
            public void setPerson(Person target, Person editedPerson) {
                throw new AssertionError("An unchanged note must not replace the contact.");
            }

            @Override
            public void commitAddressBook(ReadOnlyAddressBook previousState, String description) {
                throw new AssertionError("An unchanged note must not commit history.");
            }
        };

        CommandResult result = new NoteCommand(INDEX_FIRST_PERSON, new Notes("Existing note")).execute(unchangedModel);

        assertEquals("Note unchanged for Alice.\nSaved note: Existing note", result.getFeedbackToUser());
        assertSame(existing, unchangedModel.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_unchangedNoteAndShow_preserveRedoHistory() throws Exception {
        execute("note 1 First note");
        execute("note 1 Second note");
        assertTrue(model.undoAddressBook());
        assertEquals("Note unchanged for Alice.\nSaved note: First note",
                execute("note 1 First note").getFeedbackToUser());
        assertEquals("Note unchanged for Alice.\nSaved note: First note",
                execute("note 1 \u00A0First note\u00A0").getFeedbackToUser());
        assertEquals("Note for Alice:\nFirst note", execute("shownote 1").getFeedbackToUser());
        assertTrue(model.redoAddressBook());
        assertEquals(new Notes("Second note"), model.getFilteredPersonList().get(0).getNotes());
        assertTrue(model.undoAddressBook());
        assertTrue(model.undoAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_emptyShowAndDelete_preserveDataAndRedoHistory() throws Exception {
        execute("note 1 A note");
        assertTrue(model.undoAddressBook());
        assertEquals(ShowNoteCommand.MESSAGE_NO_NOTES, execute("shownote 1").getFeedbackToUser());
        assertEquals(DeleteNoteCommand.MESSAGE_NO_NOTES, execute("delnote 1").getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.redoAddressBook());
        assertEquals(new Notes("A note"), model.getFilteredPersonList().get(0).getNotes());
    }

    @Test
    public void execute_changedNoteAfterUndo_discardsRedoHistory() throws Exception {
        execute("note 1 First note");
        execute("note 1 Second note");
        model.undoAddressBook();
        execute("note 1 Third note");
        assertFalse(model.canRedoAddressBook());
        assertTrue(model.undoAddressBook());
        assertEquals(new Notes("First note"), model.getFilteredPersonList().get(0).getNotes());
    }

    @Test
    public void execute_unicodeBoundaryWhitespace_savesTrimmedNoteAndPreservesInternalSpaces() throws Exception {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);
        String expectedText = "Hello\u00A0World";

        logic.execute("note 1 \u00A0Hello\u00A0World\u202F");

        assertEquals(expectedText, model.getFilteredPersonList().get(0).getNotes().value);
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(expectedText, reloaded.getFilteredPersonList().get(0).getNotes().value);
        assertEquals("Note for Alice:\n" + expectedText,
                new LogicManager(reloaded, storage).execute("shownote 1").getFeedbackToUser());
    }

    @Test
    public void execute_throughLogic_savesNotesAndDeletionAcrossReloads() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Logic logic = new LogicManager(model, storage);
        assertEquals("Note saved for Alice.\nSaved note: Met at orientation",
                logic.execute("note 1 Met at orientation").getFeedbackToUser());
        assertEquals("Note replaced for Alice. Use undo to restore the previous note."
                + "\nPrevious note: Met at orientation\nSaved note: Project teammate",
                logic.execute("note 1 Project teammate").getFeedbackToUser());
        logic.execute("undo");
        assertEquals(new Notes("Met at orientation"),
                storage.readAddressBook().orElseThrow().getPersonList().get(0).getNotes());
        logic.execute("redo");

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloaded, storage);
        assertEquals("Note for Alice:\nProject teammate", reloadedLogic.execute("shownote 1").getFeedbackToUser());
        reloadedLogic.execute("delnote 1");

        Model afterDeletion = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(alice, bob), afterDeletion.getAddressBook().getPersonList());
        assertEquals(ShowNoteCommand.MESSAGE_NO_NOTES,
                new LogicManager(afterDeletion, storage).execute("shownote 1").getFeedbackToUser());
    }

    @Test
    public void constructor_invalidInputs_rejected() {
        assertThrows(NullPointerException.class, () -> new NoteCommand(null, new Notes("Text")));
        assertThrows(NullPointerException.class, () -> new NoteCommand(INDEX_FIRST_PERSON, null));
        assertThrows(IllegalArgumentException.class, () -> new NoteCommand(INDEX_FIRST_PERSON, Notes.EMPTY));
        assertThrows(NullPointerException.class, () -> new ShowNoteCommand(null));
        assertThrows(NullPointerException.class, () -> new DeleteNoteCommand(null));
    }

    @Test
    public void equals_comparesCommandTypeIndexAndNotes() {
        NoteCommand note = new NoteCommand(INDEX_FIRST_PERSON, new Notes("Text"));
        assertTrue(note.equals(note));
        assertFalse(note.equals(null));
        assertEquals(note, new NoteCommand(INDEX_FIRST_PERSON, new Notes("Text")));
        assertFalse(note.equals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Other"))));
        assertFalse(note.equals(new NoteCommand(INDEX_SECOND_PERSON, new Notes("Text"))));
        ShowNoteCommand show = new ShowNoteCommand(INDEX_FIRST_PERSON);
        assertTrue(show.equals(show));
        assertFalse(show.equals(null));
        assertEquals(show, new ShowNoteCommand(INDEX_FIRST_PERSON));
        assertFalse(show.equals(new ShowNoteCommand(INDEX_SECOND_PERSON)));
        DeleteNoteCommand delete = new DeleteNoteCommand(INDEX_FIRST_PERSON);
        assertTrue(delete.equals(delete));
        assertFalse(delete.equals(null));
        assertEquals(delete, new DeleteNoteCommand(INDEX_FIRST_PERSON));
        assertFalse(delete.equals(new DeleteNoteCommand(INDEX_SECOND_PERSON)));
        assertFalse(note.equals(show));
        assertFalse(show.equals(delete));
        assertFalse(delete.equals(note));
    }

    @Test
    public void toString_noteCommand_includesIndexAndNotes() {
        Notes notes = new Notes("Met during CS2103T");
        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, notes);
        String expected = NoteCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", notes=" + notes + "}";
        assertEquals(expected, command.toString());
    }

    @Test
    public void toString_showNoteCommand_includesIndex() {
        ShowNoteCommand command = new ShowNoteCommand(INDEX_SECOND_PERSON);
        String expected = ShowNoteCommand.class.getCanonicalName() + "{index=" + INDEX_SECOND_PERSON + "}";
        assertEquals(expected, command.toString());
    }

    @Test
    public void toString_deleteNoteCommand_includesIndex() {
        DeleteNoteCommand command = new DeleteNoteCommand(INDEX_SECOND_PERSON);
        String expected = DeleteNoteCommand.class.getCanonicalName() + "{index=" + INDEX_SECOND_PERSON + "}";
        assertEquals(expected, command.toString());
    }
}
