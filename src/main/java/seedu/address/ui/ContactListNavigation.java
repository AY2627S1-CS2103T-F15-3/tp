package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.model.person.Person;

/** Keeps keyboard traversal within the command box, list and selected contact's expandable values. */
final class ContactListNavigation {
    private static final double FOCUS_SCROLL_MARGIN = 4;
    private final TextField commandBox;
    private final ListView<Person> contactList;

    /** Installs traversal on the JavaFX thread for a list rendered by {@link PersonListPanel}. */
    ContactListNavigation(Parent root, TextField commandBox, ListView<Person> contactList) {
        requireAllNonNull(root, commandBox, contactList);
        assert Platform.isFxApplicationThread() : "Contact navigation must be installed on the JavaFX thread";
        this.commandBox = commandBox;
        this.contactList = contactList;
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTab);
    }

    private void handleTab(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB || event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
            return;
        }
        event.consume();
        if (contactList.getItems().isEmpty()) {
            commandBox.requestFocus();
            return;
        }
        if (contactList.getSelectionModel().getSelectedIndex() < 0) {
            contactList.getSelectionModel().selectFirst();
        }
        Node focused = commandBox.getScene().getFocusOwner();
        if (focused == commandBox) {
            contactList.scrollTo(contactList.getSelectionModel().getSelectedIndex());
        }
        List<Node> route = getFocusRoute();
        int current = route.indexOf(focused);
        int next = current < 0 ? 0 : Math.floorMod(current + (event.isShiftDown() ? -1 : 1), route.size());
        Node target = route.get(next);
        target.requestFocus();
        if (next > 1) {
            scrollValueIntoView(target);
        }
    }

    private void scrollValueIntoView(Node value) {
        contactList.layout();
        Bounds viewport = contactList.localToScene(contactList.getBoundsInLocal());
        Bounds bounds = value.localToScene(value.getBoundsInLocal());
        double top = viewport.getMinY() + FOCUS_SCROLL_MARGIN;
        double bottom = viewport.getMaxY() - FOCUS_SCROLL_MARGIN;
        double offset = bounds.getMinY() < top || bounds.getHeight() > bottom - top
                ? bounds.getMinY() - top : Math.max(0, bounds.getMaxY() - bottom);
        // PersonListPanel uses the standard JavaFX list skin, which exposes its virtualized viewport.
        Node viewportNode = contactList.lookup(".virtual-flow");
        assert viewportNode instanceof VirtualFlow<?> : "Contact list skin must provide a VirtualFlow";
        VirtualFlow<?> flow = (VirtualFlow<?>) viewportNode;
        flow.scrollPixels(offset);
    }

    private List<Node> getFocusRoute() {
        // A scroll or resize may have recycled the selected cell since the previous key press.
        contactList.applyCss();
        contactList.layout();
        List<Node> route = new ArrayList<>();
        route.add(commandBox);
        route.add(contactList);
        int selectedIndex = contactList.getSelectionModel().getSelectedIndex();
        contactList.lookupAll(".list-cell").stream()
                .filter(PersonListCell.class::isInstance)
                .map(PersonListCell.class::cast)
                .filter(cell -> cell.getIndex() == selectedIndex)
                .findFirst()
                .ifPresent(cell -> route.addAll(cell.getExpandableValues()));
        return route;
    }
}
