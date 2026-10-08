package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;

public class PersonCardTest {

    @Test
    public void formatAcademicFields_valuesPresent_returnsLabeledValues() {
        assertEquals("Major: Computer Science", PersonCard.formatMajor(new Major("Computer Science")));
        assertEquals("Class year: 2027", PersonCard.formatGraduationYear(new GraduationYear("2027")));
    }

    @Test
    public void formatAcademicFields_valuesAbsent_returnsNotSpecified() {
        assertEquals("Major: Not specified", PersonCard.formatMajor(Major.EMPTY));
        assertEquals("Class year: Not specified", PersonCard.formatGraduationYear(GraduationYear.EMPTY));
    }
}
