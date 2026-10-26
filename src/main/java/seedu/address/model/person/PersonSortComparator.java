package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Compares contacts by the selected field and direction without changing contact data.
 */
public class PersonSortComparator implements Comparator<Person> {
    /** Fields available for sorting contacts. */
    public enum SortField { NAME, MAJOR, YEAR }

    /** Directions available for sorting contacts. */
    public enum SortDirection { ASC, DESC }

    private final SortField field;
    private final SortDirection direction;
    private final Comparator<Person> comparator;

    /**
     * Creates a case-insensitive comparator for {@code field} in {@code direction}.
     * Unspecified academic values sort last; ties use ascending case-insensitive names.
     */
    public PersonSortComparator(SortField field, SortDirection direction) {
        this.field = requireNonNull(field);
        this.direction = requireNonNull(direction);
        Comparator<String> byValue = direction == SortDirection.ASC
                ? String.CASE_INSENSITIVE_ORDER : String.CASE_INSENSITIVE_ORDER.reversed();
        // Reverse the value comparison before adding nullsLast, so unspecified fields always stay last.
        comparator = Comparator.comparing(this::sortValue, Comparator.nullsLast(byValue))
                .thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Returns the selected value, using null only as an unspecified academic-field sorting key.
     * Four-digit graduation years have the same lexicographic and chronological order.
     */
    private String sortValue(Person person) {
        String value = switch (field) {
            case NAME -> person.getName().fullName;
            case MAJOR -> person.getMajor().value;
            case YEAR -> person.getGraduationYear().value;
        };
        return value.isEmpty() ? null : value;
    }

    /**
     * Creates a date comparator with oldest or newest updates first.
     * Equal dates compare equally; callers can append a name tie-break without reversing it.
     */
    public static Comparator<Person> byUpdateDate(SortDirection direction) {
        requireNonNull(direction);
        Comparator<Person> byDate = Comparator.comparing(Person::getLastUpdated);
        return direction == SortDirection.ASC ? byDate : byDate.reversed();
    }

    /** Returns the selected field and direction for the view status. */
    public String getDescription() {
        String fieldName = switch (field) {
            case NAME -> "Name";
            case MAJOR -> "Major";
            case YEAR -> "Class year";
        };
        return fieldName + (direction == SortDirection.ASC ? " ascending" : " descending");
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
