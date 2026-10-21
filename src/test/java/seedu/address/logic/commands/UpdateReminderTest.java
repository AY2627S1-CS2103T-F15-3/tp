package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.SystemTag;
import seedu.address.testutil.PersonBuilder;

public class UpdateReminderTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate OLD_DATE = LocalDate.of(2024, 6, 1);
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    public void needsUpdate_calendarYearBoundaryAndLeapYear_areStrict() {
        for (LocalDate date : List.of(TODAY, TODAY.minusMonths(1), TODAY.minusYears(1), TODAY.plusDays(1))) {
            Person person = new PersonBuilder().withLastUpdated(date).build();
            assertFalse(person.needsUpdate(TODAY));
            assertEquals(List.of(), SystemTag.forPerson(person, TODAY));
        }
        for (LocalDate date : List.of(TODAY.minusYears(1).minusDays(1), OLD_DATE)) {
            Person person = new PersonBuilder().withLastUpdated(date).build();
            assertTrue(person.needsUpdate(TODAY));
            assertEquals(List.of(SystemTag.UPDATE), SystemTag.forPerson(person, TODAY));
            assertEquals(List.of(SystemTag.UPDATE), SystemTag.forPerson(person, TODAY));
        }
        Person leapDay = new PersonBuilder().withLastUpdated(LocalDate.of(2024, 2, 29)).build();
        assertFalse(leapDay.needsUpdate(LocalDate.of(2025, 2, 28)));
        assertTrue(leapDay.needsUpdate(LocalDate.of(2025, 3, 1)));
    }

    @Test
    public void createPerson_defaultsToToday() {
        Person base = new PersonBuilder().build();
        LocalDate before = LocalDate.now();
        Person created = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getMajor(), base.getGraduationYear(), base.getTags());
        assertTrue(created.getLastUpdated().equals(before) || created.getLastUpdated().equals(LocalDate.now()));
    }

    @Test
    public void execute_addPreparedOnEarlierDate_recordsExecutionDateAndRestoresItOnRedo() throws Exception {
        Person prepared = new PersonBuilder().withLastUpdated(OLD_DATE).withFavourite(true)
                .withNotes("Keep notes").withTags("Friend").withMajor("Computing")
                .withGraduationYear("2028").build();
        Model model = new ModelManager();

        new AddCommand(prepared, CLOCK).execute(model);

        Person added = new PersonBuilder(prepared).withLastUpdated(TODAY).build();
        assertEquals(List.of(added), model.getAddressBook().getPersonList());
        assertEquals(OLD_DATE, prepared.getLastUpdated());
        assertTrue(model.undoAddressBook());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.redoAddressBook());
        assertEquals(List.of(added), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_duplicateAdd_preservesOriginalDateAndHistory() throws Exception {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).build();
        Model model = new ModelManager();
        model.addPerson(original);

        assertThrows(CommandException.class, () -> new AddCommand(original, CLOCK).execute(model));

        assertEquals(List.of(original), model.getAddressBook().getPersonList());
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_addUsesClockZoneForCalendarDate() throws Exception {
        Clock singaporeClock = Clock.fixed(java.time.Instant.parse("2026-10-19T17:00:00Z"),
                java.time.ZoneId.of("Asia/Singapore"));
        Model model = new ModelManager();

        new AddCommand(new PersonBuilder().withLastUpdated(OLD_DATE).build(), singaporeClock).execute(model);

        assertEquals(LocalDate.of(2026, 10, 20), model.getAddressBook().getPersonList().get(0).getLastUpdated());
    }

    @Test
    public void constructors_preserveExplicitStateAndDefaultOnlyMissingDates() {
        Person base = new PersonBuilder().withNotes("Existing note").build();
        Person favourite = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getMajor(), base.getGraduationYear(), base.getTags(), true);
        assertTrue(favourite.isFavourite());
        assertEquals(LocalDate.now(), favourite.getLastUpdated());
        Person withNotes = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getMajor(), base.getGraduationYear(), base.getTags(), base.getNotes());
        assertEquals(base, withNotes);
        Person dated = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getMajor(), base.getGraduationYear(), base.getTags(), base.getNotes(), OLD_DATE);
        assertEquals(new PersonBuilder(base).withLastUpdated(OLD_DATE).build(), dated);
        assertFalse(dated.isFavourite());
    }

    @Test
    public void execute_profileEdit_updatesDateAndUndoRedoRestoreIt() throws Exception {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).withNotes("Keep notes").build();
        Model model = new ModelManager();
        model.addPerson(original);
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setPhone(new Phone("91234567"));
        new EditCommand(Index.fromOneBased(1), descriptor, CLOCK).execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(new PersonBuilder(original).withPhone("91234567").withLastUpdated(TODAY).build(), edited);
        assertFalse(edited.needsUpdate(TODAY));
        assertTrue(model.undoAddressBook());
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertTrue(model.redoAddressBook());
        assertEquals(edited, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_tagOnlyAndReadOnlyOperations_preserveDateAndLegacyUpdateTag() throws Exception {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).withTags("update", "Friend").build();
        Model model = new ModelManager();
        model.addPerson(original);
        AddressBookParser parser = new AddressBookParser();
        for (String input : new String[]{"tag 1 t/NOC", "untag 1 t/NOC", "edit 1 t/update t/Friend", "list",
            "find Amy", "tag 1 t/update", "untag 1 t/update"}) {
            parser.parseCommand(input).execute(model);
            assertEquals(OLD_DATE, model.getFilteredPersonList().get(0).getLastUpdated(), input);
            assertTrue(model.getFilteredPersonList().get(0).needsUpdate(TODAY), input);
        }
        assertEquals(OLD_DATE, original.withNotes(new Notes("New notes")).getLastUpdated());
    }

    @Test
    public void execute_unchangedOrFailedEdit_doesNotUpdateDate() throws Exception {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).build();
        Model model = new ModelManager();
        model.addPerson(original);
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setPhone(original.getPhone());
        new EditCommand(Index.fromOneBased(1), descriptor, CLOCK).execute(model);
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertThrows(CommandException.class, () ->
                new EditCommand(Index.fromOneBased(2), descriptor, CLOCK).execute(model));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_eachBasicFieldChange_refreshesDateAndPreservesFavourite() throws Exception {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).withFavourite(true)
                .withNotes("Keep notes").withTags("Friend").build();
        for (int field = 0; field < 6; field++) {
            Model model = new ModelManager();
            model.addPerson(original);
            EditPersonDescriptor descriptor = new EditPersonDescriptor();
            switch (field) {
                case 0 -> descriptor.setName(new Name("New Name"));
                case 1 -> descriptor.setPhone(new Phone("91234567"));
                case 2 -> descriptor.setEmail(new Email("new@example.com"));
                case 3 -> descriptor.setAddress(new Address("New address"));
                case 4 -> descriptor.setMajor(new Major("Computing"));
                default -> descriptor.setGraduationYear(new GraduationYear("2028"));
            }
            new EditCommand(Index.fromOneBased(1), descriptor, CLOCK).execute(model);
            Person edited = model.getFilteredPersonList().get(0);
            assertEquals(TODAY, edited.getLastUpdated());
            assertFalse(edited.needsUpdate(TODAY));
            assertTrue(edited.isFavourite());
            assertEquals(original.getNotes(), edited.getNotes());
            assertEquals(original.getTags(), edited.getTags());
        }
    }

    @Test
    public void execute_favouriteNotesAndListChanges_preserveProfileDate() throws Exception {
        Model model = new ModelManager();
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).withTags("Friend").build();
        model.addPerson(original);
        AddressBookParser parser = new AddressBookParser();
        for (String command : new String[]{"fav 1", "listfav", "unfav 1", "list", "note 1 New note",
            "shownote 1", "delnote 1", "tag 1 t/NOC", "untag 1 t/NOC"}) {
            parser.parseCommand(command).execute(model);
            assertEquals(OLD_DATE, model.getAddressBook().getPersonList().get(0).getLastUpdated(), command);
        }
        assertEquals(original, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void equality_dateOnlyChange_isObservableAndCopyPreservesState() {
        Person original = new PersonBuilder().withLastUpdated(OLD_DATE).withFavourite(true).build();
        Person refreshed = new PersonBuilder(original).withLastUpdated(TODAY).build();
        assertFalse(original.equals(refreshed));
        assertTrue(original.isSamePerson(refreshed));
        assertEquals(refreshed, new PersonBuilder(refreshed).build());
        assertEquals(refreshed.hashCode(), new PersonBuilder(refreshed).build().hashCode());
        assertThrows(NullPointerException.class, () -> original.needsUpdate(null));
    }
}
