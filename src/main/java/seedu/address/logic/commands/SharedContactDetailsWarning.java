package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.person.Person;

/**
 * Describes other contacts that share selected non-empty contact methods in the current display.
 */
final class SharedContactDetailsWarning {
    static final String MESSAGE_WARNING = "Warning - duplicate details detected: %1$s";
    static final String MESSAGE_MATCH = "#%1$d %2$s (%3$s)";

    private SharedContactDetailsWarning() {}

    /**
     * Appends an advisory after all existing success feedback, preserving any additional summary lines.
     */
    static CommandResult appendTo(String successFeedback, String matches) {
        return matches.isEmpty() ? new CommandResult(successFeedback)
                : CommandResult.warning(successFeedback + "\n" + String.format(MESSAGE_WARNING, matches));
    }

    /**
     * Returns compact matches in displayed order, excluding {@code person} itself.
     * Phone matching is exact; email matching ignores case.
     */
    static String describeMatches(Person person, List<Person> displayedPersons,
            boolean checkPhone, boolean checkEmail) {
        if (!checkPhone && !checkEmail) {
            return "";
        }

        List<String> matches = new ArrayList<>();
        for (int i = 0; i < displayedPersons.size(); i++) {
            Person existingPerson = displayedPersons.get(i);
            if (person.isSamePerson(existingPerson)) {
                continue;
            }
            List<String> sharedFields = new ArrayList<>();
            if (checkPhone && !person.getPhone().value.isEmpty()
                    && person.getPhone().equals(existingPerson.getPhone())) {
                sharedFields.add("phone");
            }
            if (checkEmail && !person.getEmail().value.isEmpty()
                    && person.getEmail().value.equalsIgnoreCase(existingPerson.getEmail().value)) {
                sharedFields.add("email");
            }
            if (!sharedFields.isEmpty()) {
                matches.add(String.format(MESSAGE_MATCH,
                        i + 1, existingPerson.getName().fullName, String.join(" and ", sharedFields)));
            }
        }
        return String.join("; ", matches);
    }
}
