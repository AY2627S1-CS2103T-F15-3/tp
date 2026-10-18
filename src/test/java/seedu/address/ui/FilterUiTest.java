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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
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
    public void commandBox_filter_updatesCardsFeedbackAndDisplayedIndices() throws Exception {
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        Model model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(charlie);
        Path data = directory.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, data);
        window.show();
        window.fillInnerParts();
        return window;
    }

    private void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        window.getRoot().getScene().getRoot().applyCss();
        window.getRoot().getScene().getRoot().layout();
    }

    private ListView<?> list(MainWindow window) {
        return (ListView<?>) window.getRoot().getScene().lookup("#personListView");
    }

    private String feedback(MainWindow window) {
        return ((TextArea) window.getRoot().getScene().lookup("#resultDisplay")).getText();
    }

    private void assertCard(MainWindow window, Person person, String index) {
        ListCell<?> cell = list(window).lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && person.equals(candidate.getItem()))
                .map(node -> (ListCell<?>) node).findFirst().orElseThrow();
        assertEquals(index, ((Label) cell.getGraphic().lookup("#id")).getText());
        assertEquals(person.getName().fullName, ((Label) cell.getGraphic().lookup("#name")).getText());
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
