package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonSortComparatorTest {
    private final Person alice = new PersonBuilder().withName("alice Tan").build();
    private final Person bob = new PersonBuilder().withName("Bob Lim").build();
    private final Person charlie = new PersonBuilder().withName("Charlie Lee").build();

    @Test
    public void compare_names_ignoresCaseInBothDirections() {
        assertEquals(List.of(alice, bob, charlie), sorted(NAME, ASC, charlie, bob, alice));
        assertEquals(List.of(charlie, bob, alice), sorted(NAME, DESC, alice, charlie, bob));
    }

    @Test
    public void compare_equalNames_preservesInputOrder() {
        Person sameName = new PersonBuilder(alice).withName("ALICE TAN").withPhone("99999999").build();
        for (var direction : PersonSortComparator.SortDirection.values()) {
            PersonSortComparator comparator = new PersonSortComparator(NAME, direction);
            assertEquals(0, comparator.compare(alice, sameName));
            assertEquals(0, comparator.compare(alice, alice));
            assertEquals(List.of(sameName, alice), sorted(NAME, direction, sameName, alice));
        }
    }

    @Test
    public void compare_nullContact_throwsNullPointerException() {
        PersonSortComparator comparator = new PersonSortComparator(NAME, ASC);
        assertThrows(NullPointerException.class, () -> comparator.compare(null, alice));
        assertThrows(NullPointerException.class, () -> comparator.compare(alice, null));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonSortComparator(null, ASC));
        assertThrows(NullPointerException.class, () -> new PersonSortComparator(NAME, null));
    }

    @Test
    public void equalsAndHashCode_compareSortOptions() {
        PersonSortComparator comparator = new PersonSortComparator(NAME, ASC);
        assertTrue(comparator.equals(comparator));
        assertTrue(comparator.equals(new PersonSortComparator(NAME, ASC)));
        assertEquals(comparator.hashCode(), new PersonSortComparator(NAME, ASC).hashCode());
        assertFalse(comparator.equals(new PersonSortComparator(NAME, DESC)));
        assertFalse(comparator.equals(null));
        assertFalse(comparator.equals("name asc"));
    }

    @Test
    public void toStringMethod_containsSortOptions() {
        assertEquals(PersonSortComparator.class.getCanonicalName() + "{field=NAME, direction=ASC}",
                new PersonSortComparator(NAME, ASC).toString());
    }

    private List<Person> sorted(PersonSortComparator.SortField field, PersonSortComparator.SortDirection direction,
            Person... persons) {
        List<Person> result = new ArrayList<>(List.of(persons));
        result.sort(new PersonSortComparator(field, direction));
        return result;
    }
}
