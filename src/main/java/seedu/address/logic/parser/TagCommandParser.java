package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEFAULT_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a person index and one or more default tag indices.
 */
public class TagCommandParser implements Parser<TagCommand> {
    @Override
    public TagCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_DEFAULT_TAG);
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE), e);
        }
        if (arguments.getAllValues(PREFIX_DEFAULT_TAG).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        }
        return new TagCommand(index, ParserUtil.parseDefaultTags(arguments.getAllValues(PREFIX_DEFAULT_TAG)));
    }
}
