package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.PersonSortComparator.SortDirection.ASC;
import static seedu.address.model.person.PersonSortComparator.SortDirection.DESC;
import static seedu.address.model.person.PersonSortComparator.SortField.MAJOR;
import static seedu.address.model.person.PersonSortComparator.SortField.NAME;
import static seedu.address.model.person.PersonSortComparator.SortField.YEAR;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
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
    public void compare_majors_ignoresCaseAndKeepsUnspecifiedLast() {
        Person business = new PersonBuilder(bob).withMajor("Business").build();
        Person computing = new PersonBuilder(charlie).withMajor("computer science").build();
        assertEquals(List.of(business, computing, alice), sorted(MAJOR, ASC, alice, computing, business));
        assertEquals(List.of(computing, business, alice), sorted(MAJOR, DESC, alice, business, computing));
    }

    @Test
    public void compare_years_ordersChronologicallyAndKeepsUnspecifiedLast() {
        Person earliest = new PersonBuilder(bob).withGraduationYear("2000").build();
        Person latest = new PersonBuilder(charlie).withGraduationYear("2099").build();
        assertEquals(List.of(earliest, latest, alice), sorted(YEAR, ASC, alice, latest, earliest));
        assertEquals(List.of(latest, earliest, alice), sorted(YEAR, DESC, earliest, alice, latest));
    }

    @Test
    public void compare_equalOrUnspecifiedAcademicValues_breaksTiesByAscendingName() {
        Person sameMajor = new PersonBuilder(bob).withMajor("Computer Science").withGraduationYear("2027").build();
        Person sameFields = new PersonBuilder(alice).withMajor("computer science").withGraduationYear("2027").build();
        for (var field : List.of(MAJOR, YEAR)) {
            for (var direction : PersonSortComparator.SortDirection.values()) {
                assertEquals(List.of(sameFields, sameMajor), sorted(field, direction, sameMajor, sameFields));
                assertEquals(List.of(alice, bob, charlie), sorted(field, direction, charlie, bob, alice));
            }
        }
    }

    @Test
    public void compare_academicFields_isAntisymmetricAndTransitive() {
        List<Person> contacts = List.of(alice, new PersonBuilder(bob).withMajor("Business")
                .withGraduationYear("2000").build(), new PersonBuilder(charlie).withMajor("Computing")
                .withGraduationYear("2099").build());
        for (var field : PersonSortComparator.SortField.values()) {
            for (var direction : PersonSortComparator.SortDirection.values()) {
                PersonSortComparator comparator = new PersonSortComparator(field, direction);
                List<Person> ordered = new ArrayList<>(contacts);
                ordered.sort(comparator);
                assertTrue(comparator.compare(ordered.get(0), ordered.get(2)) < 0);
                for (Person first : contacts) {
                    for (Person second : contacts) {
                        assertEquals(Integer.signum(comparator.compare(first, second)),
                                -Integer.signum(comparator.compare(second, first)));
                    }
                }
            }
        }
    }

    @Test
    public void byUpdateDate_ordersCalendarDatesInBothDirections() {
        Person oldest = new PersonBuilder(bob).withLastUpdated(LocalDate.of(2020, 12, 31)).build();
        Person leapDay = new PersonBuilder(alice).withLastUpdated(LocalDate.of(2024, 2, 29)).build();
        Person newest = new PersonBuilder(charlie).withLastUpdated(LocalDate.of(2025, 1, 1)).build();
        List<Person> contacts = new ArrayList<>(List.of(newest, oldest, leapDay));
        contacts.sort(PersonSortComparator.byUpdateDate(ASC));
        assertEquals(List.of(oldest, leapDay, newest), contacts);
        contacts.sort(PersonSortComparator.byUpdateDate(DESC));
        assertEquals(List.of(newest, leapDay, oldest), contacts);
    }

    @Test
    public void byUpdateDate_equalDatesAndExtremes_compareConsistently() {
        Person earliest = new PersonBuilder(alice).withLastUpdated(LocalDate.MIN).build();
        Person latest = new PersonBuilder(bob).withLastUpdated(LocalDate.MAX).build();
        Person sameDate = new PersonBuilder(charlie).withLastUpdated(LocalDate.MIN).build();
        for (var direction : PersonSortComparator.SortDirection.values()) {
            var comparator = PersonSortComparator.byUpdateDate(direction);
            assertEquals(0, comparator.compare(earliest, sameDate));
            assertEquals(-Integer.signum(comparator.compare(earliest, latest)),
                    Integer.signum(comparator.compare(latest, earliest)));
        }
        assertTrue(PersonSortComparator.byUpdateDate(ASC).compare(earliest, latest) < 0);
        assertTrue(PersonSortComparator.byUpdateDate(DESC).compare(earliest, latest) > 0);
        assertThrows(NullPointerException.class, () -> PersonSortComparator.byUpdateDate(null));
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
        assertFalse(comparator.equals(new PersonSortComparator(MAJOR, ASC)));
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
