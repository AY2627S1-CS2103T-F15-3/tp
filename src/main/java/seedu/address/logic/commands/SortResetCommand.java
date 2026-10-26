package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;

/**
 * Restores saved contact order while preserving the active filter and history.
 */
public class SortResetCommand extends Command {
    public static final String MESSAGE_SUCCESS = "Restored saved contact order. Current filter retained.";

    private static final Logger logger = LogsCenter.getLogger(SortResetCommand.class);

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.resetSortedPersonList();
        logger.info("Sort reset to saved contact order.");
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SortResetCommand;
    }

    @Override
    public int hashCode() {
        return SortResetCommand.class.hashCode();
    }
}
