package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Compares contacts by the selected field and direction without changing contact data.
 */
public class PersonSortComparator implements Comparator<Person> {
    /** Fields available for sorting contacts. */
    public enum SortField { NAME, MAJOR, YEAR, UPDATED }

    /** Directions available for sorting contacts. */
    public enum SortDirection { ASC, DESC }

    private final SortField field;
    private final SortDirection direction;
    private final Comparator<Person> comparator;

    /**
     * Creates a comparator for {@code field} in {@code direction}, ignoring case in names and majors.
     * Unspecified academic values sort last; ties use ascending case-insensitive names.
     */
    public PersonSortComparator(SortField field, SortDirection direction) {
        this.field = requireNonNull(field);
        this.direction = requireNonNull(direction);
        Comparator<String> byValue = direction == SortDirection.ASC
                ? String.CASE_INSENSITIVE_ORDER : String.CASE_INSENSITIVE_ORDER.reversed();
        // Apply direction before nullsLast so unspecified academic values always stay last.
        Comparator<String> byAcademicValue = Comparator.nullsLast(byValue);
        Comparator<Person> byDate = Comparator.comparing(Person::getLastUpdated);
        Comparator<Person> byField = switch (field) {
            case NAME -> Comparator.comparing(person -> person.getName().fullName, byValue);
            case MAJOR -> Comparator.comparing(person -> specifiedValue(person.getMajor().value), byAcademicValue);
            case YEAR -> Comparator.comparing(
                    person -> specifiedValue(person.getGraduationYear().value), byAcademicValue);
            case UPDATED -> direction == SortDirection.ASC ? byDate : byDate.reversed();
        };
        comparator = byField.thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);
    }

    /** Returns null for unspecified academic values so they can be placed last in either direction. */
    private static String specifiedValue(String value) {
        return value.isEmpty() ? null : value;
    }

    @Override
    public int compare(Person first, Person second) {
        return comparator.compare(first, second);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof PersonSortComparator otherComparator
                && field == otherComparator.field && direction == otherComparator.direction);
    }

    @Override
    public int hashCode() {
        return 31 * field.hashCode() + direction.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("field", field).add("direction", direction).toString();
    }
}
