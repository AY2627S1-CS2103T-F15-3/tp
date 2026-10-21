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
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class IncompleteTagDisplayTest {
    @TempDir
    public Path directory;

    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX rendering requires a display");
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
    public void commandBox_fillAndClearContactDetails_refreshesRemindersAndPreservesIcons() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Person original = new PersonBuilder().withPhone("").withEmail("").withFavourite(true)
                    .withLastUpdated(LocalDate.of(2024, 10, 1)).withTags("Friend", "update", "incomplete")
                    .withNotes("Existing note").build();
            Model model = new ModelManager();
            model.addPerson(original);
            Path data = directory.resolve("contacts.json");
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model,
                    new StorageManager(new JsonAddressBookStorage(data),
                            new JsonUserPrefsStorage(directory.resolve("prefs.json")))), data);
            try {
                window.show();
                window.fillInnerParts();
                pulse(window);
                assertEquals(List.of("incomplete", "update"), reminderTexts(window));
                for (Label label : tags(window)) {
                    label.applyCss();
                    if (label.getStyleClass().contains("system-tag")) {
                        assertEquals(Color.web("#F4C542"), label.getBackground().getFills().get(0).getFill());
                        assertEquals(Color.web("#222222"), label.getTextFill());
                    } else {
                        assertEquals(Color.web("#3e7b91"), label.getBackground().getFills().get(0).getFill());
                    }
                }
                assertTrue(tags(window).stream().anyMatch(label -> label.getText().equals("incomplete (custom)")));
                assertEquals("Not specified", ((Label) window.getRoot().getScene().lookup("#phone")).getText());
                window.getPersonListPanel().refresh();
                pulse(window);
                assertEquals(2, reminderTexts(window).size());
                enter(window, "edit 1 p/91234567");
                assertTrue(reminderTexts(window).isEmpty());
                enter(window, "edit 1 p/");
                assertEquals(List.of("incomplete"), reminderTexts(window));
                enter(window, "edit 1 e/alice@example.com");
                assertTrue(reminderTexts(window).isEmpty());
                enter(window, "edit 1 e/");
                assertEquals(List.of("incomplete"), reminderTexts(window));
                enter(window, "untag 1 t/incomplete");
                assertEquals(List.of("incomplete"), reminderTexts(window));
                Label heart = (Label) window.getRoot().getScene().lookup("#favouriteIcon");
                heart.applyCss();
                assertTrue(heart.isVisible());
                assertEquals(Color.web("#e63946"), heart.getTextFill());
                assertTrue(window.getRoot().getScene().lookup("#noteIcon").isVisible());
                Person finalPerson = model.getAddressBook().getPersonList().get(0);
                assertEquals(original.getNotes(), finalPerson.getNotes());
                assertEquals(LocalDate.now(), finalPerson.getLastUpdated());
                assertEquals(finalPerson, new JsonAddressBookStorage(data).readAddressBook().orElseThrow()
                        .getPersonList().get(0));
            } finally {
                window.getRoot().close();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    private List<Label> tags(MainWindow window) {
        return window.getRoot().getScene().getRoot().lookupAll("#tags .label").stream()
                .map(node -> (Label) node).toList();
    }

    private List<String> reminderTexts(MainWindow window) {
        return tags(window).stream().filter(label -> label.getStyleClass().contains("system-tag"))
                .map(Label::getText).sorted().toList();
    }

    private void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        pulse(window);
    }

    private void pulse(MainWindow window) {
        window.getRoot().getScene().getRoot().applyCss();
        window.getRoot().getScene().getRoot().layout();
    }
}
