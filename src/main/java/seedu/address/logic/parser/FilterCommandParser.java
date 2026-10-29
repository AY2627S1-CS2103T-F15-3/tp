package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADUATION_YEAR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAJOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFilterPredicate;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.tag.Tag;

/**
 * Parses independently optional major, graduation-year and tag filter criteria.
 */
public class FilterCommandParser implements Parser<FilterCommand> {
    private static final Logger logger = LogsCenter.getLogger(FilterCommandParser.class);

    /**
     * Parses at least one criterion, rejecting repeated prefixes, unexpected preamble and invalid values.
     * Prefix-like text inside a major is retained unless it is a supported criterion prefix.
     *
     * @throws ParseException if the input is not a valid filter argument string.
     */
    @Override
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            return parseCriteria(args);
        } catch (ParseException e) {
            logger.fine("Filter arguments rejected: " + e.getMessage());
            throw e;
        }
    }

    private FilterCommand parseCriteria(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args,
                PREFIX_MAJOR, PREFIX_GRADUATION_YEAR, PREFIX_TAG);
        Optional<String> majorValue = arguments.getValue(PREFIX_MAJOR);
        Optional<String> yearValue = arguments.getValue(PREFIX_GRADUATION_YEAR);
        Optional<String> tagValue = arguments.getValue(PREFIX_TAG);
        boolean hasCriteria = majorValue.isPresent() || yearValue.isPresent() || tagValue.isPresent();
        if (!arguments.getPreamble().isEmpty() || !hasCriteria) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_MAJOR, PREFIX_GRADUATION_YEAR, PREFIX_TAG);
        Optional<Major> major = majorValue.isPresent()
                ? Optional.of(ParserUtil.parseMajor(majorValue.get())) : Optional.empty();
        Optional<GraduationYear> year = yearValue.isPresent()
                ? Optional.of(ParserUtil.parseGraduationYear(yearValue.get())) : Optional.empty();
        Optional<Tag> tag = tagValue.isPresent()
                ? Optional.of(ParserUtil.parseTag(tagValue.get())) : Optional.empty();
        return new FilterCommand(new ContactFilterPredicate(major, year, tag));
    }
}
