package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Sizes a contact card from its available width, using the same measurements during layout.
 * Preparing the detail columns before measurement prevents stale heights after resizing.
 */
public class PersonCardPane extends VBox {
    private static final double TWO_COLUMN_MIN_WIDTH = 560;
    private static final double COLUMN_GAP = 24;

    private GridPane details;
    private Node contactDetails;
    private Node academicDetails;

    /** Creates a card container that clips its contents to its current bounds. */
    public PersonCardPane() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    /** Supplies the detail groups after FXML has created the card's children. */
    void configureDetails(GridPane details, Node contactDetails, Node academicDetails) {
        this.details = requireNonNull(details);
        this.contactDetails = requireNonNull(contactDetails);
        this.academicDetails = requireNonNull(academicDetails);
        requestLayout();
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = Math.max(0, width - snappedLeftInset() - snappedRightInset());
        updateDetailColumns(contentWidth);
        double height = snappedTopInset() + snappedBottomInset();
        for (Node child : getManagedChildren()) {
            height += child.prefHeight(contentWidth);
        }
        return height + Math.max(0, getManagedChildren().size() - 1) * getSpacing();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        double contentWidth = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
        updateDetailColumns(contentWidth);
        double top = snappedTopInset();
        for (Node child : getManagedChildren()) {
            double height = child.prefHeight(contentWidth);
            child.resizeRelocate(snappedLeftInset(), top, contentWidth, height);
            top += height + getSpacing();
        }
    }

    private void updateDetailColumns(double width) {
        if (details == null) {
            return;
        }
        boolean isWide = width >= TWO_COLUMN_MIN_WIDTH;
        int columnSpan = isWide ? 1 : 2;
        if (Integer.valueOf(columnSpan).equals(GridPane.getColumnSpan(contactDetails))) {
            return;
        }
        GridPane.setColumnSpan(contactDetails, columnSpan);
        GridPane.setColumnSpan(academicDetails, columnSpan);
        GridPane.setColumnIndex(academicDetails, isWide ? 1 : 0);
        GridPane.setRowIndex(academicDetails, isWide ? 0 : 1);
        details.setHgap(isWide ? COLUMN_GAP : 0);
    }
}
