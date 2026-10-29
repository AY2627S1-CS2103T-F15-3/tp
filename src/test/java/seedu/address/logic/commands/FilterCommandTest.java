package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class FilterCommandTest {
    private final Model model = new ModelManager();
    private final Person first = new PersonBuilder().withName("Alice").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person second = new PersonBuilder().withName("Bob").withMajor("Computer Engineering")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person third = new PersonBuilder().withName("Charlie").withMajor("Computer Science")
            .withGraduationYear("2028").withTags("Friend").build();

    @Test
    public void execute_combinedCriteria_returnsOnlyFullMatchesWithoutChangingData() {
        addContacts();
        AddressBook before = new AddressBook(model.getAddressBook());
        FilterCommand command = new FilterCommand(predicate("computer science", "2027", "classmate"));
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), command.execute(model).getFeedbackToUser());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_singleCriterion_preservesContactOrder() {
        addContacts();
        FilterCommand command = new FilterCommand(predicate(null, "2027", null));
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), command.execute(model).getFeedbackToUser());
        assertEquals(List.of(first, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatches_reportsZeroWithoutDeletingContacts() {
        addContacts();
        FilterCommand command = new FilterCommand(predicate("Mathematics", null, null));
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), command.execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(first, second, third), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_emptyAddressBook_reportsZero() {
        FilterCommand command = new FilterCommand(predicate(null, null, "Classmate"));
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), command.execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_existingFilter_isReplaced() {
        addContacts();
        new FilterCommand(predicate("Computer Engineering", null, null)).execute(model);
        new FilterCommand(predicate("Computer Science", null, null)).execute(model);
        assertEquals(List.of(first, third), model.getFilteredPersonList());
    }

    @Test
    public void equals_comparesCriteria() {
        FilterCommand command = new FilterCommand(predicate("Computer Science", null, null));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new FilterCommand(predicate("Computer Science", null, null))));
        assertFalse(command.equals(new FilterCommand(predicate("Mathematics", null, null))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ListCommand()));
    }

    @Test
    public void hashCode_equalValues_supportHashSetLookup() {
        FilterCommand command = new FilterCommand(predicate("Computer Science", "2027", "Classmate"));
        FilterCommand equalCommand = new FilterCommand(predicate("Computer Science", "2027", "Classmate"));
        assertEquals(command.hashCode(), equalCommand.hashCode());
        assertTrue(new HashSet<>(List.of(command)).contains(equalCommand));
    }

    @Test
    public void toStringMethod_containsPredicate() {
        ContactFilterPredicate predicate = predicate(null, "2027", null);
        assertEquals(FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}",
                new FilterCommand(predicate).toString());
    }

    private void addContacts() {
        model.addPerson(first);
        model.addPerson(second);
        model.addPerson(third);
    }

    private ContactFilterPredicate predicate(String major, String year, String tag) {
        return new ContactFilterPredicate(Optional.ofNullable(major).map(Major::new),
                Optional.ofNullable(year).map(GraduationYear::new), Optional.ofNullable(tag).map(Tag::new));
    }
}
