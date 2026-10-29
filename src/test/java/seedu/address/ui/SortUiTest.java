package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runActionOnFxThread;
import static seedu.address.ui.MainWindowTestUtil.assertCard;
import static seedu.address.ui.MainWindowTestUtil.enter;
import static seedu.address.ui.MainWindowTestUtil.feedback;
import static seedu.address.ui.MainWindowTestUtil.list;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises the actual command box, result display and rendered contact list on the JavaFX thread.
 */
public class SortUiTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Zoology")
            .withGraduationYear("2028").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withTags("Classmate").build();

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void commandBox_sort_updatesCardsFeedbackAndAcademicOrder() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "sort name asc");
                assertEquals("Sorted contacts by name (asc).", feedback(window));
                assertCards(window, List.of(alice, bob, charlie));
                enter(window, "sort name desc");
                assertCards(window, List.of(charlie, bob, alice));
                enter(window, "sort major asc");
                assertCards(window, List.of(bob, alice, charlie));
                enter(window, "sort year desc");
                assertCards(window, List.of(alice, bob, charlie));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_updateDateSort_updatesCardsAndStatus() throws Exception {
        Person alice = new PersonBuilder(this.alice).withLastUpdated(LocalDate.of(2024, 12, 31)).build();
        Person bob = new PersonBuilder(this.bob).withLastUpdated(LocalDate.of(2024, 12, 31)).build();
        Person charlie = new PersonBuilder(this.charlie).withLastUpdated(LocalDate.of(2025, 1, 1)).build();
        runActionOnFxThread(() -> {
            MainWindow window = createWindow(alice, bob, charlie);
            try {
                enter(window, "sort updated asc");
                assertCards(window, List.of(alice, bob, charlie));
                assertTrue(((Label) window.getRoot().getScene().lookup("#viewStatus"))
                        .getText().endsWith("Sort: Last updated ascending"));
                enter(window, "sort updated desc");
                assertCards(window, List.of(charlie, alice, bob));
                assertEquals("Sorted contacts by updated (desc).", feedback(window));
                enter(window, "filter t/Classmate");
                assertCards(window, List.of(charlie, bob));
                enter(window, "sort reset");
                assertCards(window, List.of(charlie, bob));
                assertTrue(((Label) window.getRoot().getScene().lookup("#viewStatus"))
                        .getText().endsWith("Sort: Saved order"));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_sortedFilteredMutations_targetDisplayedCardsAndRenumber() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "filter t/Classmate");
                enter(window, "sort name desc");
                assertCards(window, List.of(charlie, bob));
                enter(window, "tag 1 t/Mentor");
                Person taggedCharlie = new PersonBuilder(charlie).withTags("Classmate", "Mentor").build();
                assertCards(window, List.of(taggedCharlie, bob));
                enter(window, "untag 1 t/Classmate");
                assertCards(window, List.of(bob));
                enter(window, "delete 1");
                assertTrue(list(window).getItems().isEmpty());
                assertTrue(feedback(window).startsWith("Deleted person: Bob"));
                enter(window, "list");
                assertCards(window, List.of(new PersonBuilder(charlie).withTags("Mentor").build(), alice));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_batchDelete_usesOriginalIndicesAndRenumbersCards() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "sort name asc");
                assertCards(window, List.of(alice, bob, charlie));
                enter(window, "delete 1 3");
                assertCards(window, List.of(bob));
                assertEquals("Deleted 2 contacts:\n1. " + Messages.format(alice)
                        + "\n3. " + Messages.format(charlie), feedback(window));
                enter(window, "undo");
                assertCards(window, List.of(alice, bob, charlie));
                enter(window, "redo");
                assertCards(window, List.of(bob));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_duplicateAdd_identifiesDisplayedAndHiddenContacts() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "sort name asc");
                enter(window, "add n/charlie p/88888888 a/Different address");
                assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON + "\nExisting contact at displayed index 3:\n"
                        + Messages.format(charlie), feedback(window));
                assertCards(window, List.of(alice, bob, charlie));

                enter(window, "filter t/Classmate");
                String command = "add n/ALICE p/77777777 a/Different address";
                enter(window, command);
                assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON
                        + "\nExisting contact is hidden by the current filter (saved index 3):\n"
                        + Messages.format(alice) + "\nRun list, then sort reset to view this contact at index 3.",
                        feedback(window));
                assertCards(window, List.of(bob, charlie));
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertEquals(command, input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                enter(window, "list");
                enter(window, "sort reset");
                assertCards(window, List.of(charlie, bob, alice));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_sharedDetailsWarning_addsContactAndShowsCurrentIndices() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "sort name asc");
                enter(window, "filter t/Classmate");
                Person aaron = new PersonBuilder().withName("Aaron").withPhone(PersonBuilder.DEFAULT_PHONE)
                        .withEmail("aaron@example.com").withAddress("NUS").build();

                enter(window, "add n/Aaron p/" + PersonBuilder.DEFAULT_PHONE + " e/aaron@example.com a/NUS");

                String successMessage = String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(aaron));
                String advisory = "Warning - duplicate details detected: "
                        + "#2 Alice (phone); #3 Bob (phone); #4 Charlie (phone)";
                assertEquals(successMessage + "\n" + advisory, feedback(window));
                TextArea result = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                assertTrue(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                assertEquals(Color.web("#ffcf66"), ((Text) result.lookup(".text")).getFill());
                assertTrue(result.getBorder() == null || result.getBorder().getStrokes().isEmpty());
                assertNull(window.getRoot().getScene().lookup("#warningDisplay"));
                assertCards(window, List.of(aaron, alice, bob, charlie));
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertEquals("", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                enter(window, "undo");
                assertCards(window, List.of(alice, bob, charlie));
                assertEquals("Undid: " + successMessage, feedback(window));
                assertFalse(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                assertEquals(Color.web("#d5d8db"), ((Text) result.lookup(".text")).getFill());
                enter(window, "redo");
                assertCards(window, List.of(aaron, alice, bob, charlie));
                assertEquals("Redid: " + successMessage, feedback(window));
                assertEquals(Color.web("#d5d8db"), ((Text) result.lookup(".text")).getFill());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_successAndWarning_shareOriginalScrollableTextArea() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                window.getRoot().setWidth(640);
                window.getRoot().getScene().getRoot().resize(620, 700);
                String longAddress = "Lower Kent Ridge Road ".repeat(30).strip();
                enter(window, "add n/Aaron p/" + PersonBuilder.DEFAULT_PHONE
                        + " e/aaron@example.com a/" + longAddress);

                TextArea result = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                assertTrue(result.isWrapText());
                assertFalse(result.isEditable());
                assertTrue(result.getHeight() > 0);
                assertEquals(1, result.getParent().lookupAll(".text-area").size());
                assertNull(window.getRoot().getScene().lookup("#warningDisplay"));
                assertEquals(100, window.getRoot().getScene().lookup("#resultDisplayPlaceholder")
                        .getLayoutBounds().getHeight());
                ScrollPane scrollPane = (ScrollPane) result.lookup(".scroll-pane");
                assertTrue(result.lookup(".content").getLayoutBounds().getHeight()
                        > scrollPane.getViewportBounds().getHeight(), "Long feedback remains scrollable.");
                assertEquals(0, result.getScrollTop());
                assertEquals(Color.web("#ffcf66"), ((Text) result.lookup(".text")).getFill());
                result.selectAll();
                assertEquals(result.getText(), result.getSelectedText());
                result.positionCaret(0);
                result.setScrollTop(0);

                enter(window, "add n/Zara p/96667777 e/zara@example.com a/NUS");
                assertFalse(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                assertEquals(Color.web("#d5d8db"), ((Text) result.lookup(".text")).getFill());
                assertTrue(feedback(window).startsWith("New person added: Zara"));

                enter(window, "add n/AnotherAaron p/" + PersonBuilder.DEFAULT_PHONE + " a/NUS");
                assertTrue(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                assertEquals(Color.web("#ffcf66"), ((Text) result.lookup(".text")).getFill());

                enter(window, "sort name sideways");
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE),
                        feedback(window));
                assertFalse(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                assertEquals(Color.web("#d5d8db"), ((Text) result.lookup(".text")).getFill());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_invalidSortAndEditUndoRedo_preserveExpectedView() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "sort name asc");
                enter(window, "filter t/Classmate");
                enter(window, "sort name sideways");
                assertCards(window, List.of(bob, charlie));
                assertEquals(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE),
                        feedback(window));
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertEquals("sort name sideways", input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                enter(window, "edit 1 n/Zara");
                Person zara = new PersonBuilder(bob).withName("Zara").build();
                assertCards(window, List.of(alice, charlie, zara));
                enter(window, "undo");
                assertCards(window, List.of(alice, bob, charlie));
                enter(window, "redo");
                assertCards(window, List.of(alice, charlie, zara));
            } finally {
                window.getRoot().close();
            }
        });
    }

    private MainWindow createWindow() {
        return createWindow(alice, bob, charlie);
    }

    private MainWindow createWindow(Person alice, Person bob, Person charlie) {
        return MainWindowTestUtil.createWindow(directory, charlie, bob, alice);
    }

    private void assertCards(MainWindow window, List<Person> expected) {
        assertEquals(expected, list(window).getItems());
        for (int index = 0; index < expected.size(); index++) {
            list(window).scrollTo(index);
            FxTestUtil.layout(window.getRoot().getScene().getRoot());
            VirtualFlow<?> flow = (VirtualFlow<?>) list(window).lookup(".virtual-flow");
            flow.scrollTo(index);
            flow.layout();
            assertCard(window, expected.get(index), (index + 1) + ". ");
        }
    }

}
