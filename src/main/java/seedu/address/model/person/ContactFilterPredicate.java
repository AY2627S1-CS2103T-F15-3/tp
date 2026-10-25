package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Matches every supplied academic, tag and favourite criterion against a contact's complete field values.
 * Major and tag matching are case-insensitive; graduation years match exactly.
 */
public class ContactFilterPredicate implements Predicate<Person> {
    private final Optional<Major> major;
    private final Optional<GraduationYear> graduationYear;
    private final Optional<Tag> tag;
    private final Optional<Boolean> favourite;

    /**
     * Creates a predicate with at least one supplied criterion.
     * Empty optionals leave their corresponding fields unrestricted.
     */
    public ContactFilterPredicate(Optional<Major> major, Optional<GraduationYear> graduationYear, Optional<Tag> tag) {
        this(major, graduationYear, tag, Optional.empty());
    }

    /**
     * Creates a predicate with an optional favourite status and at least one supplied criterion.
     * Empty optionals leave their corresponding fields unrestricted.
     */
    public ContactFilterPredicate(Optional<Major> major, Optional<GraduationYear> graduationYear,
            Optional<Tag> tag, Optional<Boolean> favourite) {
        requireAllNonNull(major, graduationYear, tag, favourite);
        checkArgument(major.isPresent() || graduationYear.isPresent() || tag.isPresent() || favourite.isPresent(),
                "At least one filter criterion is required.");
        this.major = major;
        this.graduationYear = graduationYear;
        this.tag = tag;
        this.favourite = favourite;
    }

    @Override
    public boolean test(Person person) {
        return major.map(value -> value.value.equalsIgnoreCase(person.getMajor().value)).orElse(true)
                && graduationYear.map(value -> value.equals(person.getGraduationYear())).orElse(true)
                && tag.map(value -> person.getTags().stream()
                    .anyMatch(personTag -> value.tagName.equalsIgnoreCase(personTag.tagName))).orElse(true)
                && favourite.map(value -> value == person.isFavourite()).orElse(true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ContactFilterPredicate otherPredicate
                && major.equals(otherPredicate.major) && graduationYear.equals(otherPredicate.graduationYear)
                && tag.equals(otherPredicate.tag) && favourite.equals(otherPredicate.favourite));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("major", major).add("graduationYear", graduationYear)
                .add("tag", tag).add("favourite", favourite).toString();
    }
}
