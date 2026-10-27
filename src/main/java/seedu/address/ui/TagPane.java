package seedu.address.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

/**
 * Wraps tags into rows, measuring expanded tags at the width they actually receive.
 */
public class TagPane extends FlowPane {
    private static final double ROUNDING_TOLERANCE = 0.01;

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefHeight(double width) {
        return arrangeTags(width, false);
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        arrangeTags(getWidth(), true);
    }

    /** Uses one row calculation for both preferred height and node placement. */
    private double arrangeTags(double width, boolean shouldLayout) {
        double left = snappedLeftInset();
        double right = Math.max(left, width - snappedRightInset());
        double rowX = left;
        double rowY = snappedTopInset();
        double rowHeight = 0;
        for (Node tag : getManagedChildren()) {
            double tagWidth = Math.min(tag.prefWidth(-1), Math.max(0, right - left));
            double tagHeight = tag.prefHeight(tagWidth);
            if (rowX > left && rowX + tagWidth > right + ROUNDING_TOLERANCE) {
                rowX = left;
                rowY += rowHeight + getVgap();
                rowHeight = 0;
            }
            if (shouldLayout) {
                tag.resizeRelocate(rowX, rowY, tagWidth, tagHeight);
            }
            rowX += tagWidth + getHgap();
            rowHeight = Math.max(rowHeight, tagHeight);
        }
        return rowY + rowHeight + snappedBottomInset();
    }
}
