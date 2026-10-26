package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
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
public class EditContactUiTest {
    @TempDir
    public Path directory;

    private final Person alice = new PersonBuilder().withName("Alice").withMajor("Mathematics")
            .withGraduationYear("2027").withTags("Friend").build();
    private final Person bob = new PersonBuilder().withName("Bob").withMajor("Computer Science")
            .withGraduationYear("2027").withTags("Classmate").withNotes("Study together")
            .withFavourite(true).build();
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
    public void edit_filteredContact_updatesRenderedFieldsAndStorage() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "find Bob");
                enter(window, "edit 1 m/Mathematics c/2028 t/StudyGroup");
                assertEquals(3, list(window).getItems().size());
                Person edited = (Person) list(window).getItems().get(1);
                assertEquals("Bob", edited.getName().fullName);
                assertEquals(bob.getNotes(), edited.getNotes());
                assertTrue(edited.isFavourite());
                assertEquals("Edited person: " + Messages.format(edited), feedback(window));
                assertEquals("", input(window).getText());
                assertAcademicCard(window, edited, "Major: Mathematics", "Class year: 2028");
                assertTrue(card(window, edited).lookup("#favouriteIcon").isVisible());
                assertTrue(card(window, edited).lookup("#noteIcon").isVisible());
                assertTrue(card(window, edited).lookupAll(".label").stream()
                        .anyMatch(node -> node instanceof Label label && label.getText().equals("StudyGroup")));

                enter(window, "find Bob");
                enter(window, "edit 1 m/ c/");
                Person cleared = (Person) list(window).getItems().get(1);
                assertAcademicCard(window, cleared, "Major: Not specified", "Class year: Not specified");
                try {
                    Person saved = new JsonAddressBookStorage(directory.resolve("contacts.json"))
                            .readAddressBook().orElseThrow().getPersonList().get(1);
                    assertEquals(cleared, saved);
                    assertEquals(bob.getNotes(), saved.getNotes());
                    assertTrue(saved.isFavourite());
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void invalidEdit_preservesFilteredCardAndInputAndShowsError() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enter(window, "find Bob");
                String[] commands = {"edit 1 c/2100", "edit 2 m/Mathematics", "edit 1 n/Alice"};
                String[] errors = {GraduationYear.MESSAGE_CONSTRAINTS,
                    Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, EditCommand.MESSAGE_DUPLICATE_PERSON};
                for (int i = 0; i < commands.length; i++) {
                    enter(window, commands[i]);
                    assertEquals(List.of(bob), list(window).getItems());
                    assertEquals(commands[i], input(window).getText());
                    assertEquals(errors[i], feedback(window));
                    assertTrue(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                    assertAcademicCard(window, bob, "Major: Computer Science", "Class year: 2027");
                }
                enter(window, "edit 1 m/Mathematics");
                assertFalse(input(window).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals("", input(window).getText());
                assertEquals(3, list(window).getItems().size());
            } finally {
                window.getRoot().close();
            }
        });
    }

    private TextField input(MainWindow window) {
        return (TextField) window.getRoot().getScene().lookup("#commandTextField");
    }

    private javafx.scene.Node card(MainWindow window, Person person) {
        return list(window).lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && person.equals(candidate.getItem()))
                .map(node -> ((ListCell<?>) node).getGraphic()).findFirst().orElseThrow();
    }

    private void assertAcademicCard(MainWindow window, Person person, String major, String year) {
        javafx.scene.Node graphic = card(window, person);
        assertEquals(major, ((Label) graphic.lookup("#major")).getText());
        assertEquals(year, ((Label) graphic.lookup("#graduationYear")).getText());
        assertEquals(person.getName().fullName, ((Label) graphic.lookup("#name")).getText());
        String displayedIndex = (list(window).getItems().indexOf(person) + 1) + ". ";
        assertEquals(displayedIndex, ((Label) graphic.lookup("#id")).getText());
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

    private void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
