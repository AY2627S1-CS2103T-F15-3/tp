package seedu.address.ui;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory commandHistory = new CommandHistory();
    private boolean recallingHistory;
    private final FadeTransition historyFeedback = new FadeTransition(Duration.millis(200));

    @FXML
    private TextField commandTextField;

    @FXML
    private Label historyPositionLabel;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        historyFeedback.setNode(historyPositionLabel);
        historyFeedback.setFromValue(0.35);
        historyFeedback.setToValue(1.0);
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryNavigation);
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            if (!recallingHistory) {
                commandHistory.resetPrefixSearch();
            }
        });
    }

    TextField getTextField() {
        return commandTextField;
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        hideHistoryPosition();
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        commandHistory.add(commandText);
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Recalls submitted commands without executing them.
     */
    private void handleHistoryNavigation(KeyEvent event) {
        if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN) {
            return;
        }
        String text;
        if (event.isAltDown() && !event.isControlDown() && !event.isMetaDown() && !event.isShiftDown()) {
            text = event.getCode() == KeyCode.UP
                    ? commandHistory.previousMatching(commandTextField.getText()) : commandHistory.nextMatching();
        } else {
            text = event.getCode() == KeyCode.UP
                    ? commandHistory.previous(commandTextField.getText()) : commandHistory.next();
        }
        if (text != null) {
            recallingHistory = true;
            try {
                commandTextField.setText(text);
                commandTextField.positionCaret(text.length());
            } finally {
                recallingHistory = false;
            }
        }
        showHistoryPosition();
        event.consume();
    }

    /**
     * Shows the absolute history position and restarts feedback even when the text did not change.
     */
    private void showHistoryPosition() {
        if (commandHistory.getPosition() == 0) {
            hideHistoryPosition();
            return;
        }
        historyPositionLabel.setText("History " + commandHistory.getPosition() + "/" + commandHistory.size());
        historyPositionLabel.setVisible(true);
        historyPositionLabel.setManaged(true);
        historyFeedback.stop();
        historyPositionLabel.setOpacity(0.35);
        historyFeedback.playFromStart();
    }

    /**
     * Hides feedback at the draft or on submission, releasing the space beside the command box.
     */
    private void hideHistoryPosition() {
        historyFeedback.stop();
        historyPositionLabel.setOpacity(1.0);
        historyPositionLabel.setVisible(false);
        historyPositionLabel.setManaged(false);
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
