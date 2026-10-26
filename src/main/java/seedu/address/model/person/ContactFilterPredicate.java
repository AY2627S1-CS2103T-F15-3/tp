package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Matches every supplied academic and tag criterion against a contact's complete field values.
 * Major and tag matching are case-insensitive; graduation years match exactly.
 */
public class ContactFilterPredicate implements PersonFilter {
    private final Optional<Major> major;
    private final Optional<GraduationYear> graduationYear;
    private final Optional<Tag> tag;

    /**
     * Creates a predicate with at least one supplied criterion.
     * Empty optionals leave their corresponding fields unrestricted.
     */
    public ContactFilterPredicate(Optional<Major> major, Optional<GraduationYear> graduationYear, Optional<Tag> tag) {
        requireAllNonNull(major, graduationYear, tag);
        checkArgument(major.isPresent() || graduationYear.isPresent() || tag.isPresent(),
                "At least one filter criterion is required.");
        this.major = major;
        this.graduationYear = graduationYear;
        this.tag = tag;
    }

    @Override
    public boolean test(Person person) {
        return major.map(value -> value.value.equalsIgnoreCase(person.getMajor().value)).orElse(true)
                && graduationYear.map(value -> value.equals(person.getGraduationYear())).orElse(true)
                && tag.map(value -> person.getTags().stream()
                    .anyMatch(personTag -> value.tagName.equalsIgnoreCase(personTag.tagName))).orElse(true);
    }

    @Override
    public String getDescription() {
        List<String> criteria = new ArrayList<>();
        major.ifPresent(value -> criteria.add("Major: " + value.value));
        graduationYear.ifPresent(value -> criteria.add("Class year: " + value.value));
        tag.ifPresent(value -> criteria.add("Tag: " + value.tagName));
        return String.join(" AND ", criteria);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ContactFilterPredicate otherPredicate
                && major.equals(otherPredicate.major) && graduationYear.equals(otherPredicate.graduationYear)
                && tag.equals(otherPredicate.tag));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("major", major).add("graduationYear", graduationYear)
                .add("tag", tag).toString();
    }
}
