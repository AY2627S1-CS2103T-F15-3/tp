package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.time.LocalDate;
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
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
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
    public void commandBox_sort_updatesCardsFeedbackAndAcademicOrder() throws Exception {
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
        runOnFxThread(() -> {
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
    public void commandBox_invalidSortAndEditUndoRedo_preserveExpectedView() throws Exception {
        runOnFxThread(() -> {
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
        Model model = new ModelManager();
        model.addPerson(charlie);
        model.addPerson(bob);
        model.addPerson(alice);
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

    private void assertCards(MainWindow window, List<Person> expected) {
        assertEquals(expected, list(window).getItems());
        for (int index = 0; index < expected.size(); index++) {
            list(window).scrollTo(index);
            list(window).layout();
            assertCard(window, expected.get(index), (index + 1) + ". ");
        }
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
