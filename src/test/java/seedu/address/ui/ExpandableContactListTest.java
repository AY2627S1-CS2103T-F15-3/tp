package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.MainWindowTestUtil.enter;
import static seedu.address.ui.MainWindowTestUtil.list;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Regression checks against the real scrolling list, including its recycled, variable-height cells. */
public class ExpandableContactListTest {
    @TempDir
    public Path directory;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void longFields_clickAndKeyboardExpandAndCollapse_resizeTheirListCell() throws Exception {
        MainWindow window = FxTestUtil.runOnFxThread(this::createWindow);
        try {
            for (String field : List.of("phone", "address", "email", "major", "name")) {
                for (double width : List.of(1000.0, 450.0, 820.0)) {
                    FxTestUtil.runActionOnFxThread(() -> assertExpansionCycle(window, field, width));
                }
            }
        } finally {
            FxTestUtil.runOnFxThread(() -> {
                window.getRoot().close();
                return null;
            });
        }
    }

    @Test
    public void manyTags_scrollingResizingAndRefresh_keepsRowsInsideCompactCards() throws Exception {
        MainWindow window = FxTestUtil.runOnFxThread(this::createWindow);
        try {
            FxTestUtil.runOnFxThread(() -> {
                for (double width : List.of(1000.0, 450.0, 1200.0, 740.0)) {
                    window.getRoot().setWidth(width);
                    enter(window, "list");
                    for (int index : List.of(0, 12, 30, 1, 0)) {
                        list(window).scrollTo(index);
                        settle(window);
                        assertVisibleCardsFit(window);
                    }
                    window.getPersonListPanel().refresh();
                    settle(window);
                    assertVisibleCardsFit(window);
                }
                ScrollBar scrollbar = (ScrollBar) list(window).lookupAll(".scroll-bar").stream()
                        .filter(node -> node instanceof ScrollBar bar
                                && bar.getOrientation() == javafx.geometry.Orientation.VERTICAL)
                        .findFirst().orElseThrow();
                assertTrue(scrollbar.isVisible());
                assertTrue(scrollbar.getWidth() >= 14);
                scrollbar.increment();
                settle(window);
                assertVisibleCardsFit(window);
                return null;
            });
        } finally {
            FxTestUtil.runOnFxThread(() -> {
                window.getRoot().close();
                return null;
            });
        }
    }

    @Test
    public void windowResize_existingCardsFitViewportAndStackDetails() throws Exception {
        MainWindow window = FxTestUtil.runOnFxThread(this::createWindow);
        try {
            FxTestUtil.runOnFxThread(() -> {
                enter(window, "find ManyTags");
                for (double width : List.of(1000.0, 450.0, 740.0, 450.0)) {
                    window.getRoot().setWidth(width);
                    Parent root = window.getRoot().getScene().getRoot();
                    root.resize(width, root.getBoundsInLocal().getHeight());
                    settle(window);
                    assertVisibleCardsFit(window);
                    Node card = firstCard(window);
                    if (width == 450) {
                        assertTrue(bounds(card.lookup("#major")).getMinY()
                                > bounds(card.lookup("#email")).getMaxY(),
                                "Academic details should stack below contact details in a narrow window");
                    } else {
                        assertEquals(bounds(card.lookup("#phone")).getMinY(),
                                bounds(card.lookup("#major")).getMinY(), 0.5);
                    }
                }
                return null;
            });
        } finally {
            FxTestUtil.runOnFxThread(() -> {
                window.getRoot().close();
                return null;
            });
        }
    }

