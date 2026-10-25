package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FAVOURITE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADUATION_YEAR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAJOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.tag.Tag;

public class FilterCommandParserTest {
    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_validCriteria_acceptsEveryNonemptyCombination() {
        for (int mask = 1; mask < 8; mask++) {
            boolean hasMajor = (mask & 1) != 0;
            boolean hasYear = (mask & 2) != 0;
            boolean hasTag = (mask & 4) != 0;
            String input = (hasMajor ? " m/Computer Science" : "") + (hasYear ? " c/2027" : "")
                    + (hasTag ? " t/Classmate" : "");
            assertParseSuccess(parser, input, command(hasMajor ? "Computer Science" : null,
                    hasYear ? "2027" : null, hasTag ? "Classmate" : null));
        }
    }

    @Test
    public void parse_favouriteCriteria_acceptsBothValuesAndEveryCombination() {
        for (boolean favourite : new boolean[]{true, false}) {
            for (int mask = 0; mask < 8; mask++) {
                boolean hasMajor = (mask & 1) != 0;
                boolean hasYear = (mask & 2) != 0;
                boolean hasTag = (mask & 4) != 0;
                String input = " f/" + favourite + (hasMajor ? " m/Computer Science" : "")
                        + (hasYear ? " c/2027" : "") + (hasTag ? " t/Classmate" : "");
                assertParseSuccess(parser, input, command(hasMajor ? "Computer Science" : null,
                        hasYear ? "2027" : null, hasTag ? "Classmate" : null, favourite));
            }
        }
        assertParseSuccess(parser, "  t/Classmate f/ TrUe   m/Computer Science c/2027  ",
                command("Computer Science", "2027", "Classmate", true));
        assertParseSuccess(parser, "f/FALSE", command(null, null, null, false));
    }

    @Test
    public void parse_invalidFavourite_rejectsInput() {
        for (String value : new String[]{"", "   ", "yes", "no", "1", "0", "true false", "true extra"}) {
            assertParseFailure(parser, "f/" + value, FilterCommandParser.MESSAGE_INVALID_FAVOURITE);
        }
        assertParseFailure(parser, "f/ m/Computing", FilterCommandParser.MESSAGE_INVALID_FAVOURITE);
        assertParseFailure(parser, "F/true",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_orderAndSurroundingSpaces_doNotChangeCriteria() {
        assertParseSuccess(parser, "  t/Classmate   c/2027  m/Computer Science  ",
                command("Computer Science", "2027", "Classmate"));
        assertParseSuccess(parser, "m/Computer Science", command("Computer Science", null, null));
        assertParseSuccess(parser, "m/Philosophy, Politics & Economics",
                command("Philosophy, Politics & Economics", null, null));
    }

    @Test
    public void parse_yearBoundaries_acceptsBothEndpoints() {
        assertParseSuccess(parser, "c/2000", command(null, "2000", null));
        assertParseSuccess(parser, "c/2099", command(null, "2099", null));
    }

    @Test
    public void parse_missingCriteriaOrUnexpectedArguments_showsUsage() {
        for (String input : new String[]{"", "   ", "Computer Science", "or m/Computing", "n/Alice",
            "d/2", "x/value", "m/Computing x/value", "m/Computing n/Alice", "M/Computing"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_blankCriteria_rejectsInput() {
        assertParseFailure(parser, "m/", Major.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "m/   c/2027", Major.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "c/", GraduationYear.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "c/   t/Friend", GraduationYear.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "m/Computing t/   ", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidValues_rejectsInput() {
        for (String year : new String[]{"1999", "2100", "202", "20270", "abcd", "2027 extra"}) {
            assertParseFailure(parser, "c/" + year, GraduationYear.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "m/Computer\nScience", Major.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/Study Group", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/Friend!", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefixes_rejectsEvenIdenticalValues() {
        assertParseFailure(parser, "m/Computing m/Computing",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MAJOR));
        assertParseFailure(parser, "c/2027 c/2028",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GRADUATION_YEAR));
        assertParseFailure(parser, "t/Friend t/Classmate",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
        assertParseFailure(parser, "f/true f/true",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FAVOURITE));
        assertParseFailure(parser, "f/true f/false",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FAVOURITE));
    }

    private FilterCommand command(String major, String year, String tag) {
        return command(major, year, tag, null);
    }

    private FilterCommand command(String major, String year, String tag, Boolean favourite) {
        return new FilterCommand(new ContactFilterPredicate(Optional.ofNullable(major).map(Major::new),
                Optional.ofNullable(year).map(GraduationYear::new), Optional.ofNullable(tag).map(Tag::new),
                Optional.ofNullable(favourite)));
    }
}
