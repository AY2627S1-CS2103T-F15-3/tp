package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class GraduationYearTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GraduationYear(null));
    }

    @Test
    public void constructor_invalidGraduationYear_throwsIllegalArgumentException() {
        String invalidYear = "1999";
        assertThrows(IllegalArgumentException.class, () -> new GraduationYear(invalidYear));
    }

    @Test
    public void isValidGraduationYear() {
        // null graduation year
        assertThrows(NullPointerException.class, () -> GraduationYear.isValidGraduationYear(null));

        // invalid graduation year
        assertFalse(GraduationYear.isValidGraduationYear(" ")); // spaces only
        assertFalse(GraduationYear.isValidGraduationYear("1999")); // below the lower boundary
        assertFalse(GraduationYear.isValidGraduationYear("2100")); // above the upper boundary
        assertFalse(GraduationYear.isValidGraduationYear("27")); // two digits
        assertFalse(GraduationYear.isValidGraduationYear("202")); // three digits
        assertFalse(GraduationYear.isValidGraduationYear("20270")); // five digits
        assertFalse(GraduationYear.isValidGraduationYear("20XX")); // letters
        assertFalse(GraduationYear.isValidGraduationYear("2027.0")); // decimal value
        assertFalse(GraduationYear.isValidGraduationYear("+2027")); // signed value
        assertFalse(GraduationYear.isValidGraduationYear("2027 ")); // trailing space
        assertFalse(GraduationYear.isValidGraduationYear("２０２７")); // non-ASCII digits

        // valid graduation year
        assertTrue(GraduationYear.isValidGraduationYear("")); // unspecified optional field
        assertTrue(GraduationYear.isValidGraduationYear("2000")); // lower boundary
        assertTrue(GraduationYear.isValidGraduationYear("2027")); // typical year
        assertTrue(GraduationYear.isValidGraduationYear("2099")); // upper boundary
    }

    @Test
    public void equals() {
        GraduationYear year = new GraduationYear("2027");

        // same values -> returns true
        assertTrue(year.equals(new GraduationYear("2027")));

        // same object -> returns true
        assertTrue(year.equals(year));

        // null -> returns false
        assertFalse(year.equals(null));

        // different types -> returns false
        assertFalse(year.equals("2027"));

        // different values -> returns false
        assertFalse(year.equals(new GraduationYear("2028")));

        // unspecified fields -> returns true
        assertTrue(GraduationYear.EMPTY.equals(new GraduationYear("")));
    }

    @Test
    public void hashCode_sameValue_returnsSameHashCode() {
        GraduationYear year = new GraduationYear("2027");
        assertEquals(year.hashCode(), new GraduationYear("2027").hashCode());
    }

    @Test
    public void toStringMethod() {
        GraduationYear year = new GraduationYear("2027");
        assertEquals("2027", year.toString());
    }
}
