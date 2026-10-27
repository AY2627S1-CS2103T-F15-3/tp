package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests tag removal from parsing through model updates.
 */
public class UntagCommandIntegrationTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_oneTag_preservesOtherTagsAndDetails() throws Exception {
        Person original = new PersonBuilder().withTags("Friend", "NOC", "CCA").build();
        model.addPerson(original);
        parser.parseCommand("untag 1 t/NOC").execute(model);
        assertEquals(new PersonBuilder(original).withTags("Friend", "CCA").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_multipleTags_removesDefaultTagsAndAllowsEmptyResult() throws Exception {
        Person original = new PersonBuilder().withTags("Friend", "NOC").build();
        model.addPerson(original);
        parser.parseCommand("untag 1 t/Friend t/NOC t/NOC").execute(model);
        assertEquals(new PersonBuilder(original).withTags().build(), model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_noRequestedTagPresent_rejectsAndKeepsPersonUnchanged() throws Exception {
        Person original = new PersonBuilder().withName("Alice").withTags("NOC").build();
        model.addPerson(original);
        CommandException exception = assertThrows(CommandException.class, () ->
                parser.parseCommand("untag 1 t/CCA t/Missing").execute(model));
        assertEquals(String.format(UntagCommand.MESSAGE_NONE_FOUND, "Alice", "CCA, Missing"),
                exception.getMessage());
        assertEquals(original, model.getFilteredPersonList().get(0));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_someRequestedTagsMissing_removesPresentTagsAndReportsMissing() throws Exception {
        Person original = new PersonBuilder().withTags("NOC", "CCA").build();
        model.addPerson(original);
        CommandResult result = parser.parseCommand("untag 1 t/noc t/Missing").execute(model);
        Person expected = new PersonBuilder(original).withTags("CCA").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        String successMessage = String.format(UntagCommand.MESSAGE_SUCCESS, Messages.format(expected));
        assertEquals(successMessage + "\n" + String.format(UntagCommand.MESSAGE_SOME_NOT_FOUND, "Missing"),
                result.getFeedbackToUser());
        // The history description is the plain success message, without the not-found note.
        assertEquals(successMessage, model.getUndoDescription().orElseThrow());
    }

    @Test
    public void execute_differentCapitalization_removesTag() throws Exception {
        Person original = new PersonBuilder().withTags("Friend", "NOC").build();
        model.addPerson(original);
        parser.parseCommand("untag 1 t/friend").execute(model);
        assertEquals(new PersonBuilder(original).withTags("NOC").build(), model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").withTags("NOC").build();
        Person bob = new PersonBuilder().withName("Bob").withTags("NOC", "Tutor").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.updateFilteredPersonList(person -> person.getName().equals(bob.getName()));
        parser.parseCommand("untag 1 t/NOC").execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withTags("Tutor").build(), model.getFilteredPersonList().get(0));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
        assertThrows(CommandException.class, () -> parser.parseCommand("untag 2 t/NOC").execute(model));
    }

    @Test
    public void execute_emptyList_rejectsIndex() {
        assertThrows(CommandException.class, () -> parser.parseCommand("untag 1 t/NOC").execute(model));
    }

    @Test
    public void parse_invalidInput_leavesPersonUnchanged() {
        Person original = new PersonBuilder().withTags("NOC", "Friend").build();
        model.addPerson(original);
        for (String input : new String[]{"untag", "untag 1", "untag 0 t/NOC", "untag x t/NOC",
            "untag -1 t/NOC", "untag 1 t/", "untag 1 t/NOC t/", "untag 1 t/bad!",
            "untag 1 d/1", "untag 1 t/NOC d/1", "untag 1 f/NOC", "untag 1 t/two words"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(original, model.getFilteredPersonList().get(0));
        }
    }
}
