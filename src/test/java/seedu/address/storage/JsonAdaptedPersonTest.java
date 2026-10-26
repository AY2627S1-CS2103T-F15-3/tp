package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    /** Values with nothing visible: Unicode spaces, format characters and a lone variation selector. */
    private static final String[] INVISIBLE_VALUES =
        {" ", "\u00a0", "\u2003", "\u200b", "\ufeff", "\u2060\u200b", "\ufe0f"};

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_notesRoundTrip_preservesText() throws Exception {
        Person expected = new PersonBuilder(BENSON).withNotes("Met at \"orientation\"; \u674e\u540c\u5b66").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(expected));
        assertEquals(expected, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_emptyOrNullNotes_returnsEmptyNotes() throws Exception {
        for (String notes : new String[]{"", null}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                    null, null, VALID_TAGS, notes);
            assertEquals(new PersonBuilder(BENSON).withLastUpdated(null).build(), person.toModelType());
            assertEquals(Notes.EMPTY, person.toModelType().getNotes());
        }
    }

    @Test
    public void toModelType_invalidNotes_throwsIllegalValueException() {
        for (String notes : new String[]{"First\nSecond", "a".repeat(Notes.MAX_LENGTH + 1)}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                    null, null, VALID_TAGS, notes);
            assertThrows(IllegalValueException.class, Notes.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null, null, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null, null, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_loadsAsUnspecified() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, null, null, VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withPhone("").withLastUpdated(null).build(), person.toModelType());
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, null, null, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_loadsAsUnspecified() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, null, null, VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withEmail("").withLastUpdated(null).build(), person.toModelType());
    }

    @Test
    public void toModelType_invisibleOnlyAddress_throwsIllegalValueException() {
        for (String invisible : INVISIBLE_VALUES) {
            JsonAdaptedPerson person =
                    new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, invisible, null, null, VALID_TAGS);
            assertThrows(IllegalValueException.class, Address.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_invisibleOnlyMajorOrNotes_loadsAsUnspecified() throws Exception {
        // Optional fields that look blank should not stop the whole data file from loading.
        for (String invisible : INVISIBLE_VALUES) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                    invisible, null, VALID_TAGS, invisible);
            Person loaded = person.toModelType();
            assertEquals("", loaded.getMajor().value);
            assertEquals(Notes.EMPTY, loaded.getNotes());
        }
    }

    @Test
    public void toModelType_unicodeSpacesInAddressAndMajor_normalisedLikeCommandInput() throws Exception {
        for (String space : new String[]{"\u00a0", "\u2003", "\u202f", "\u3000", "\t"}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    space + "Blk" + space + "456" + space, space + "Computer" + space + "Science" + space, null,
                    VALID_TAGS);
            Person loaded = person.toModelType();
            assertEquals("Blk 456", loaded.getAddress().value);
            assertEquals("Computer Science", loaded.getMajor().value);
        }
    }

    @Test
    public void toModelType_unicodeSpacesInNotes_preserved() throws Exception {
        String notes = "Met\u00a0at\u2003orientation";
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, null, VALID_TAGS, notes);
        assertEquals(notes, person.toModelType().getNotes().value);
    }

    @Test
    public void toModelType_emptyMajor_returnsUnspecifiedMajor() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, "", null, VALID_TAGS);
        assertEquals("", person.toModelType().getMajor().value);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, null, null, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, null, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null, null, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_nullTag_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(null);
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null, null, invalidTags);

        assertThrows(IllegalValueException.class, "Tag entry 3 is null.", person::toModelType);
    }

    @Test
    public void toModelType_nullFirstTag_reportsFirstTagPosition() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, null, Collections.singletonList(null));

        assertThrows(IllegalValueException.class, "Tag entry 1 is null.", person::toModelType);
    }

    @Test
    public void toModelType_nullTagName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, null, List.of(new JsonAdaptedTag((String) null)));

        assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, person::toModelType);
    }


    @Test
    public void toModelType_universityFields_returnsPerson() throws Exception {
        Person expected = new PersonBuilder(BENSON).withMajor("Computer Science").withGraduationYear("2027").build();
        assertEquals(expected, new JsonAdaptedPerson(expected).toModelType());
    }

    @Test
    public void toModelType_missingOptionalFields_preservesLegacyContact() throws Exception {
        JsonAdaptedPerson legacy = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, null, VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withLastUpdated(null).build(), legacy.toModelType());
        JsonAdaptedPerson empty = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                "", "", VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withLastUpdated(null).build(), empty.toModelType());
    }

    @Test
    public void toModelType_oneOptionalFieldMissing_preservesOtherField() throws Exception {
        JsonAdaptedPerson majorOnly = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                "Business", null, VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withMajor("Business").withLastUpdated(null).build(),
                majorOnly.toModelType());
        JsonAdaptedPerson yearOnly = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, "2027", VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withGraduationYear("2027").withLastUpdated(null).build(),
                yearOnly.toModelType());
    }

    @Test
    public void toModelType_invalidUniversityFields_throwsIllegalValueException() {
        JsonAdaptedPerson invalidMajor = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                "Computer\u0001Science", "2027", VALID_TAGS);
        assertThrows(IllegalValueException.class, Major.MESSAGE_CONSTRAINTS, invalidMajor::toModelType);
        JsonAdaptedPerson invalidYear = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                "Computer Science", "2100", VALID_TAGS);
        assertThrows(IllegalValueException.class, GraduationYear.MESSAGE_CONSTRAINTS, invalidYear::toModelType);
    }
}
