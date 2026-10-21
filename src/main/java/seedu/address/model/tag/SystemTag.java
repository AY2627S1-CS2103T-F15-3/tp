package seedu.address.model.tag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.person.Person;

/**
 * Represents derived reminders, separate from user-owned tags and JSON tag data.
 */
public enum SystemTag {
    UPDATE("update"),
    INCOMPLETE("incomplete");

    public final String label;

    SystemTag(String label) {
        this.label = label;
    }

    /**
     * Returns the reminders applicable to the profile on the supplied date.
     */
    public static List<SystemTag> forPerson(Person person, LocalDate today) {
        List<SystemTag> reminders = new ArrayList<>();
        if (person.needsUpdate(today)) {
            reminders.add(UPDATE);
        }
        if (person.isIncomplete()) {
            reminders.add(INCOMPLETE);
        }
        return List.copyOf(reminders);
    }

    /**
     * Returns whether a custom tag name needs distinguishing from a system reminder in the UI.
     */
    public static boolean isSystemLabel(String name) {
        return Arrays.stream(values()).anyMatch(tag -> tag.label.equals(name));
    }
}
