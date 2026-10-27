package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @Test
    public void setAcademicFieldTexts_valuesPresent_setsValues() {
        Person person = new PersonBuilder().withMajor("Computer Science").withGraduationYear("2027").build();
        AtomicReference<String> majorText = new AtomicReference<>();
        AtomicReference<String> graduationYearText = new AtomicReference<>();

        PersonCard.setAcademicFieldTexts(person, majorText::set, graduationYearText::set);

        assertEquals("Computer Science", majorText.get());
        assertEquals("2027", graduationYearText.get());
    }

    @Test
    public void setAcademicFieldTexts_valuesAbsent_setsNotSpecified() {
        Person person = new PersonBuilder().build();
        AtomicReference<String> majorText = new AtomicReference<>();
        AtomicReference<String> graduationYearText = new AtomicReference<>();

        PersonCard.setAcademicFieldTexts(person, majorText::set, graduationYearText::set);

        assertEquals("Not specified", majorText.get());
        assertEquals("Not specified", graduationYearText.get());
    }
}
