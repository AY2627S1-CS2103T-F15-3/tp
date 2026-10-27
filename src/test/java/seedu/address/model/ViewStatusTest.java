package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListFavCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.SortResetCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.FilterCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonContainsKeywordsPredicate;
import seedu.address.model.person.PersonSortComparator;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class ViewStatusTest {
    private final ModelManager model = new ModelManager();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").withFavourite(true).build();
    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Mathematics")
            .withGraduationYear("2028").withTags("Friend").build();

    @Test
    public void status_tracksCriteriaEvenWhenResultsAreIdentical() throws Exception {
        model.addPerson(bob);
        model.addPerson(alice);
        ContactFilterPredicate filter = new ContactFilterPredicate(Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.of(new Tag("Classmate")));
        new FilterCommand(filter).execute(model);
        new SortCommand(NAME, ASC).execute(model);
        assertStatus("Showing 1 of 2 contacts | Filter: Major: Computer Science AND Class year: 2027"
                + " AND Tag: Classmate | Sort: Name ascending");
        new SortCommand(NAME, DESC).execute(model);
        assertTrue(status().endsWith("Sort: Name descending"));
        new FindCommand(new PersonContainsKeywordsPredicate(List.of("Bob", "Computing"))).execute(model);
        assertStatus("Showing 1 of 2 contacts | Filter: Search: Bob OR Computing | Sort: Name descending");
        new ListFavCommand().execute(model);
        assertStatus("Showing 1 of 2 contacts | Filter: Favourites | Sort: Name descending");
        model.setPerson(bob, new PersonBuilder(bob).withFavourite(false).build());
        assertStatus("Showing 0 of 2 contacts | Filter: Favourites | Sort: Name descending");
        new ListCommand().execute(model);
        assertStatus("Showing 2 of 2 contacts | Filter: All contacts | Sort: Name descending");
        new SortResetCommand().execute(model);
        assertStatus("Showing 2 of 2 contacts | Filter: All contacts | Sort: Saved order");
    }

    @Test
    public void status_observesHiddenContactsAndMatchingFieldChanges() {
        model.addPerson(bob);
        model.addPerson(alice);
        new FilterCommand(new ContactFilterPredicate(Optional.empty(), Optional.of(new GraduationYear("2027")),
                Optional.empty())).execute(model);
        AtomicInteger changes = new AtomicInteger();
        model.viewStatusProperty().addListener((observable, oldValue, newValue) -> changes.incrementAndGet());
        model.deletePerson(alice);
        assertStatus("Showing 1 of 1 contacts | Filter: Class year: 2027 | Sort: Saved order");
        model.setPerson(bob, new PersonBuilder(bob).withGraduationYear("2028").build());
        assertStatus("Showing 0 of 1 contacts | Filter: Class year: 2027 | Sort: Saved order");
        assertTrue(changes.get() >= 2);
        new ClearCommand().execute(model);
        assertStatus("Showing 0 of 0 contacts | Filter: Class year: 2027 | Sort: Saved order");
    }

    @Test
    public void status_addEditAndUndoResetFilterButPreserveSort() throws Exception {
        new AddCommand(bob).execute(model);
        new SortCommand(NAME, ASC).execute(model);
        new FindCommand(new PersonContainsKeywordsPredicate(List.of("Nobody"))).execute(model);
        new AddCommand(alice).execute(model);
        assertStatus("Showing 2 of 2 contacts | Filter: All contacts | Sort: Name ascending");
        new ListFavCommand().execute(model);
        new EditCommand(Index.fromOneBased(1), new EditPersonDescriptorBuilder().withMajor("Business").build())
                .execute(model);
        assertStatus("Showing 2 of 2 contacts | Filter: All contacts | Sort: Name ascending");
        new ListFavCommand().execute(model);
        new UndoCommand().execute(model);
        assertStatus("Showing 2 of 2 contacts | Filter: All contacts | Sort: Name ascending");
    }

    @Test
    public void status_failedFilterPreservesViewAndFreshModelResetsSettings() {
        model.addPerson(bob);
        new FindCommand(new PersonContainsKeywordsPredicate(List.of("Bob"))).execute(model);
        new SortCommand(NAME, DESC).execute(model);
        String before = status();
        assertThrows(ParseException.class, () -> new FilterCommandParser().parse(" c/2100"));
        assertEquals(before, status());
        ModelManager reopened = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        assertEquals("Showing 1 of 1 contacts | Filter: All contacts | Sort: Saved order",
                reopened.viewStatusProperty().get());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void status_describesEverySortFieldAndDirection() {
        for (var field : PersonSortComparator.SortField.values()) {
            String label = switch (field) {
                case NAME -> "Name";
                case MAJOR -> "Major";
                case YEAR -> "Class year";
                case UPDATED -> "Last updated";
            };
            new SortCommand(field, ASC).execute(model);
            assertTrue(status().endsWith("Sort: " + label + " ascending"));
            new SortCommand(field, DESC).execute(model);
            assertTrue(status().endsWith("Sort: " + label + " descending"));
        }
    }

    private String status() {
        return model.viewStatusProperty().get();
    }

    private void assertStatus(String expected) {
        assertEquals(expected, status());
    }
}
