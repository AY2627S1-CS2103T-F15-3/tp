package seedu.address.model.tag;

import java.time.LocalDate;
import java.util.List;

import seedu.address.model.person.Person;

/**
 * Represents derived reminders, separate from user-owned tags and JSON tag data.
 */
public enum SystemTag {
    UPDATE("update");

    public final String label;

    SystemTag(String label) {
        this.label = label;
    }

    /**
     * Returns the reminders applicable to the profile on the supplied date.
     */
    public static List<SystemTag> forPerson(Person person, LocalDate today) {
        return person.needsUpdate(today) ? List.of(UPDATE) : List.of();
    }
}
