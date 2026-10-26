package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/** Checks date sorting through commands, displayed indices, history and storage. */
public class UpdatedSortIntegrationTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withTags("Classmate")
            .withLastUpdated(LocalDate.of(2024, 12, 31)).build();
    private final Person bob = new PersonBuilder().withName("Bob").withTags("Classmate")
            .withFavourite(true).withNotes("Discuss project")
            .withLastUpdated(LocalDate.of(2024, 12, 31)).build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withTags("Friend")
            .withLastUpdated(LocalDate.of(2025, 1, 1)).build();

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(charlie);
        model.addPerson(bob);
        model.addPerson(alice);
        storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_bothDirectionsAndTies_preservesSavedDataAndReportsStatus() throws Exception {
        assertEquals("Sorted contacts by updated (asc).",
                logic.execute("sort updated asc").getFeedbackToUser());
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
        assertTrue(model.viewStatusProperty().get().endsWith("Sort: Last updated ascending"));
        logic.execute("SORT UPDATED DESC");
        assertEquals(List.of(charlie, alice, bob), model.getFilteredPersonList());
        assertTrue(model.viewStatusProperty().get().endsWith("Sort: Last updated descending"));
        assertEquals(List.of(charlie, bob, alice), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
        ModelManager reopened = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(List.of(charlie, bob, alice), reopened.getFilteredPersonList());
        assertTrue(reopened.viewStatusProperty().get().endsWith("Sort: Saved order"));
    }

    @Test
    public void execute_unknownDate_staysLastUntilProfileEditAndReturnsOnUndo() throws Exception {
        Person legacy = new PersonBuilder().withName("Legacy").withLastUpdated(null).build();
        model.addPerson(legacy);
        for (String command : List.of("note 4 Keep note", "tag 4 t/Legacy", "fav 4", "unfav 4", "untag 4 t/Legacy")) {
            logic.execute(command);
            assertNull(model.getAddressBook().getPersonList().get(3).getLastUpdated(), command);
        }
        Person unchangedDate = model.getAddressBook().getPersonList().get(3);
        logic.execute("sort updated asc");
        assertEquals(List.of(alice, bob, charlie, unchangedDate), model.getFilteredPersonList());
        logic.execute("sort updated desc");
        assertEquals(List.of(charlie, alice, bob, unchangedDate), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        Clock clock = Clock.fixed(Instant.parse("2026-01-02T00:00:00Z"), ZoneOffset.UTC);
        new EditCommand(Index.fromOneBased(4), new EditPersonDescriptorBuilder().withPhone("99999999").build(),
                clock).execute(model);
        Person edited = new PersonBuilder(unchangedDate).withPhone("99999999")
                .withLastUpdated(LocalDate.of(2026, 1, 2)).build();
        assertEquals(List.of(edited, charlie, alice, bob), model.getFilteredPersonList());
        logic.execute("undo");
        assertEquals(List.of(charlie, alice, bob, unchangedDate), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("redo");
        assertEquals(List.of(edited, charlie, alice, bob), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_filterAndReset_preservesCriteriaAndSavedOrder() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated desc");
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        assertTrue(model.viewStatusProperty().get().contains("Filter: Tag: Classmate"));
        logic.execute("sort reset");
        assertEquals(List.of(bob, alice), model.getFilteredPersonList());
        logic.execute("sort updated asc");
        logic.execute("find Charlie");
        assertEquals(List.of(charlie), model.getFilteredPersonList());
        logic.execute("listfav");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
        logic.execute("filter t/Absent");
        logic.execute("sort updated desc");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_editDisplayedContact_reordersAndUndoRedoRestoreDates() throws Exception {
        logic.execute("filter t/Classmate");
        logic.execute("sort updated asc");
        Clock clock = Clock.fixed(Instant.parse("2026-01-02T00:00:00Z"), ZoneOffset.UTC);
        new EditCommand(Index.fromOneBased(2), new EditPersonDescriptorBuilder().withPhone("99999999").build(),
                clock).execute(model);
        Person editedBob = new PersonBuilder(bob).withPhone("99999999")
                .withLastUpdated(LocalDate.of(2026, 1, 2)).build();
        assertEquals(List.of(alice, charlie, editedBob), model.getFilteredPersonList());
        assertEquals(List.of(charlie, editedBob, alice), model.getAddressBook().getPersonList());
        logic.execute("undo");
        assertEquals(List.of(alice, bob, charlie), model.getFilteredPersonList());
        logic.execute("sort updated desc");
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(List.of(editedBob, charlie, alice), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidDateSort_preservesFilterOrderDataAndRedo() throws Exception {
        logic.execute("edit 1 p/99999999");
        logic.execute("undo");
        logic.execute("filter t/Classmate");
        logic.execute("sort updated desc");
        AddressBook before = new AddressBook(model.getAddressBook());
        String status = model.viewStatusProperty().get();
        for (String command : List.of("sort updated", "sort updated ascending", "sort updated desc extra")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(alice, bob), model.getFilteredPersonList());
            assertEquals(status, model.viewStatusProperty().get());
            assertEquals(before, model.getAddressBook());
            assertTrue(model.canRedoAddressBook());
            assertFalse(model.canUndoAddressBook());
        }
    }
}
