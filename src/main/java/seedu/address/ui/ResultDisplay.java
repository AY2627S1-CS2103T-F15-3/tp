package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String WARNING_STYLE_CLASS = "warning";

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Shows ordinary feedback using the normal theme colour, clearing previous warning styling.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /**
     * Shows the complete feedback in the original text area, starting at the top of the message.
     * A warning uses amber text; other feedback uses the normal theme colour without a warning border.
     */
    public void setFeedbackToUser(String feedbackToUser, boolean warning) {
        requireNonNull(feedbackToUser);
        resultDisplay.getStyleClass().remove(WARNING_STYLE_CLASS);
        if (warning) {
            resultDisplay.getStyleClass().add(WARNING_STYLE_CLASS);
        }
        resultDisplay.setText(feedbackToUser);
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
    }

}
