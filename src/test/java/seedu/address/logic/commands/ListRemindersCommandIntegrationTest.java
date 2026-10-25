package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.tag.SystemTag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests reminder filtering through the command, model and other commands using displayed indices.
 */
public class ListRemindersCommandIntegrationTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private static final Clock CLOCK = clockOn(TODAY);
    private static final Set<SystemTag> EITHER = Set.of(SystemTag.UPDATE, SystemTag.INCOMPLETE);

    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();
    private final Person ordinary = new PersonBuilder().withName("Alice").withLastUpdated(TODAY)
            .withTags("update", "incomplete").build();
    private final Person outdated = new PersonBuilder().withName("Bob").withLastUpdated(TODAY.minusYears(2))
            .withNotes("Keep notes").withMajor("Computing").withFavourite(true).build();
    private final Person incomplete = new PersonBuilder().withName("Zoe").withLastUpdated(TODAY)
            .withPhone("").withEmail("").build();
    private final Person both = new PersonBuilder().withName("Mia").withLastUpdated(TODAY.minusYears(2))
            .withPhone("").withEmail("").withTags("update", "incomplete").build();

    @BeforeEach
    public void setUp() {
        model.addPerson(ordinary);
        model.addPerson(outdated);
        model.addPerson(incomplete);
        model.addPerson(both);
    }

    @Test
    public void execute_eitherReminder_listsUnionOnceWithoutChangingStoredData() {
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = new ListRemindersCommand(EITHER, CLOCK).execute(model);

        assertEquals(String.format(ListRemindersCommand.MESSAGE_SUCCESS, 3), result.getFeedbackToUser());
        assertEquals(List.of(outdated, incomplete, both), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
        assertTrue(outdated.getTags().isEmpty());
        assertTrue(incomplete.getTags().isEmpty());
    }

    @Test
    public void execute_oneCriterion_matchesOnlyItsDerivedReminder() {
        new ListRemindersCommand(Set.of(SystemTag.UPDATE), CLOCK).execute(model);
        assertEquals(List.of(outdated, both), model.getFilteredPersonList());

        new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE), CLOCK).execute(model);
        assertEquals(List.of(incomplete, both), model.getFilteredPersonList());
    }

    @Test
    public void execute_customTagsAlone_doNotMatchAutomaticReminderCriteria() throws Exception {
        Model customTagModel = new ModelManager();
        customTagModel.addPerson(ordinary);
        for (Set<SystemTag> reminders : List.of(EITHER, Set.of(SystemTag.UPDATE), Set.of(SystemTag.INCOMPLETE))) {
            CommandResult result = new ListRemindersCommand(reminders, CLOCK).execute(customTagModel);
            assertEquals(ListRemindersCommand.MESSAGE_EMPTY, result.getFeedbackToUser());
            assertTrue(customTagModel.getFilteredPersonList().isEmpty());
            assertEquals(List.of(ordinary), customTagModel.getAddressBook().getPersonList());
        }
        assertFalse(customTagModel.canUndoAddressBook());
        assertFalse(customTagModel.canRedoAddressBook());
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyResult() {
        Model emptyModel = new ModelManager();
        assertEquals(ListRemindersCommand.MESSAGE_EMPTY,
                new ListRemindersCommand(EITHER, CLOCK).execute(emptyModel).getFeedbackToUser());
        assertTrue(emptyModel.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_replacesPreviousFilter_preservesSortAndListRestoresEveryone() throws Exception {
        parser.parseCommand("sort name desc").execute(model);
        parser.parseCommand("find Alice").execute(model);
        assertEquals(List.of(ordinary), model.getFilteredPersonList());

        new ListRemindersCommand(EITHER, CLOCK).execute(model);
        assertEquals(List.of(incomplete, both, outdated), model.getFilteredPersonList());

        parser.parseCommand("list").execute(model);
        assertEquals(List.of(incomplete, both, outdated, ordinary), model.getFilteredPersonList());
    }

    @Test
    public void execute_indexedCommands_targetFilteredContacts() throws Exception {
        new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE), CLOCK).execute(model);
        parser.parseCommand("fav 1").execute(model);
        Person favourited = new PersonBuilder(incomplete).withFavourite(true).build();
        assertEquals(List.of(favourited, both), model.getFilteredPersonList());

        parser.parseCommand("delete 2").execute(model);
        assertEquals(List.of(favourited), model.getFilteredPersonList());
        assertEquals(List.of(ordinary, outdated, favourited), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_beforeUndoAndRedo_keepsRecordedChangesAvailable() throws Exception {
        Model historyModel = new ModelManager();
        new AddCommand(incomplete, CLOCK).execute(historyModel);
        new ListRemindersCommand(EITHER, CLOCK).execute(historyModel);
        assertTrue(historyModel.canUndoAddressBook());
        parser.parseCommand("undo").execute(historyModel);
        assertTrue(historyModel.getAddressBook().getPersonList().isEmpty());

        new ListRemindersCommand(EITHER, CLOCK).execute(historyModel);
        assertTrue(historyModel.canRedoAddressBook());
        parser.parseCommand("redo").execute(historyModel);
        assertEquals(List.of(incomplete), historyModel.getAddressBook().getPersonList());
        assertTrue(historyModel.canUndoAddressBook());
    }

    @Test
    public void execute_invalidArguments_preservesViewDataAndHistory() throws Exception {
        new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE), CLOCK).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> viewBefore = List.copyOf(model.getFilteredPersonList());
        for (String input : new String[]{"listreminders other", "listreminders update incomplete"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(viewBefore, model.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertFalse(model.canUndoAddressBook());
        }
    }

    @Test
    public void execute_updateReminder_respectsStrictCalendarYearBoundary() {
        Model boundaryModel = new ModelManager();
        Person anniversary = new PersonBuilder().withName("Anniversary").withLastUpdated(TODAY.minusYears(1)).build();
        Person older = new PersonBuilder().withName("Older").withLastUpdated(TODAY.minusYears(1).minusDays(1)).build();
        Person future = new PersonBuilder().withName("Future").withLastUpdated(TODAY.plusDays(1)).build();
        boundaryModel.addPerson(anniversary);
        boundaryModel.addPerson(older);
        boundaryModel.addPerson(future);

        new ListRemindersCommand(Set.of(SystemTag.UPDATE), CLOCK).execute(boundaryModel);

        assertEquals(List.of(older), boundaryModel.getFilteredPersonList());
    }

    @Test
    public void execute_updateReminder_respectsLeapYearAndClockZone() {
        Model boundaryModel = new ModelManager();
        Person leapDay = new PersonBuilder().withLastUpdated(LocalDate.of(2024, 2, 29)).build();
        boundaryModel.addPerson(leapDay);
        new ListRemindersCommand(Set.of(SystemTag.UPDATE), clockOn(LocalDate.of(2025, 2, 28))).execute(boundaryModel);
        assertTrue(boundaryModel.getFilteredPersonList().isEmpty());

        Clock singaporeClock = Clock.fixed(java.time.Instant.parse("2025-02-28T17:00:00Z"),
                ZoneId.of("Asia/Singapore"));
        new ListRemindersCommand(Set.of(SystemTag.UPDATE), singaporeClock).execute(boundaryModel);
        assertEquals(List.of(leapDay), boundaryModel.getFilteredPersonList());
    }

    private static Clock clockOn(LocalDate date) {
        return Clock.fixed(date.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }
}
