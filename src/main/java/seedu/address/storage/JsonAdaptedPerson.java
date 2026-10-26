package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_NULL_TAG = "Tag entry %1$d is null.";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String major;
    private final String graduationYear;
    private final String notes;
    private final String lastUpdated;
    private final boolean favourite;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs an adapted contact without notes or favourite status.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String major,
            String graduationYear, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, major, graduationYear, tags, null, false, null);
    }

    /**
     * Constructs an adapted contact with notes.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String major,
            String graduationYear, List<JsonAdaptedTag> tags, String notes) {
        this(name, phone, email, address, major, graduationYear, tags, notes, false, null);
    }

    /**
     * Constructs an adapted contact with favourite status and a legacy missing date.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String major,
            String graduationYear, List<JsonAdaptedTag> tags, String notes, Boolean favourite) {
        this(name, phone, email, address, major, graduationYear, tags, notes, favourite, null);
    }

    /**
     * Constructs an adapted contact with an explicit date and no favourite status.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String major,
            String graduationYear, List<JsonAdaptedTag> tags, String notes, String lastUpdated) {
        this(name, phone, email, address, major, graduationYear, tags, notes, false, lastUpdated);
    }

    /**
     * Reads optional notes, favourite status and the ISO profile modification date.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("major") String major, @JsonProperty("graduationYear") String graduationYear,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("notes") String notes,
            @JsonProperty("favourite") Boolean favourite, @JsonProperty("lastUpdated") String lastUpdated) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.major = major;
        this.graduationYear = graduationYear;
        this.notes = notes;
        this.lastUpdated = lastUpdated;
        this.favourite = Boolean.TRUE.equals(favourite);
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        major = source.getMajor().value;
        graduationYear = source.getGraduationYear().value;
        notes = source.getNotes().value;
        lastUpdated = source.getLastUpdated().toString();
        favourite = source.isFavourite();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (int tagIndex = 0; tagIndex < tags.size(); tagIndex++) {
            JsonAdaptedTag tag = tags.get(tagIndex);
            if (tag == null) {
                throw new IllegalValueException(String.format(MESSAGE_NULL_TAG, tagIndex + 1));
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        String storedPhone = phone == null ? "" : phone;
        if (!Phone.isValidPhone(storedPhone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(storedPhone);

        String storedEmail = email == null ? "" : email;
        if (!Email.isValidEmail(storedEmail)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(storedEmail);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        String storedAddress = normalizeStoredText(address);
        if (!Address.isValidAddress(storedAddress)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(storedAddress);

        // Older AB3 files do not contain these optional fields.
        // An optional field with nothing visible in it (e.g. only NBSP or ZERO WIDTH SPACE) loads as unspecified,
        // so that one blank-looking value does not stop the whole file from loading.
        String storedMajor = isAbsentOrInvisible(major) ? "" : normalizeStoredText(major);
        if (!Major.isValidMajor(storedMajor)) {
            throw new IllegalValueException(Major.MESSAGE_CONSTRAINTS);
        }
        final Major modelMajor = new Major(storedMajor);

        String storedYear = graduationYear == null ? "" : graduationYear;
        if (!GraduationYear.isValidGraduationYear(storedYear)) {
            throw new IllegalValueException(GraduationYear.MESSAGE_CONSTRAINTS);
        }
        final GraduationYear modelGraduationYear = new GraduationYear(storedYear);

        String storedNotes = isAbsentOrInvisible(notes) ? "" : notes;
        if (!Notes.isValidNotes(storedNotes)) {
            throw new IllegalValueException(Notes.MESSAGE_CONSTRAINTS);
        }
        final Notes modelNotes = new Notes(storedNotes);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        LocalDate modelLastUpdated;
        try {
            // A missing date is a migration baseline, not a claim about the historical edit date.
            modelLastUpdated = lastUpdated == null ? LocalDate.now() : LocalDate.parse(lastUpdated);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException("Last updated date must be a valid ISO date (YYYY-MM-DD).", e);
        }
        return new Person(modelName, modelPhone, modelEmail, modelAddress,
                modelMajor, modelGraduationYear, modelTags, modelNotes, favourite, modelLastUpdated);
    }

    /**
     * Returns {@code value} as a command would store it: Unicode spaces become ASCII spaces and the ends are trimmed.
     * Data files saved before command input was normalised (#141) may still contain Unicode spaces.
     */
    private static String normalizeStoredText(String value) {
        return StringUtil.normalizeWhitespace(value).trim();
    }

    private static boolean isAbsentOrInvisible(String value) {
        return value == null || !StringUtil.hasVisibleCharacter(value);
    }

}
