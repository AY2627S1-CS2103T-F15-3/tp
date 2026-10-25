package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.SystemTag;

/**
 * Matches derived automatic reminders on a fixed date, without inspecting user-owned tags.
 */
public class ReminderPredicate implements Predicate<Person> {
    private final Set<SystemTag> reminders;
    private final LocalDate today;

    /**
     * Creates a predicate matching any selected reminder on {@code today}.
     */
    public ReminderPredicate(Set<SystemTag> reminders, LocalDate today) {
        this.reminders = Set.copyOf(requireNonNull(reminders));
        checkArgument(!this.reminders.isEmpty(), "At least one automatic reminder must be selected.");
        this.today = requireNonNull(today);
    }

    @Override
    public boolean test(Person person) {
        return SystemTag.forPerson(requireNonNull(person), today).stream().anyMatch(reminders::contains);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ReminderPredicate otherPredicate
                && reminders.equals(otherPredicate.reminders) && today.equals(otherPredicate.today));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("reminders", reminders).add("today", today).toString();
    }
}
