package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests custom tag additions from command input through model updates.
 */
public class CustomTagsIntegrationTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_customTag_appendsWithoutReplacingExistingTags() throws Exception {
        Person original = new PersonBuilder().withTags("Friend", "CCA").build();
        model.addPerson(original);
        parser.parseCommand("tag 1 t/NOC").execute(model);
        assertEquals(new PersonBuilder(original).withTags("Friend", "CCA", "NOC").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_multipleAndMixedTags_deduplicatesAcrossInputs() throws Exception {
        Person original = new PersonBuilder().withTags("Friend").build();
        model.addPerson(original);
        parser.parseCommand("tag 1 t/NOC d/1 t/CCA t/NOC d/3 t/Teammate").execute(model);
        Person expected = new PersonBuilder(original).withTags("Friend", "NOC", "CCA", "Teammate").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        parser.parseCommand("tag 1 t/NOC").execute(model);
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_customTag_usesCurrentListIndex() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").withTags("Tutor").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        parser.parseCommand("tag 1 t/NOC").execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withTags("Tutor", "NOC").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertThrows(CommandException.class, () -> parser.parseCommand("tag 2 t/NOC").execute(model));
    }

    @Test
    public void parse_invalidTag_rejectsWholeCommandWithoutChangingPerson() {
        Person original = new PersonBuilder().withTags("Friend").build();
        model.addPerson(original);
        for (String input : new String[]{"tag 1 t/", "tag 1 t/NOC t/", "tag 1 d/2 t/bad!",
            "tag 1 t/two words", "tag 1 t/NOC d/6", "tag 1 t/NOC f/CCA"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(original, model.getFilteredPersonList().get(0));
        }
    }
}
