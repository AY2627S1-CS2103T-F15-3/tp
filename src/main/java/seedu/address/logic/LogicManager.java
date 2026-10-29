package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String MESSAGE_UNSAVED_CHANGE = "\nThe change made by this command has been applied"
            + ", but it is not saved to the local data file. Running %1$s now would revert this change. "
            + "If you close UniMate now, this change will be lost. To keep it, fix the error, "
            + "then run list to save the change locally.";

    public static final String MESSAGE_PENDING_UNSAVED_CHANGES = "\nEarlier contact changes have not been saved. "
            + "Fix the error, then run list to retry. Closing UniMate now will lose those changes.";

    public static final String MESSAGE_UNSAVED_EXIT = "\nUniMate is still open because some contact changes "
            + "have not been saved. Fix the error, then run list or exit to retry.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    private List<Person> lastSavedPersons;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
        lastSavedPersons = List.copyOf(model.getAddressBook().getPersonList());
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        logger.info("Executing command: " + command.getClass().getSimpleName());
        List<Person> personsBeforeCommand = List.copyOf(model.getAddressBook().getPersonList());
        commandResult = command.execute(model);

        if (commandResult.isShowHelp() || (commandResult.isExit() && !hasUnsavedChanges())) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
            lastSavedPersons = List.copyOf(model.getAddressBook().getPersonList());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage())
                    + describeUnsavedChange(command, commandResult, personsBeforeCommand), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage())
                    + describeUnsavedChange(command, commandResult, personsBeforeCommand), ioe);
        }

        return commandResult;
    }

    /**
     * Returns the relevant recovery note when contact changes remain unsaved.
     */
    private String describeUnsavedChange(Command command, CommandResult result, List<Person> personsBeforeCommand) {
        if (!hasUnsavedChanges()) {
            return "";
        }
        if (result.isExit()) {
            return MESSAGE_UNSAVED_EXIT;
        }
        if (personsBeforeCommand.equals(model.getAddressBook().getPersonList())) {
            return MESSAGE_PENDING_UNSAVED_CHANGES;
        }
        // An undo is reverted by redo; every other change is reverted by undo.
        String revertingCommand = command instanceof UndoCommand ? RedoCommand.COMMAND_WORD : UndoCommand.COMMAND_WORD;
        return String.format(MESSAGE_UNSAVED_CHANGE, revertingCommand);
    }

    private boolean hasUnsavedChanges() {
        return !lastSavedPersons.equals(model.getAddressBook().getPersonList());
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ReadOnlyStringProperty viewStatusProperty() {
        return model.viewStatusProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
