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
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.SystemTag;
import seedu.address.testutil.PersonBuilder;

public class IncompleteReminderTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final LocalDate OLD_DATE = LocalDate.of(2024, 10, 1);
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void completeness_allCombinations_useOnlyPhoneAndEmail() {
        for (String phone : List.of("", "91234567")) {
            for (String email : List.of("", "alice@example.com")) {
                Person person = new PersonBuilder().withPhone(phone).withEmail(email).withMajor("")
                        .withGraduationYear("").withTags("Friend").withFavourite(true).withLastUpdated(TODAY).build();
                boolean missingBoth = phone.isEmpty() && email.isEmpty();
                assertEquals(missingBoth, person.isIncomplete());
                assertEquals(missingBoth ? List.of(SystemTag.INCOMPLETE) : List.of(),
                        SystemTag.forPerson(person, TODAY));
            }
        }
    }

    @Test
    public void add_optionalContactFields_supportsBothMissingAndEitherPresent() throws Exception {
        for (String fields : List.of("", " p/91234567", " e/alice@example.com",
                " p/91234567 e/alice@example.com")) {
            Model model = new ModelManager();
            parser.parseCommand("add n/Alice a/NUS" + fields).execute(model);
            Person person = model.getFilteredPersonList().get(0);
            assertEquals(fields.contains("p/") ? "91234567" : "", person.getPhone().value);
            assertEquals(fields.contains("e/") ? "alice@example.com" : "", person.getEmail().value);
            assertEquals(fields.isEmpty(), person.isIncomplete());
        }
    }

    @Test
    public void edit_fillOrClearEitherContact_updatesDateAndPreservesOtherState() throws Exception {
        Person original = new PersonBuilder().withPhone("").withEmail("").withLastUpdated(OLD_DATE)
                .withNotes("Keep notes").withFavourite(true).withMajor("Computing").withGraduationYear("2027")
                .withTags("Friend", "incomplete").build();
        assertEquals(List.of(SystemTag.UPDATE, SystemTag.INCOMPLETE), SystemTag.forPerson(original, TODAY));
        for (boolean usePhone : List.of(true, false)) {
            Model model = new ModelManager();
            model.addPerson(original);
            EditPersonDescriptor descriptor = new EditPersonDescriptor();
            if (usePhone) {
                descriptor.setPhone(new Phone("91234567"));
            } else {
                descriptor.setEmail(new Email("alice@example.com"));
            }
            new EditCommand(Index.fromOneBased(1), descriptor, CLOCK).execute(model);
            Person filled = model.getFilteredPersonList().get(0);
            assertEquals(TODAY, filled.getLastUpdated());
            assertEquals(List.of(), SystemTag.forPerson(filled, TODAY));
            Person expected = usePhone
                    ? new PersonBuilder(original).withPhone("91234567").withLastUpdated(TODAY).build()
                    : new PersonBuilder(original).withEmail("alice@example.com").withLastUpdated(TODAY).build();
            assertEquals(expected, filled);
            parser.parseCommand(usePhone ? "edit 1 p/" : "edit 1 e/").execute(model);
            Person cleared = model.getFilteredPersonList().get(0);
            assertTrue(cleared.isIncomplete());
            assertEquals(new PersonBuilder(original).withLastUpdated(LocalDate.now()).build(), cleared);
        }
    }

    @Test
    public void tagFavouriteAndReadOnlyOperations_cannotRemoveReminderOrResetDate() throws Exception {
        Person original = new PersonBuilder().withPhone("").withEmail("").withLastUpdated(OLD_DATE)
                .withTags("incomplete").withNotes("Keep notes").build();
        Model model = new ModelManager();
        model.addPerson(original);
        for (String command : List.of("fav 1", "listfav", "unfav 1", "list", "tag 1 t/Friend",
                "untag 1 t/incomplete", "tag 1 t/incomplete", "find Amy", "sort name asc")) {
            parser.parseCommand(command).execute(model);
            Person person = model.getAddressBook().getPersonList().get(0);
            assertEquals(OLD_DATE, person.getLastUpdated(), command);
            assertEquals(original.getNotes(), person.getNotes(), command);
            assertEquals(List.of(SystemTag.UPDATE, SystemTag.INCOMPLETE), SystemTag.forPerson(person, TODAY));
        }
    }

    @Test
    public void invalidNonEmptyValuesAndBlankAddPrefixes_areStillRejectedAtomically() throws Exception {
        Model model = new ModelManager();
        Person original = new PersonBuilder().withFavourite(true).build();
        model.addPerson(original);
        for (String command : List.of("add n/Alice a/NUS p/", "add n/Alice a/NUS e/", "add n/Alice a/NUS p/12",
                "add n/Alice a/NUS e/invalid", "edit 1 p/12 e/", "edit 1 p/ e/invalid")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertEquals(List.of(original), model.getAddressBook().getPersonList());
        }
        parser.parseCommand("edit 1 p/ e/").execute(model);
        assertTrue(model.getFilteredPersonList().get(0).isIncomplete());
        assertTrue(model.getFilteredPersonList().get(0).isFavourite());
        assertFalse(Phone.isValidPhone(" "));
        assertFalse(Email.isValidEmail(" "));
    }
}
