package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Compares contacts by the selected field and direction without changing contact data.
 */
public class PersonSortComparator implements Comparator<Person> {
    /** Fields available for sorting contacts. */
    public enum SortField { NAME }

    /** Directions available for sorting contacts. */
    public enum SortDirection { ASC, DESC }

    private final SortField field;
    private final SortDirection direction;
    private final Comparator<Person> comparator;

    /**
     * Creates a case-insensitive comparator for {@code field} in {@code direction}.
     */
    public PersonSortComparator(SortField field, SortDirection direction) {
        this.field = requireNonNull(field);
        this.direction = requireNonNull(direction);
        Comparator<Person> byName = Comparator.comparing(person -> person.getName().fullName,
                String.CASE_INSENSITIVE_ORDER);
        comparator = direction == SortDirection.ASC ? byName : byName.reversed();
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
