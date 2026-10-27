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

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
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
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonContainsKeywordsPredicate;
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
        String clearUsage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, clearUsage, () -> parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), Index.fromOneBased(5)),
                parser.parseCommand("DELETE\u00A01\u20073\u202F5"));
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
        String exitUsage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, exitUsage, () -> parser.parseCommand(ExitCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new PersonContainsKeywordsPredicate(keywords)), command);
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
    public void parseCommand_unicodeSpaces_treatedAsSeparators() throws Exception {
        // Command word followed by a Unicode space
        assertTrue(parser.parseCommand("list\u2003") instanceof ListCommand);
        assertTrue(parser.parseCommand("\u3000list\u00A0") instanceof ListCommand);

        // Keywords separated by Unicode spaces
        assertEquals(new FindCommand(new PersonContainsKeywordsPredicate(List.of("Computer", "Science"))),
                parser.parseCommand("find\u2003Computer\u2003Science"));

        // Prefixes preceded by Unicode spaces, and Unicode spaces within values
        FilterCommand expected = new FilterCommand(new ContactFilterPredicate(
                Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.empty()));
        assertEquals(expected, parser.parseCommand("filter\u2003m/Computer\u2003Science\u00A0c/2027\u2003"));
    }

    @Test
    public void parseCommand_unicodeSpaceVariants_sameResultAsAsciiSpaces() {
        // Note text is kept as typed, so note inputs below only use spaces as separators.
        List<String> asciiInputs = List.of(
                "add n/Bob a/Main St", "add  n/Bob   a/Main St ",
                "add n/Bob p/98765432 e/bob@example.com a/Main St m/Computer Science c/2027 t/friend t/CS",
                "add n/Bob", "add",
                "fav 1", "fav", "fav x", "fav 1 2", "unfav 1", "unfav",
                "note 1 Hello", "note 1", "note x Hello", "note",
                "shownote 1", "shownote", "delnote 1", "delnote",
                "tag 1 t/NOC", "tag 1 d/1 t/NOC", "tag 1", "untag 1 t/NOC t/CS", "untag 1",
                "tags", "tags x",
                "edit 1 n/Bob m/Physics Honours", "edit 1 c/2028 t/", "edit 1", "edit",
                "delete 1", "delete",
                "clear", "exit", "help",
                "sort name asc", "sort year desc", "sort name",
                "filter m/Computer Science c/2027 t/Classmate", "filter",
                "find alice bob", "find",
                "list", "list x", "listfav", "listfav x",
                "undo", "undo x", "redo", "redo x",
                "foo bar", "LIST");

        for (String asciiInput : asciiInputs) {
            for (String space : new String[]{"\u00A0", "\u2003", "\u3000", "\u202F", "\t"}) {
                String variant = asciiInput.replace(" ", space);
                assertSameParseResult(asciiInput, variant);
                assertSameParseResult(asciiInput, space + variant + space);
            }
        }
    }

    /**
     * Asserts that {@code variant} parses to a command equal to that of {@code expectedInput},
     * or fails with the same message.
     */
    private void assertSameParseResult(String expectedInput, String variant) {
        Command expected;
        try {
            expected = parser.parseCommand(expectedInput);
        } catch (ParseException e) {
            assertThrows(ParseException.class, e.getMessage(), () -> parser.parseCommand(variant));
            return;
        }

        try {
            Command actual = parser.parseCommand(variant);
            assertEquals(expected.getClass(), actual.getClass(), variant);
            // Commands without fields do not override equals, so for those the type check above is enough.
            if (overridesEquals(expected)) {
                assertEquals(expected, actual, variant);
            }
        } catch (ParseException e) {
            throw new AssertionError("Expected [" + variant + "] to parse like [" + expectedInput + "]", e);
        }
    }

    private static boolean overridesEquals(Command command) {
        try {
            return command.getClass().getMethod("equals", Object.class).getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void parseCommand_surroundingControlCharacters_stripped() throws Exception {
        // Matches the previous String#trim() behaviour.
        assertTrue(parser.parseCommand("\u0000list\u0001") instanceof ListCommand);
        assertTrue(parser.parseCommand("list\u2003\u0001") instanceof ListCommand);
    }

    @Test
    public void parseCommand_noteWithUnicodeSpaces_textNotNormalised() throws Exception {
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met at\u2003CS2103T")),
                parser.parseCommand("note\u20031\u2003Met at\u2003CS2103T"));
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met at\u00A0CS2103T")),
                parser.parseCommand("note\u00A01\u00A0Met at\u00A0CS2103T"));
    }

    @Test
    public void parseCommand_uppercaseNoteWithUnicodeSpaces_textNotNormalised() throws Exception {
        // Command words are case-insensitive, so any casing of note must keep the text as typed.
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met\u00A0at\u2003CS2103T")),
                parser.parseCommand("NOTE 1 Met\u00A0at\u2003CS2103T"));
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Notes("Met\u00A0at\u2003CS2103T")),
                parser.parseCommand("Note 1 Met\u00A0at\u2003CS2103T"));
    }

    @Test
    public void parseCommand_noteWithSurroundingUnicodeSpaces_trimsEdgesAndPreservesInternalSpaces() throws Exception {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_PERSON, new Notes("Hello\u00A0World"));
        for (String commandWord : List.of("note", "NOTE", "Note")) {
            assertEquals(expected, parser.parseCommand(commandWord + " 1 \u00A0Hello\u00A0World\u202F"));
        }
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
        String helpUsage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, helpUsage, () -> parser.parseCommand(HelpCommand.COMMAND_WORD + " 3"));
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
        Set<Tag> tags = Set.of(new Tag("NOC"));
        assertEquals(new TagCommand(INDEX_FIRST_PERSON, tags), parser.parseCommand("TAG 1 t/NOC t/noc"));
        assertEquals(new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("NOC"))),
                parser.parseCommand("UNTAG 1 t/NOC"));
        assertEquals(new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice", "NUS"))),
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
