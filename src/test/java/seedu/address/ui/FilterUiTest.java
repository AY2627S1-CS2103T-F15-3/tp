package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runActionOnFxThread;
import static seedu.address.ui.MainWindowTestUtil.assertCard;
import static seedu.address.ui.MainWindowTestUtil.enter;
import static seedu.address.ui.MainWindowTestUtil.feedback;
import static seedu.address.ui.MainWindowTestUtil.list;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.TextField;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises the actual command box, result display and rendered contact list on the JavaFX thread.
 */
public class FilterUiTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Mathematics")
            .withGraduationYear("2027").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").withMajor("Computer Science")
            .withGraduationYear("2028").withTags("Classmate").build();

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void commandBox_filter_updatesCardsFeedbackAndDisplayedIndices() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "filter m/computer science c/2027 t/classmate");
                assertEquals(List.of(bob), list(window).getItems());
                assertEquals("1 person(s) listed!", feedback(window));
                assertCard(window, bob, "1. ");
                enter(window, "filter c/2100");
                assertEquals(List.of(bob), list(window).getItems());
                assertEquals(GraduationYear.MESSAGE_CONSTRAINTS, feedback(window));
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertEquals("filter c/2100", input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                enter(window, "filter c/2099");
                assertTrue(list(window).getItems().isEmpty());
                assertEquals("0 person(s) listed!", feedback(window));
                enter(window, "list");
                assertEquals(List.of(alice, bob, charlie), list(window).getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_mutations_refreshResultsAndRenumberCards() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "filter t/Classmate");
                enter(window, "untag 1 t/Classmate");
                assertEquals(List.of(charlie), list(window).getItems());
                assertCard(window, charlie, "1. ");
                enter(window, "delete 1");
                assertTrue(list(window).getItems().isEmpty());
                assertTrue(feedback(window).startsWith("Deleted person: Charlie"));
                enter(window, "list");
                assertEquals(2, list(window).getItems().size());
                assertEquals(alice, list(window).getItems().get(0));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_edit_resetsViewAndReapplyingFilterReflectsChangedMajor() throws Exception {
        runActionOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "filter m/Computer Science");
                enter(window, "edit 1 m/Mathematics");
                assertEquals(3, list(window).getItems().size());
                enter(window, "filter m/Computer Science");
                assertEquals(List.of(charlie), list(window).getItems());
                assertCard(window, charlie, "1. ");
            } finally {
                window.getRoot().close();
            }
        });
    }

    private MainWindow createWindow() {
        return MainWindowTestUtil.createWindow(directory, alice, bob, charlie);
    }
}
