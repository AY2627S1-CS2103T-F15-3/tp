package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests contact summaries shown in command feedback.
 */
public class MessagesTest {

    @Test
    public void format_missingPhone_showsNotSpecified() {
        Person person = new PersonBuilder().withName("Alice").withPhone("").withEmail("alice@example.com")
                .withAddress("1 College Road").withTags("Friend").build();

        assertEquals("Alice; Phone: Not specified; Email: alice@example.com"
                + "; Address: 1 College Road; Tags: [Friend]", Messages.format(person));
    }

    @Test
    public void format_missingEmail_showsNotSpecified() {
        Person person = new PersonBuilder().withName("Alice").withPhone("91234567").withEmail("")
                .withAddress("1 College Road").withTags("Friend").build();

        assertEquals("Alice; Phone: 91234567; Email: Not specified"
                + "; Address: 1 College Road; Tags: [Friend]", Messages.format(person));
    }

    @Test
    public void format_missingContactMethodsAndTags_showsExplicitEmptyValues() {
        Person person = new PersonBuilder().withName("Alice").withPhone("").withEmail("")
                .withAddress("1 College Road").withTags().build();

        assertEquals("Alice; Phone: Not specified; Email: Not specified"
                + "; Address: 1 College Road; Tags: None", Messages.format(person));
    }

    @Test
    public void format_noUserTags_showsNoneAndPreservesAcademicFields() {
        Person person = new PersonBuilder().withName("Alice").withPhone("91234567").withEmail("alice@example.com")
                .withAddress("1 College Road").withMajor("Computer Science").withGraduationYear("2027")
                .withTags().build();

        assertEquals("Alice; Phone: 91234567; Email: alice@example.com; Address: 1 College Road"
                + "; Major: Computer Science; Class year: 2027; Tags: None", Messages.format(person));
    }
}
