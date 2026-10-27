package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Checks actual JavaFX focus traversal rather than only the calculated navigation order. */
public class ContactListNavigationTest {
    private static final String LONG_TAG = "T".repeat(500);

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void tabAndShiftTab_visitOnlySelectedContactInReadingOrder() throws Exception {
        try (View view = open(List.of(longContact("First"), longContact("Second")))) {
            FxTestUtil.runOnFxThread(() -> {
                assertSame(view.command, view.focused());
                view.press(KeyCode.TAB, false);
                assertSame(view.list, view.focused());
                assertEquals(0, view.list.getSelectionModel().getSelectedIndex());
                view.press(KeyCode.DOWN, false);
                assertEquals(1, view.list.getSelectionModel().getSelectedIndex());
                List<Label> values = view.selectedCell().getExpandableValues();
                assertEquals(List.of("name", "tag", "phone", "address", "email", "major"),
                        values.stream().map(label -> label.getId() == null ? "tag" : label.getId()).toList());
                for (Label value : values) {
                    view.press(KeyCode.TAB, false);
                    assertSame(value, view.focused());
                }
                view.press(KeyCode.TAB, false);
                assertSame(view.command, view.focused());
                for (int index = values.size() - 1; index >= 0; index--) {
                    view.press(KeyCode.TAB, true);
                    assertSame(values.get(index), view.focused());
                }
                view.press(KeyCode.TAB, true);
                assertSame(view.list, view.focused());
                view.press(KeyCode.UP, false);
                assertEquals(0, view.list.getSelectionModel().getSelectedIndex());
                view.press(KeyCode.TAB, true);
                assertSame(view.command, view.focused());
                assertEquals("draft command", view.command.getText());
                return null;
            });
        }
    }

    @Test
    public void shortSelectedContact_skipsLongValuesOnOtherCards() throws Exception {
        try (View view = open(List.of(new PersonBuilder().withName("Short").build(), longContact("Other")))) {
            FxTestUtil.runOnFxThread(() -> {
                view.press(KeyCode.TAB, false);
                assertSame(view.list, view.focused());
                assertTrue(view.selectedCell().getExpandableValues().isEmpty());
                view.press(KeyCode.TAB, false);
                assertSame(view.command, view.focused());
                view.press(KeyCode.TAB, true);
                assertSame(view.list, view.focused());
                view.press(KeyCode.TAB, true);
                assertSame(view.command, view.focused());
                return null;
            });
        }
    }

    @Test
    public void emptyList_bothDirectionsKeepCommandFocus() throws Exception {
        try (View view = open(List.of())) {
            FxTestUtil.runOnFxThread(() -> {
                view.press(KeyCode.TAB, false);
                assertSame(view.command, view.focused());
                view.press(KeyCode.TAB, true);
                assertSame(view.command, view.focused());
                return null;
            });
        }
    }

    @Test
    public void focusedLongTag_hasOrangeOutlineAndKeyboardExpansionKeepsFocus() throws Exception {
        try (View view = open(List.of(longContact("Long")))) {
            FxTestUtil.runOnFxThread(() -> {
                view.press(KeyCode.TAB, false);
                view.press(KeyCode.TAB, false);
                view.press(KeyCode.TAB, false);
                Label tag = (Label) view.focused();
                assertEquals(LONG_TAG, tag.getTooltip().getText());
                assertSame(tag, view.focused());
                // Check focus styling independently of the desktop activating the test window.
                tag.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
                FxTestUtil.layout(view.root);
                assertEquals(Color.web("#d99664"), tag.getBorder().getStrokes().get(0).getTopStroke());
                view.press(KeyCode.ENTER, false);
                assertTrue(tag.isWrapText());
                assertSame(tag, view.focused());
                view.press(KeyCode.SPACE, false);
                assertFalse(tag.isWrapText());
                assertSame(tag, view.focused());
                view.press(KeyCode.TAB, false);
                assertEquals("phone", view.focused().getId());
                return null;
            });
        }
    }

    @Test
    public void offscreenSelectionAndResize_rebuildRouteFromSelectedCell() throws Exception {
        List<Person> contacts = IntStream.range(0, 40).mapToObj(index -> longContact("Contact " + index)).toList();
        try (View view = open(contacts)) {
            FxTestUtil.runOnFxThread(() -> {
                view.list.getSelectionModel().select(39);
                view.press(KeyCode.TAB, true);
                assertEquals("major", view.focused().getId());
                assertTrue(view.selectedCell().getExpandableValues().contains(view.focused()));
                view.command.requestFocus();
                view.stage.setWidth(450);
                FxTestUtil.layout(view.root);
                view.press(KeyCode.TAB, false);
                assertSame(view.list, view.focused());
                view.press(KeyCode.TAB, false);
                assertEquals("name", view.focused().getId());
                assertTrue(view.selectedCell().getExpandableValues().contains(view.focused()));
                return null;
            });
        }
    }

    @Test
    public void resultDisplayTab_returnsToCommandBox() throws Exception {
        try (View view = open(List.of(longContact("Long")))) {
            FxTestUtil.runOnFxThread(() -> {
                view.result.requestFocus();
                assertSame(view.result, view.focused());
                view.press(KeyCode.TAB, false);
                assertSame(view.command, view.focused());
                return null;
            });
        }
    }

