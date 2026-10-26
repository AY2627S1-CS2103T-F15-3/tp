package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.shape.Rectangle;
import seedu.address.model.person.Person;

/**
 * Fits a contact card to the list viewport and measures its height at that same width.
 * Expansion state belongs to the contact, so recycling a cell does not collapse its fields.
 */
final class PersonListCell extends ListCell<Person> {
    // Matches the vertical scrollbar width in DarkTheme.css.
    private static final double SCROLLBAR_WIDTH = 16;

    private final Map<Person, Set<String>> expandedFields;
    private PersonCard card;

    PersonListCell(ListView<Person> listView, Map<Person, Set<String>> expandedFields) {
        this.expandedFields = requireNonNull(expandedFields);
        requireNonNull(listView);
        prefWidthProperty().bind(Bindings.max(0, listView.widthProperty().subtract(SCROLLBAR_WIDTH)));
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    @Override
    protected void updateItem(Person person, boolean empty) {
        super.updateItem(person, empty);
        setText(null);
        if (empty || person == null) {
            card = null;
            setGraphic(null);
        } else {
            card = new PersonCard(person, getIndex() + 1, Clock.systemDefaultZone(),
                    expandedFields.computeIfAbsent(person, ignored -> new HashSet<>()));
            card.setOnContentChanged(this::requestContentLayout);
            setGraphic(card.getRoot());
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        return 0;
    }

    @Override
    protected double computePrefHeight(double width) {
        if (card == null) {
            return 0;
        }
        double cellWidth = width > 0 ? width : getWidth();
        if (cellWidth <= 0) {
            cellWidth = getViewportWidth();
        }
        double contentWidth = Math.max(0, cellWidth - snappedLeftInset() - snappedRightInset());
        return snappedTopInset() + card.getRoot().prefHeight(contentWidth) + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (card != null) {
            double width = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
            card.getRoot().resizeRelocate(snappedLeftInset(), snappedTopInset(),
                    width, card.getRoot().prefHeight(width));
        }
    }

    private double getViewportWidth() {
        return getListView() == null ? 0 : Math.max(0, getListView().getWidth() - SCROLLBAR_WIDTH);
    }

    private void requestContentLayout() {
        requestLayout();
        if (getListView() != null) {
            getListView().requestLayout();
        }
    }
}
