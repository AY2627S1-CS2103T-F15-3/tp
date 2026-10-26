package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * Controls a label's collapsed and expanded presentation without changing its underlying value.
 * The shared state allows the same field to remain expanded when a list cell is recreated.
 */
final class ExpandableText {
    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass EXPANDABLE = PseudoClass.getPseudoClass("expandable");
    private static final String ACCESSIBLE_HELP = "Click or press Enter to expand or collapse the full value.";

    private final Label label;
    private final String fieldKey;
    private final String fullText;
    private final String collapsedText;
    private final Set<String> expandedFields;
    private final Runnable onContentChanged;
    private final Text textMeasure;

    /** Attaches expansion controls to a label, using the supplied contact's display state. */
    ExpandableText(Label label, String fieldKey, String fullText, String collapsedText,
            Set<String> expandedFields, Runnable onContentChanged) {
        this.label = requireNonNull(label);
        this.fieldKey = requireNonNull(fieldKey);
        this.fullText = requireNonNull(fullText);
        this.collapsedText = requireNonNull(collapsedText);
        this.expandedFields = requireNonNull(expandedFields);
        this.onContentChanged = requireNonNull(onContentChanged);
        textMeasure = new Text(fullText);

        configureLabel();
        registerHandlers();
        updateDisplay();
        updateAvailability();
    }

    private void configureLabel() {
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxHeight(Region.USE_PREF_SIZE);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setEllipsisString("...");
    }

    private void registerHandlers() {
        label.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                toggleExpansion();
            }
        });
        label.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                toggleExpansion();
                event.consume();
            }
        });
        label.widthProperty().addListener((observable, oldValue, newValue) -> updateAvailability());
        label.fontProperty().addListener((observable, oldValue, newValue) -> updateAvailability());
    }

    private boolean isExpanded() {
        return expandedFields.contains(fieldKey);
    }

    private void toggleExpansion() {
        if (!label.isFocusTraversable()) {
            return;
        }
        if (isExpanded()) {
            expandedFields.remove(fieldKey);
        } else {
            expandedFields.add(fieldKey);
        }
        updateDisplay();
        updateAvailability();
        onContentChanged.run();
    }

    private void updateDisplay() {
        boolean isExpanded = isExpanded();
        label.setText(isExpanded ? fullText : collapsedText);
        label.setWrapText(isExpanded);
        label.pseudoClassStateChanged(EXPANDED, isExpanded);
    }

    private void updateAvailability() {
        textMeasure.setFont(label.getFont());
        double availableWidth = label.getWidth() - label.getInsets().getLeft() - label.getInsets().getRight();
        boolean isExpandable = isExpanded() || !fullText.equals(collapsedText)
                || textMeasure.getLayoutBounds().getWidth() > availableWidth;
        label.setFocusTraversable(isExpandable);
        label.pseudoClassStateChanged(EXPANDABLE, isExpandable);
        label.setAccessibleHelp(isExpandable ? ACCESSIBLE_HELP : "");
    }
}
