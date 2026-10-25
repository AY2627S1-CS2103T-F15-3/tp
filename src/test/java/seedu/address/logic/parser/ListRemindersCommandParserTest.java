package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListRemindersCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.SystemTag;

public class ListRemindersCommandParserTest {
    private final ListRemindersCommandParser parser = new ListRemindersCommandParser();
    private final AddressBookParser addressBookParser = new AddressBookParser();

    @Test
    public void parse_noCriterion_matchesEitherReminder() {
        assertParseSuccess(parser, "", new ListRemindersCommand());
        assertParseSuccess(parser, " \t\n ", new ListRemindersCommand());
    }

    @Test
    public void parse_singleCriterion_caseInsensitive() {
        assertParseSuccess(parser, " update ", new ListRemindersCommand(Set.of(SystemTag.UPDATE)));
        assertParseSuccess(parser, "UpDaTe", new ListRemindersCommand(Set.of(SystemTag.UPDATE)));
        assertParseSuccess(parser, "incomplete", new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE)));
        assertParseSuccess(parser, " INCOMPLETE ", new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE)));
    }

    @Test
    public void parse_invalidOrExtraCriteria_rejected() {
        for (String input : new String[]{"all", "t/update", "1", "update incomplete", "update update",
            "incomplete extra", "update\tincomplete", "update,incomplete", "in complete"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListRemindersCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parseCommand_registeredWithCaseInsensitiveCommandWord() throws ParseException {
        assertEquals(new ListRemindersCommand(), addressBookParser.parseCommand("listreminders"));
        assertEquals(new ListRemindersCommand(Set.of(SystemTag.UPDATE)),
                addressBookParser.parseCommand("LISTREMINDERS UPDATE"));
        assertEquals(new ListRemindersCommand(Set.of(SystemTag.INCOMPLETE)),
                addressBookParser.parseCommand(" listreminders\u00a0INCOMPLETE "));
    }

    @Test
    public void parseCommand_extraCriteria_rejected() {
        String input = "listreminders update incomplete";
        ParseException error = assertThrows(ParseException.class, () -> addressBookParser.parseCommand(input));
        assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListRemindersCommand.MESSAGE_USAGE),
                error.getMessage());
    }
}
