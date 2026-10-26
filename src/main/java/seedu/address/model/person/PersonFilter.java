package seedu.address.model.person;

import java.util.function.Predicate;

/**
 * A contact filter with a description of the criteria it applies.
 */
public interface PersonFilter extends Predicate<Person> {
    /** Returns the active criteria for the view status. */
    String getDescription();
}
