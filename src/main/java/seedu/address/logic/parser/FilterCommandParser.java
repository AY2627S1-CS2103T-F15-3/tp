package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GRADUATION_YEAR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MAJOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<!\\S)([a-zA-Z]/)");
    private static final Set<String> SUPPORTED_PREFIXES = Set.of(PREFIX_MAJOR.getPrefix(),
            PREFIX_GRADUATION_YEAR.getPrefix(), PREFIX_TAG.getPrefix());

    /**
     * Parses at least one criterion, rejecting repeated prefixes, unsupported prefixes and invalid values.
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
        rejectUnsupportedPrefixes(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args,
                PREFIX_MAJOR, PREFIX_GRADUATION_YEAR, PREFIX_TAG);
        if (!arguments.getPreamble().isEmpty()
                || (arguments.getValue(PREFIX_MAJOR).isEmpty()
                && arguments.getValue(PREFIX_GRADUATION_YEAR).isEmpty()
                && arguments.getValue(PREFIX_TAG).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_MAJOR, PREFIX_GRADUATION_YEAR, PREFIX_TAG);
        Optional<Major> major = arguments.getValue(PREFIX_MAJOR).isPresent()
                ? Optional.of(ParserUtil.parseMajor(arguments.getValue(PREFIX_MAJOR).get())) : Optional.empty();
        Optional<GraduationYear> year = arguments.getValue(PREFIX_GRADUATION_YEAR).isPresent()
                ? Optional.of(ParserUtil.parseGraduationYear(arguments.getValue(PREFIX_GRADUATION_YEAR).get()))
                : Optional.empty();
        Optional<Tag> tag = arguments.getValue(PREFIX_TAG).isPresent()
                ? Optional.of(ParserUtil.parseTag(arguments.getValue(PREFIX_TAG).get())) : Optional.empty();
        return new FilterCommand(new ContactFilterPredicate(major, year, tag));
    }

    private void rejectUnsupportedPrefixes(String args) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            if (!SUPPORTED_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
            }
        }
    }
}
