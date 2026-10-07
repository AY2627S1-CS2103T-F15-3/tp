package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests default tag commands from parsing through model updates.
 */
public class DefaultTagsIntegrationTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_tags_displaysFixedOrderWithoutChangingModel() throws Exception {
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        assertEquals("1. Friend\n2. Classmate\n3. Teammate\n4. Tutor\n5. Prof",
                parser.parseCommand("tags").execute(model).getFeedbackToUser());
        assertEquals(person, model.getFilteredPersonList().get(0));
        assertThrows(ParseException.class, () -> parser.parseCommand("tags extra"));
    }

    @Test
    public void execute_tag_preservesDetailsAndExistingTagsAndDeduplicates() throws Exception {
        Person original = new PersonBuilder().withTags("NOC", "Friend").build();
        model.addPerson(original);
        parser.parseCommand("tag 1 d/1 d/3 d/3").execute(model);
        Person expected = new PersonBuilder(original).withTags("NOC", "Friend", "Teammate").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        parser.parseCommand("tag 1 d/3").execute(model);
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_tag_usesFilteredIndexAndPreservesFilter() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        parser.parseCommand("tag 1 d/2").execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withTags("Classmate").build(), model.getFilteredPersonList().get(0));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertThrows(CommandException.class, () -> parser.parseCommand("tag 2 d/1").execute(model));
    }

    @Test
    public void execute_add_supportsDefaultAndCustomTagsTogether() throws Exception {
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/NUS d/1 t/NOC d/3 d/1 t/Friend")
                .execute(model);
        assertEquals(Set.of(new Tag("Friend"), new Tag("Teammate"), new Tag("NOC")),
                model.getFilteredPersonList().get(0).getTags());
    }

    @Test
    public void execute_add_supportsAllDefaultTags() throws Exception {
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/NUS d/1 d/2 d/3 d/4 d/5")
                .execute(model);
        assertEquals(Set.of(new Tag("Friend"), new Tag("Classmate"), new Tag("Teammate"),
                new Tag("Tutor"), new Tag("Prof")), model.getFilteredPersonList().get(0).getTags());
    }

    @Test
    public void parse_invalidDefaultIndices_rejectsEntireCommand() {
        for (String value : new String[]{"", "0", "-1", "6", "1.5", "abc", "99999999999999999"}) {
            for (String prefix : new String[]{"tag 1 d/1 d/",
                "add n/Alice p/91234567 e/alice@example.com a/NUS d/1 d/"}) {
                ParseException exception = assertThrows(ParseException.class,
                        () -> parser.parseCommand(prefix + value));
                assertEquals(ParserUtil.MESSAGE_INVALID_DEFAULT_TAG_INDEX, exception.getMessage());
            }
        }
        assertEquals(0, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void parse_invalidTagSyntax_rejectsCommand() {
        for (String input : new String[]{"tag", "tag 1", "tag 0 d/1", "tag -1 d/1",
            "tag x d/1", "tag 1 t/NOC", "tag 1 d/1 t/NOC", "tag 1 f/NOC d/1"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void execute_editTags_retainsOriginalReplacementAndClearBehavior() throws Exception {
        model.addPerson(new PersonBuilder().withTags("Friend", "NOC").build());
        parser.parseCommand("edit 1 t/Custom").execute(model);
        assertEquals(Set.of(new Tag("Custom")), model.getFilteredPersonList().get(0).getTags());
        parser.parseCommand("edit 1 t/").execute(model);
        assertEquals(Set.of(), model.getFilteredPersonList().get(0).getTags());
    }
}
