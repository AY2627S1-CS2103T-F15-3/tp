package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ShowNoteCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSortComparator.SortDirection;
import seedu.address.model.person.PersonSortComparator.SortField;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_filter_routesCriteriaToFilterCommand() throws Exception {
        FilterCommand expected = new FilterCommand(new ContactFilterPredicate(
                Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.of(new Tag("Classmate"))));
        assertEquals(expected, parser.parseCommand("filter m/Computer Science c/2027 t/Classmate"));
        assertEquals(expected, parser.parseCommand("  filter   t/Classmate c/2027 m/Computer Science  "));
    }

    @Test
    public void parseCommand_filterWithoutCriteria_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("filter"));
    }

    @Test
    public void parseCommand_sort_routesFieldAndDirection() throws Exception {
        assertEquals(new SortCommand(SortField.NAME, SortDirection.ASC), parser.parseCommand("SoRt Name ASC"));
        assertEquals(new SortCommand(SortField.NAME, SortDirection.ASC), parser.parseCommand("sort name asc"));
        assertEquals(new SortCommand(SortField.MAJOR, SortDirection.DESC),
                parser.parseCommand("  sort   major  desc  "));
        assertEquals(new SortCommand(SortField.YEAR, SortDirection.ASC), parser.parseCommand("sort year asc"));
    }

    @Test
    public void parseCommand_invalidSort_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("sort"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("sort name"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("sort name asc extra"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("SORT name"));
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand("  list  \t ") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        for (String input : List.of("list 3", "list all", "list t/Friend", "list \t extra")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("LIST all"));
    }

    @Test
    public void parseCommand_undo() throws Exception {
        assertTrue(parser.parseCommand(UndoCommand.COMMAND_WORD) instanceof UndoCommand);
        assertTrue(parser.parseCommand("  " + UndoCommand.COMMAND_WORD + "   ") instanceof UndoCommand);
    }

    @Test
    public void parseCommand_undoWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(UndoCommand.COMMAND_WORD + " 1"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(UndoCommand.COMMAND_WORD
                + " all"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("UNDO 1"));
    }

    @Test
    public void parseCommand_notes() throws Exception {
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met during CS2103T")),
                parser.parseCommand("note 1 Met during CS2103T"));
        assertEquals(new ShowNoteCommand(INDEX_FIRST_PERSON), parser.parseCommand("shownote 1"));
        assertEquals(new DeleteNoteCommand(INDEX_FIRST_PERSON), parser.parseCommand("delnote 1"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_redo() throws Exception {
        assertTrue(parser.parseCommand(RedoCommand.COMMAND_WORD) instanceof RedoCommand);
        assertTrue(parser.parseCommand("  " + RedoCommand.COMMAND_WORD + "   ") instanceof RedoCommand);
    }

    @Test
    public void parseCommand_redoWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(RedoCommand.COMMAND_WORD + " 1"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(RedoCommand.COMMAND_WORD
                + " all"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("REDO 1"));
    }

    @Test
    public void parseCommand_allCommandWords_acceptDifferentCases() throws Exception {
        List<String> inputs = List.of(
                "add n/Alice a/NUS", "fav 1", "unfav 1", "note 1 Met at NUS",
                "shownote 1", "delnote 1", "tag 1 t/NOC", "untag 1 t/NOC", "tags",
                "edit 1 n/Alice", "delete 1", "clear", "sort name asc",
                "filter t/NOC", "find Alice", "list", "listfav", "undo", "exit", "help", "redo");
        for (String input : inputs) {
            int separator = input.indexOf(' ');
            String word = separator < 0 ? input : input.substring(0, separator);
            String arguments = separator < 0 ? "" : input.substring(separator);
            String mixedCase = word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
            assertEquals(parser.parseCommand(input).getClass(),
                    parser.parseCommand(word.toUpperCase(Locale.ROOT) + arguments).getClass(), input);
            assertEquals(parser.parseCommand(input).getClass(),
                    parser.parseCommand(mixedCase + arguments).getClass(), input);
        }
        assertEquals(parser.parseCommand("untag 1 t/NOC"), parser.parseCommand("UnTag 1 t/NOC"));
        assertEquals(parser.parseCommand("unfav 1"), parser.parseCommand("UnFav 1"));
        assertEquals(parser.parseCommand("listfav").getClass(), parser.parseCommand("ListFav").getClass());
    }

    @Test
    public void parseCommand_commandWordNormalization_preservesArguments() throws Exception {
        Set<Tag> tags = Set.of(new Tag("NOC"), new Tag("noc"));
        assertEquals(new TagCommand(INDEX_FIRST_PERSON, tags), parser.parseCommand("TAG 1 t/NOC t/noc"));
        assertEquals(new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("NOC"))),
                parser.parseCommand("UNTAG 1 t/NOC"));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "NUS"))),
                parser.parseCommand("FIND Alice NUS"));
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met at NUS t/NOC")),
                parser.parseCommand("NOTE 1 Met at NUS t/NOC"));
        Person person = new PersonBuilder().build();
        String addInput = PersonUtil.getAddCommand(person);
        assertEquals(new AddCommand(person), parser.parseCommand("ADD" + addInput.substring(3)));
    }

    @Test
    public void parseCommand_uppercasePrefix_stillRejected() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("TAG 1 T/NOC"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("UNKNOWNCOMMAND"));
    }
}
