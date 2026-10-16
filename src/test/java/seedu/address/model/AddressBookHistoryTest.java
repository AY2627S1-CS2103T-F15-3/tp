package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class AddressBookHistoryTest {

    private AddressBook addressBook;
    private AddressBookHistory history;

    @BeforeEach
    public void setUp() {
        addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        history = new AddressBookHistory(addressBook);
    }

    /**
     * Adds {@code person} to the tracked address book and records the change.
     */
    private void addAndCommit(Person person) {
        AddressBook previousState = new AddressBook(addressBook);
        addressBook.addPerson(person);
        history.commit(previousState);
    }

}
