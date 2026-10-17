package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches every supplied academic criterion against a contact's complete field values.
 * Major matching is case-insensitive; graduation years match exactly.
 */
public class ContactFilterPredicate implements Predicate<Person> {
    private final Optional<Major> major;
    private final Optional<GraduationYear> graduationYear;

    /**
     * Creates a predicate with at least one supplied academic criterion.
     * Empty optionals leave their corresponding fields unrestricted.
     */
    public ContactFilterPredicate(Optional<Major> major, Optional<GraduationYear> graduationYear) {
        requireAllNonNull(major, graduationYear);
        checkArgument(major.isPresent() || graduationYear.isPresent(), "At least one filter criterion is required.");
        this.major = major;
        this.graduationYear = graduationYear;
    }

    @Override
    public boolean test(Person person) {
        return major.map(value -> value.value.equalsIgnoreCase(person.getMajor().value)).orElse(true)
                && graduationYear.map(value -> value.equals(person.getGraduationYear())).orElse(true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ContactFilterPredicate otherPredicate
                && major.equals(otherPredicate.major) && graduationYear.equals(otherPredicate.graduationYear));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("major", major).add("graduationYear", graduationYear).toString();
    }
}
