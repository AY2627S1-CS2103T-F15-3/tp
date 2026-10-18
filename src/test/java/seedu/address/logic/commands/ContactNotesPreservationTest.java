package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Ensures existing commands preserve notes when they replace a contact.
 */
public class ContactNotesPreservationTest {

    private final Person person = new PersonBuilder().withMajor("Computer Science").withGraduationYear("2027")
            .withTags("Friend").withNotes("Met during orientation").build();
    private final Model model = createModel();

    private Model createModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        return new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_edit_preservesNotesThroughUndoAndRedo() throws Exception {
        Person expected = new PersonBuilder(person).withName("New Name").withPhone("91234567")
                .withMajor("Mathematics").withTags("Teammate").build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder(expected).build()).execute(model);
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertTrue(model.undoAddressBook());
        assertEquals(person, model.getFilteredPersonList().get(0));
        assertTrue(model.redoAddressBook());
        assertEquals(expected, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_tag_preservesNotesAndOtherDetails() throws Exception {
        new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("Teammate"))).execute(model);
        assertEquals(new PersonBuilder(person).withTags("Friend", "Teammate").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_untag_preservesNotesAndOtherDetails() throws Exception {
        new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("Friend"))).execute(model);
        assertEquals(new PersonBuilder(person).withTags().build(), model.getFilteredPersonList().get(0));
    }
}
