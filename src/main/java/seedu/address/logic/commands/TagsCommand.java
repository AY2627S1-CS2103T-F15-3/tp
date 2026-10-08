package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.model.Model;
import seedu.address.model.tag.DefaultTags;

/**
 * Displays the numbered default tags.
 */
public class TagsCommand extends Command {
    public static final String COMMAND_WORD = "tags";
    public static final String MESSAGE_USAGE = "tags: Displays the default tags.\nUsage: tags";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(IntStream.range(0, DefaultTags.TAGS.size())
                .mapToObj(i -> (i + 1) + ". " + DefaultTags.TAGS.get(i).tagName)
                .collect(Collectors.joining("\n")));
    }
}
