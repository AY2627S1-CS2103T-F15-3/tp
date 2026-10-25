package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;

public class SortCommandParserTest {
    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_allSupportedOptions_returnsExpectedCommand() {
        for (SortField field : SortField.values()) {
            for (SortDirection direction : SortDirection.values()) {
                String input = field.name().toLowerCase(Locale.ROOT) + " "
                        + direction.name().toLowerCase(Locale.ROOT);
                assertParseSuccess(parser, input, new SortCommand(field, direction));
                assertParseSuccess(parser, input.toUpperCase(Locale.ROOT), new SortCommand(field, direction));
                assertParseSuccess(parser, field.name().substring(0, 1)
                        + field.name().substring(1).toLowerCase(Locale.ROOT) + " " + direction.name(),
                        new SortCommand(field, direction));
            }
        }
    }

    @Test
    public void parse_surroundingAndSeparatingWhitespace_acceptsInput() {
        assertParseSuccess(parser, "  UpDaTeD   DeSc  ", new SortCommand(SortField.UPDATED, SortDirection.DESC));
        assertParseSuccess(parser, "  name   asc  ", new SortCommand(SortField.NAME, SortDirection.ASC));
        assertParseSuccess(parser, "\tmajor\tdesc\t", new SortCommand(SortField.MAJOR, SortDirection.DESC));
    }

    @Test
    public void parse_missingUnsupportedRepeatedOrExtraArguments_showsUsage() {
        for (String input : new String[]{"", "   ", "name", "asc", "name asc extra", "name asc desc",
            "name name asc", "phone asc", "tag desc", "year ascending", "name 1", "asc name", "n/asc",
            "name asc m/Computing", "graduationYear asc", "class year asc", "updated", "updated asc desc",
            "lastUpdated asc", "updated newest", "updated asc 1"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
