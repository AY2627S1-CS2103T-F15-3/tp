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
        assertFalse(Address.isValidAddress("\u00a0")); // no-break space only
        assertFalse(Address.isValidAddress("\u2003")); // EM SPACE only
        assertFalse(Address.isValidAddress("\u200b")); // zero-width space only
        assertFalse(Address.isValidAddress("\ufeff")); // byte order mark only
        assertFalse(Address.isValidAddress("\u200d\u200c")); // zero width joiner and non-joiner only
        assertFalse(Address.isValidAddress("\ufe0f")); // variation selector only
        assertFalse(Address.isValidAddress("\u0301\ufe0f")); // combining marks only
        assertFalse(Address.isValidAddress("\u2060\u200b\u00a0")); // Unicode spaces and invisible characters

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Main\u200bSt")); // invisible character within visible text
        assertTrue(Address.isValidAddress("Blk\u00a0456")); // Unicode space within visible text
        assertTrue(Address.isValidAddress("\u0645\u06cc\u200c\u0634\u0648\u062f")); // zero width non-joiner
        assertTrue(Address.isValidAddress("\u0915\u094d\u200d\u0937")); // zero width joiner
        assertTrue(Address.isValidAddress("\ud83d\udc68\u200d\ud83d\udcbb")); // emoji sequence
        assertTrue(Address.isValidAddress("\u2764\ufe0f")); // emoji with variation selector
        assertTrue(Address.isValidAddress("Caf\u0065\u0301")); // combining accent after a letter
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