    @Test
    public void expandedTallCard_tabScrollsFocusedValueIntoView() throws Exception {
        Person tall = new PersonBuilder(longContact("Long"))
                .withName("Long" + " Contact".repeat(250)).build();
        try (View view = open(List.of(tall))) {
            FxTestUtil.runOnFxThread(() -> {
                view.stage.setWidth(450);
                view.root.resize(450, 700);
                FxTestUtil.layout(view.root);
                view.press(KeyCode.TAB, false);
                view.press(KeyCode.TAB, false);
                view.press(KeyCode.ENTER, false);
                assertTrue(view.selectedCell().getHeight() > view.list.getHeight());
                view.press(KeyCode.TAB, false);
                view.press(KeyCode.ENTER, false);
                view.press(KeyCode.TAB, false);
                assertEquals("phone", view.focused().getId());
                Bounds field = view.focused().localToScene(view.focused().getBoundsInLocal());
                Bounds viewport = view.list.localToScene(view.list.getBoundsInLocal());
                assertTrue(field.getMinY() >= viewport.getMinY() - 1);
                assertTrue(field.getMaxY() <= viewport.getMaxY() + 1);
                view.press(KeyCode.TAB, true);
                assertEquals(LONG_TAG, ((Label) view.focused()).getTooltip().getText());
                view.list.scrollTo(0);
                FxTestUtil.layout(view.root);
                view.press(KeyCode.TAB, true);
                assertEquals("name", view.focused().getId());
                Bounds name = view.focused().localToScene(view.focused().getBoundsInLocal());
                assertTrue(name.getMinY() >= viewport.getMinY());
                assertTrue(name.getMinY() <= viewport.getMinY() + 8);
                view.press(KeyCode.TAB, false);
                assertEquals(LONG_TAG, ((Label) view.focused()).getTooltip().getText());
                view.press(KeyCode.TAB, true);
                assertEquals("name", view.focused().getId());
                name = view.focused().localToScene(view.focused().getBoundsInLocal());
                assertTrue(name.getMinY() >= viewport.getMinY());
                assertTrue(name.getMinY() <= viewport.getMinY() + 8);
                return null;
            });
        }
    }

    @Test
    public void modifiedTab_remainsAvailableForPlatformShortcuts() throws Exception {
        try (View view = open(List.of(longContact("Long")))) {
            FxTestUtil.runOnFxThread(() -> {
                AtomicBoolean delivered = new AtomicBoolean();
                view.root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    delivered.set(!event.isConsumed());
                    event.consume();
                });
                for (int modifier = 0; modifier < 3; modifier++) {
                    delivered.set(false);
                    view.command.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                            false, modifier == 0, modifier == 1, modifier == 2));
                    assertTrue(delivered.get());
                    assertSame(view.command, view.focused());
                }
                return null;
            });
        }
    }

    private static Person longContact(String prefix) {
        return new PersonBuilder().withName(prefix + " Long Contact".repeat(30))
                .withPhone("9".repeat(100)).withAddress("Lower Kent Ridge Road ".repeat(15))
                .withEmail("a".repeat(100) + "@example.com").withMajor("Computer Science ".repeat(15))
                .withTags(LONG_TAG).build();
    }

    private View open(List<Person> contacts) throws Exception {
        View view = FxTestUtil.runOnFxThread(() -> new View(contacts));
        CompletableFuture<Void> ready = new CompletableFuture<>();
        FxTestUtil.runOnFxThread(() -> {
            PauseTransition pause = new PauseTransition(Duration.millis(150));
            pause.setOnFinished(event -> {
                FxTestUtil.layout(view.root);
                view.command.requestFocus();
                ready.complete(null);
            });
            pause.play();
            return null;
        });
        ready.get(10, TimeUnit.SECONDS);
        return view;
    }

    private static class View implements AutoCloseable {
        private final Stage stage = new Stage();
        private final TextField command = new TextField("draft command");
        private final TextArea result = new TextArea("Command feedback");
        private final VBox root;
        private final ListView<Person> list;

        View(List<Person> contacts) {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(contacts));
            list = panel.getPersonListView();
            result.setEditable(false);
            result.setPrefHeight(70);
            root = new VBox(command, result, panel.getRoot());
            VBox.setVgrow(panel.getRoot(), Priority.ALWAYS);
            root.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            stage.setScene(new Scene(root, 1000, 700));
            new ContactListNavigation(root, command, list);
            stage.show();
            FxTestUtil.layout(root);
        }

        Node focused() {
            return stage.getScene().getFocusOwner();
        }

        void press(KeyCode key, boolean shift) {
            focused().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, shift, false, false, false));
            FxTestUtil.layout(root);
        }

        PersonListCell selectedCell() {
            return list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof PersonListCell cell
                            && cell.getIndex() == list.getSelectionModel().getSelectedIndex() && !cell.isEmpty())
                    .map(PersonListCell.class::cast).findFirst().orElseThrow();
        }

        @Override
        public void close() throws Exception {
            FxTestUtil.runOnFxThread(() -> {
                stage.close();
                return null;
            });
        }
    }
}