    @Test
    public void allValuesAndTagExpanded_canScrollToBottomAndCollapseWithoutChangingData() throws Exception {
        MainWindow window = FxTestUtil.runOnFxThread(this::createWindow);
        try {
            FxTestUtil.runOnFxThread(() -> {
                window.getRoot().setWidth(450);
                window.getRoot().getScene().getRoot().resize(450, 760);
                enter(window, "find LongAll");
                for (String field : List.of("name", "phone", "address", "email", "major")) {
                    pressEnter((Label) firstCard(window).lookup("#" + field));
                    settle(window);
                }
                Label tag = (Label) firstCard(window).lookup("#tags").lookupAll(".label").stream()
                        .filter(node -> node instanceof Label label && label.getTooltip() != null
                                && label.getTooltip().getText().equals("T".repeat(500)))
                        .findFirst().orElseThrow();
                pressEnter(tag);
                settle(window);
                assertTrue(tag.isWrapText());
                assertEquals("T".repeat(500), ((Text) tag.lookup(".text")).getText());
                assertVisibleCardsFit(window);
                ScrollBar bar = (ScrollBar) list(window).lookupAll(".scroll-bar").stream()
                        .filter(node -> node instanceof ScrollBar scroll
                                && scroll.getOrientation() == javafx.geometry.Orientation.VERTICAL)
                        .findFirst().orElseThrow();
                assertTrue(bar.isVisible());
                bar.setValue(bar.getMax());
                settle(window);
                Node card = firstCard(window);
                double lastContent = List.of("phone", "address", "email", "major", "graduationYear", "lastUpdated")
                        .stream().mapToDouble(id -> bounds(card.lookup("#" + id)).getMaxY()).max().orElseThrow();
                assertTrue(lastContent <= bounds(list(window)).getMaxY() + 1,
                        "The end of an expanded card must be reachable by scrolling");
                window.getPersonListPanel().refresh();
                settle(window);
                assertTrue(((Label) firstCard(window).lookup("#phone")).isWrapText());
                for (String field : List.of("name", "phone", "address", "email", "major")) {
                    pressEnter((Label) firstCard(window).lookup("#" + field));
                    settle(window);
                }
                tag = (Label) firstCard(window).lookup("#tags").lookupAll(".label").stream()
                        .filter(node -> node instanceof Label label && label.getText().equals("T".repeat(500)))
                        .findFirst().orElseThrow();
                pressEnter(tag);
                settle(window);
                assertTrue(firstCard(window).getBoundsInLocal().getHeight() < 350,
                        "Collapsed cards should return to a compact height");
                assertVisibleCardsFit(window);
                try {
                    Person stored = new JsonAddressBookStorage(directory.resolve("contacts.json"))
                            .readAddressBook().orElseThrow().getPersonList().get(1);
                    assertEquals("9".repeat(300), stored.getPhone().value);
                    assertTrue(stored.getTags().stream().anyMatch(value -> value.tagName.equals("T".repeat(500))));
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
                return null;
            });
        } finally {
            FxTestUtil.runOnFxThread(() -> {
                window.getRoot().close();
                return null;
            });
        }
    }

    private void assertExpansionCycle(MainWindow window, String field, double width) {
        window.getRoot().setWidth(width);
        enter(window, "find LongAll");
        Node card = firstCard(window);
        Label label = (Label) card.lookup("#" + field);
        if (label.isWrapText()) {
            pressEnter(label);
            settle(window);
        }
        assertCollapsed(label, field);
        double collapsedHeight = card.getBoundsInLocal().getHeight();
        label.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                MouseButton.PRIMARY, 1, false, false, false, false,
                false, false, false, false, false, true, null));
        settle(window);
        Label expanded = (Label) firstCard(window).lookup("#" + field);
        assertTrue(expanded.isWrapText(), field);
        assertEquals(expanded.getText(), ((Text) expanded.lookup(".text")).getText(), field);
        assertTrue(firstCard(window).getBoundsInLocal().getHeight() > collapsedHeight, field);
        assertVisibleCardsFit(window);
        pressEnter(expanded);
        settle(window);
        assertCollapsed((Label) firstCard(window).lookup("#" + field), field);
        assertEquals(collapsedHeight, firstCard(window).getBoundsInLocal().getHeight(), 2.0, field);
        assertVisibleCardsFit(window);
    }

    private void assertCollapsed(Label label, String field) {
        assertFalse(label.isWrapText(), field);
        String rendered = ((Text) label.lookup(".text")).getText();
        assertTrue(rendered.endsWith("..."), field + " width=" + label.getWidth() + " rendered=" + rendered);
    }

    private MainWindow createWindow() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("ManyTags").withTags(IntStream.range(0, 25)
                .mapToObj(index -> "Category" + index).toArray(String[]::new)).build());
        model.addPerson(new PersonBuilder().withName("LongAll " + "Long Contact ".repeat(20).strip())
                .withPhone("9".repeat(300)).withEmail("a".repeat(300) + "@example.com")
                .withAddress("Lower Kent Ridge Road ".repeat(30)).withMajor("Computer Science ".repeat(30))
                .withGraduationYear("2027").withTags("T".repeat(500), "Friend")
                .withNotes("Keep this note").withFavourite(true).build());
        for (int index = 0; index < 35; index++) {
            model.addPerson(new PersonBuilder().withName("Regular " + index).build());
        }
        Path data = directory.resolve("contacts.json");
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model,
                new StorageManager(new JsonAddressBookStorage(data),
                        new JsonUserPrefsStorage(directory.resolve("prefs.json")))), data);
        window.getRoot().setWidth(1000);
        window.getRoot().setHeight(800);
        window.show();
        window.fillInnerParts();
        settle(window);
        return window;
    }

    private void assertVisibleCardsFit(MainWindow window) {
        for (Node node : list(window).lookupAll(".list-cell")) {
            if (!(node instanceof ListCell<?> cell) || cell.isEmpty() || cell.getGraphic() == null) {
                continue;
            }
            if (!cell.isVisible() || !bounds(cell).intersects(bounds(list(window)))) {
                continue;
            }
            Parent card = (Parent) cell.getGraphic();
            Bounds container = bounds(card);
            assertTrue(container.getMaxX() <= bounds(list(window)).getMaxX() + 0.5,
                    "Card exceeds the list viewport width");
            assertTrue(container.getMaxY() <= bounds(cell).getMaxY() + 1, "Card exceeds its list cell");
            double bottom = container.getMinY();
            for (String id : List.of("name", "phone", "address", "email", "major", "graduationYear", "lastUpdated")) {
                Node value = card.lookup("#" + id);
                Bounds actual = bounds(value);
                assertTrue(actual.getMaxY() <= container.getMaxY() + 0.5, id + " exceeds card bottom");
                assertTrue(actual.getMaxX() <= container.getMaxX() + 0.5, id + " exceeds card width");
                bottom = Math.max(bottom, actual.getMaxY());
            }
            Node details = card.lookup("#details");
            for (Node tag : card.lookup("#tags").lookupAll(".label")) {
                assertTrue(bounds(tag).getMaxY() <= bounds(details).getMinY() + 0.5,
                        "Tag overlaps details: " + bounds(tag) + " / " + bounds(details));
            }
            assertTrue(container.getMaxY() - bottom <= 30, "Excess blank space below card details");
        }
    }

    private Bounds bounds(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    private Node firstCard(MainWindow window) {
        return list(window).lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> cell && cell.getIndex() == 0 && !cell.isEmpty())
                .map(node -> ((ListCell<?>) node).getGraphic()).findFirst().orElseThrow();
    }

    private void pressEnter(Label label) {
        label.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
    }

    private void settle(MainWindow window) {
        FxTestUtil.layout(window.getRoot().getScene().getRoot());
    }
}
