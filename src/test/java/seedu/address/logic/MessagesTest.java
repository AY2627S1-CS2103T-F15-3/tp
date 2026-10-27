package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_allContactMethods_includesPhoneAndEmail() {
        assertEquals("Alice; Phone: 91234567; Email: alice@example.com; Address: NUS; Tags: [Friend]",
                Messages.format(new PersonBuilder().withName("Alice").withPhone("91234567")
                        .withEmail("alice@example.com").withAddress("NUS").withTags("Friend").build()));
    }

    @Test
    public void format_unspecifiedPhoneAndEmail_omitsBlankFields() {
        assertEquals("Alice; Address: NUS; Tags: ",
                Messages.format(new PersonBuilder().withName("Alice").withPhone("").withEmail("")
                        .withAddress("NUS").withTags().build()));
    }
}
