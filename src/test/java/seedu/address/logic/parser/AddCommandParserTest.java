package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FAVOURITE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADUATION_YEAR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAJOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedPerson));


        // multiple tags - all accepted
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_favouriteFlagInAnyPosition_success() {
        String existingFields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        Person expected = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).withFavourite(true).build();
        for (String input : List.of(" f/" + existingFields + TAG_DESC_FRIEND,
                NAME_DESC_BOB + " f/" + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_FRIEND,
                existingFields + TAG_DESC_FRIEND + " f/  \t ")) {
            assertParseSuccess(parser, input, new AddCommand(expected));
        }
    }

    @Test
    public void parse_favouriteFlagWithOtherOptionalFields_success() {
        Person expected = new PersonBuilder().withName("Alice").withAddress("NUS").withPhone("").withEmail("")
                .withMajor("Computer Science").withGraduationYear("2027").withTags("Friend", "classmate")
                .withFavourite(true).build();
        assertParseSuccess(parser, " n/Alice a/NUS f/ m/Computer Science c/2027 d/1 t/classmate",
                new AddCommand(expected));
        Person minimal = new PersonBuilder().withName("Alice").withAddress("NUS").withPhone("").withEmail("")
                .withTags().withFavourite(true).build();
        assertParseSuccess(parser, " n/Alice a/NUS f/", new AddCommand(minimal));
    }

    @Test
    public void parse_favouriteFlagWithValue_failure() {
        String expectedMessage = "The f/ flag does not take a value. "
                + "Use f/ alone to mark the new contact as a favourite.";
        for (String value : List.of("true", "false", "yes", "1", "extra text")) {
            assertParseFailure(parser, NAME_DESC_BOB + ADDRESS_DESC_BOB + " f/" + value, expectedMessage);
        }
    }

    @Test
    public void parse_addressContainingEmbeddedFavouritePrefix_preservesAddressWithoutFavourite() {
        Person expected = new PersonBuilder().withName("Alice").withAddress("Blockf/3")
                .withPhone("").withEmail("").build();
        assertParseSuccess(parser, " n/Alice a/Blockf/3", new AddCommand(expected));
    }

    @Test
    public void parse_addressFollowedByFavouritePrefix_recognisesFlag() {
        Person expected = new PersonBuilder().withName("Alice").withAddress("Block")
                .withPhone("").withEmail("").withFavourite(true).build();
        assertParseSuccess(parser, " n/Alice a/Block f/", new AddCommand(expected));
        assertParseSuccess(parser, " n/Alice a/Block f/ p/91234567",
                new AddCommand(new PersonBuilder(expected).withPhone("91234567").build()));
    }

    @Test
    public void parse_addressContainingSpaceBeforeFavouritePrefixWithValue_failure() {
        assertParseFailure(parser, " n/Alice a/Block f/3", AddCommandParser.MESSAGE_FAVOURITE_FLAG_VALUE);
    }

    @Test
    public void parse_repeatedFavouriteFlag_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FAVOURITE);
        for (String flags : List.of(" f/ f/", " f/true f/", " f/ f/true")) {
            assertParseFailure(parser, NAME_DESC_BOB + ADDRESS_DESC_BOB + flags, expectedMessage);
        }
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing address prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB,
                expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Address.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + INVALID_TAG_DESC + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_universityFieldsAndTags_success() {
        String existingFields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        Person expected = new PersonBuilder(BOB).withMajor("Computer Science").withGraduationYear("2027")
                .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND).build();
        assertParseSuccess(parser, existingFields + " m/Computer Science c/2027"
                + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, new AddCommand(expected));
        assertParseSuccess(parser, " c/ 2027  m/ Computer Science  " + existingFields
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, new AddCommand(expected));
    }

    @Test
    public void parse_universityFieldsIndependentlyOptional_success() {
        String existingFields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        Person majorOnly = new PersonBuilder(BOB).withTags().withMajor("Business").build();
        Person yearOnly = new PersonBuilder(BOB).withTags().withGraduationYear("2027").build();
        assertParseSuccess(parser, existingFields + " m/Business", new AddCommand(majorOnly));
        assertParseSuccess(parser, existingFields + " c/2027", new AddCommand(yearOnly));
    }

    @Test
    public void parse_invalidUniversityFields_failure() {
        String existingFields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        for (String invalidMajor : List.of(" m/", " m/   ", " m/Computer\nScience")) {
            assertParseFailure(parser, existingFields + invalidMajor, Major.MESSAGE_CONSTRAINTS);
        }
        for (String invalidYear : List.of(" c/", " c/   ", " c/1999", " c/2100", " c/27", " c/20XX",
                " c/20270", " c/2027.0", " c/２０２７")) {
            assertParseFailure(parser, existingFields + invalidYear, GraduationYear.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedUniversityFields_failure() {
        String existingFields = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        assertParseFailure(parser, existingFields + " m/Business m/Computer Science",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MAJOR));
        assertParseFailure(parser, existingFields + " m/Business m/Business",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MAJOR));
        assertParseFailure(parser, existingFields + " c/20XX c/2027",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GRADUATION_YEAR));
    }
}
