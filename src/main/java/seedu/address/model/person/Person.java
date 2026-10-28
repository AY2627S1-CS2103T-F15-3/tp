package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: contact details are not null, field values are validated, immutable.
 * The last-updated date may be null for legacy contacts whose update date is unknown.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Major major;
    private final GraduationYear graduationYear;
    private final Notes notes;
    private final LocalDate lastUpdated;
    private final Set<Tag> tags = new HashSet<>();
    private final boolean favourite;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags) {
        this(name, phone, email, address, major, graduationYear, tags, Notes.EMPTY, false);
    }

    /**
     * Creates a person with the specified favourite state and empty notes.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags, boolean favourite) {
        this(name, phone, email, address, major, graduationYear, tags, Notes.EMPTY, favourite);
    }

    /**
     * Creates a person with optional notes and no favourite status.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags, Notes notes) {
        this(name, phone, email, address, major, graduationYear, tags, notes, false, LocalDate.now());
    }

    /**
     * Creates a person with an explicit date and no favourite status.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags, Notes notes, LocalDate lastUpdated) {
        this(name, phone, email, address, major, graduationYear, tags, notes, false, lastUpdated);
    }

    /**
     * Creates a person preserving notes and favourite status, initially updated today.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags, Notes notes, boolean favourite) {
        this(name, phone, email, address, major, graduationYear, tags, notes, favourite, LocalDate.now());
    }

    /**
     * Creates a person preserving all contact state and the basic-profile date.
     * A null date represents an unknown last update in legacy data.
     */
    public Person(Name name, Phone phone, Email email, Address address, Major major,
            GraduationYear graduationYear, Set<Tag> tags, Notes notes, boolean favourite, LocalDate lastUpdated) {
        requireAllNonNull(name, phone, email, address, major, graduationYear, tags, notes);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.major = major;
        this.graduationYear = graduationYear;
        this.notes = notes;
        this.lastUpdated = lastUpdated;
        this.tags.addAll(tags);
        this.favourite = favourite;
    }

    /**
     * Returns whether this person is a favourite.
     */
    public boolean isFavourite() {
        return favourite;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Major getMajor() {
        return major;
    }

    public GraduationYear getGraduationYear() {
        return graduationYear;
    }

    public Notes getNotes() {
        return notes;
    }

    /**
     * Returns the date added or last actual change to the basic profile, excluding tags, notes and favourites.
     * Returns null when no date was recorded in a legacy contact.
     */
    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    //@@author aziz-Almarzooq
    /**
     * Returns a copy with the specified basic-profile date, preserving all other contact state.
     */
    public Person withLastUpdated(LocalDate updatedDate) {
        return new Person(name, phone, email, address, major, graduationYear, tags, notes,
                favourite, updatedDate);
    }
    //@@author

    /**
     * Returns whether both ways to contact this person are unspecified.
     */
    public boolean isIncomplete() {
        return phone.value.isEmpty() && email.value.isEmpty();
    }

    /**
     * Returns whether the basic profile is strictly more than one calendar year old on the given date.
     */
    public boolean needsUpdate(LocalDate today) {
        java.util.Objects.requireNonNull(today);
        return lastUpdated != null && lastUpdated.isBefore(today) && lastUpdated.plusYears(1).isBefore(today);
    }

    /**
     * Returns a copy with the given notes, preserving all other contact details.
     */
    public Person withNotes(Notes updatedNotes) {
        return new Person(name, phone, email, address, major, graduationYear, tags, updatedNotes,
                favourite, lastUpdated);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name, ignoring case.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().fullName.equalsIgnoreCase(getName().fullName);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && major.equals(otherPerson.major)
                && graduationYear.equals(otherPerson.graduationYear)
                && notes.equals(otherPerson.notes)
                && tags.equals(otherPerson.tags)
                && Objects.equals(lastUpdated, otherPerson.lastUpdated)
                && favourite == otherPerson.favourite;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, major, graduationYear, tags, notes, favourite, lastUpdated);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("major", major)
                .add("graduationYear", graduationYear)
                .add("tags", tags)
                .add("notes", notes)
                .add("favourite", favourite)
                .add("lastUpdated", lastUpdated)
                .toString();
    }

}
