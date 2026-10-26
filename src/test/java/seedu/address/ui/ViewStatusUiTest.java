package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks persistent view status through actual command submissions and JavaFX layout.
 */
public class ViewStatusUiTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withGraduationYear("2028").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withFavourite(true).withNotes("Study group").build();
    private final Person charlie = new PersonBuilder().withName("Charlie").build();

    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX UI tests require a display");
        FutureTask<Void> started = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        try {
            Platform.startup(started);
        } catch (IllegalStateException e) {
            Platform.runLater(started);
        }
        started.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void commandBox_statusSurvivesFeedbackAndResetKeepsFilter() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                assertStatus(window, "Showing 3 of 3 contacts | Filter: All contacts | Sort: Saved order");
                enter(window, "filter c/2027");
                enter(window, "sort name asc");
                String filtered = "Showing 1 of 3 contacts | Filter: Class year: 2027 | Sort: Name ascending";
                assertStatus(window, filtered);
                enter(window, "shownote 1");
                assertTrue(((TextArea) window.getRoot().getScene().lookup("#resultDisplay"))
                        .getText().contains("Study group"));
                assertStatus(window, filtered);
                enter(window, "sort reset desc");
                assertStatus(window, filtered);
                enter(window, "sort reset");
                assertStatus(window, "Showing 1 of 3 contacts | Filter: Class year: 2027 | Sort: Saved order");
                assertEquals(List.of(bob), list(window).getItems());
                enter(window, "list");
                assertStatus(window, "Showing 3 of 3 contacts | Filter: All contacts | Sort: Saved order");
                assertEquals(List.of(charlie, bob, alice), list(window).getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_statusTracksFavouritesCountsAndUndo() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "listfav");
                assertStatus(window, "Showing 1 of 3 contacts | Filter: Favourites | Sort: Saved order");
                enter(window, "unfav 1");
                assertStatus(window, "Showing 0 of 3 contacts | Filter: Favourites | Sort: Saved order");
                enter(window, "find Nobody");
                assertStatus(window, "Showing 0 of 3 contacts | Filter: Search: Nobody | Sort: Saved order");
                enter(window, "list");
                enter(window, "sort name desc");
                enter(window, "edit 1 n/Zara");
                enter(window, "find Alice");
                enter(window, "undo");
                assertStatus(window, "Showing 3 of 3 contacts | Filter: All contacts | Sort: Name descending");
                enter(window, "delete 1");
                assertStatus(window, "Showing 2 of 2 contacts | Filter: All contacts | Sort: Name descending");
                enter(window, "clear");
                assertStatus(window, "Showing 0 of 0 contacts | Filter: All contacts | Sort: Name descending");
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void commandBox_longCriteriaWrapWithinWindow() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                window.getRoot().setWidth(650);
                enter(window, "list");
                double initialHeight = status(window).getHeight();
                String major = "Computer Science and ".repeat(12) + "Mathematics";
                enter(window, "filter m/" + major + " c/2027 t/Classmate");
                assertTrue(status(window).getText().contains(major));
                assertTrue(status(window).isWrapText());
                assertTrue(status(window).getHeight() > initialHeight,
                        "initial=" + initialHeight + ", height=" + status(window).getHeight()
                                + ", width=" + status(window).getWidth() + ", preferred="
                                + status(window).prefHeight(status(window).getWidth()));
                assertTrue(status(window).getWidth() <= window.getRoot().getScene().getWidth());
            } finally {
                window.getRoot().close();
            }
        });
    }

    private MainWindow createWindow() {
        ModelManager model = new ModelManager();
        model.addPerson(charlie);
        model.addPerson(bob);
        model.addPerson(alice);
        Path data = directory.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, data);
        window.show();
        window.fillInnerParts();
        layout(window);
        return window;
    }

    private void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        layout(window);
    }

    private void layout(MainWindow window) {
        window.getRoot().getScene().getRoot().applyCss();
        window.getRoot().getScene().getRoot().layout();
    }

    private Label status(MainWindow window) {
        return (Label) window.getRoot().getScene().lookup("#viewStatus");
    }

    private ListView<?> list(MainWindow window) {
        return (ListView<?>) window.getRoot().getScene().lookup("#personListView");
    }

    private void assertStatus(MainWindow window, String expected) {
        assertEquals(expected, status(window).getText());
    }

    private void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
