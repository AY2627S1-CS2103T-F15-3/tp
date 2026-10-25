package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only
        assertFalse(Address.isValidAddress(" ")); // no-break space only
        assertFalse(Address.isValidAddress(" ")); // EM SPACE only
        assertFalse(Address.isValidAddress("​")); // zero-width space only
        assertFalse(Address.isValidAddress("﻿")); // byte order mark only
        assertFalse(Address.isValidAddress("\u200d\u200c")); // zero width joiner and non-joiner only
        assertFalse(Address.isValidAddress("⁠​ ")); // Unicode spaces and invisible characters

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Main​St")); // invisible character within visible text
        assertTrue(Address.isValidAddress("Blk 456")); // Unicode space within visible text
        assertTrue(Address.isValidAddress("\u0645\u06cc\u200c\u0634\u0648\u062f")); // zero width non-joiner
        assertTrue(Address.isValidAddress("\u0915\u094d\u200d\u0937")); // zero width joiner
        assertTrue(Address.isValidAddress("\ud83d\udc68\u200d\ud83d\udcbb")); // emoji sequence
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
