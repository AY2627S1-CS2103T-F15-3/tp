package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class TagOverflowTest {
    private static final String LONG_TAG = "A".repeat(500);

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
    public void longTag_resizesWithinCardWithEllipsisAndFullTooltip() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Person person = new PersonBuilder().withTags(LONG_TAG, "Friend", "update", "incomplete")
                    .withPhone("").withEmail("").withFavourite(true).withNotes("Keep note")
                    .withLastUpdated(LocalDate.now().minusYears(2)).build();
            PersonCard card = new PersonCard(person, 1);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 740, 500);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            for (double width : List.of(740.0, 560.0, 1100.0, 740.0)) {
                // Test the layout constraint, independently of native window borders or screen size limits.
                root.resize(width, 500);
                root.applyCss();
                root.layout();
                Label longLabel = tag(root, LONG_TAG);
                FlowPane pane = (FlowPane) longLabel.getParent();
                Text text = (Text) longLabel.lookup(".text");
                assertEquals(width, root.getWidth(), 0.01);
                assertEquals("A".repeat(20) + "...", text.getText());
                assertEquals("A".repeat(20) + "...", longLabel.getText());
                assertTrue(longLabel.localToScene(longLabel.getBoundsInLocal()).getMaxX()
                        <= pane.localToScene(pane.getBoundsInLocal()).getMaxX() + 0.5);
                assertTrue(text.localToScene(text.getBoundsInLocal()).getMaxX()
                        <= card.getRoot().localToScene(card.getRoot().getBoundsInLocal()).getMaxX() + 0.5);
                assertTrue(pane.localToScene(pane.getBoundsInLocal()).getMaxX() <= root.getWidth() + 0.5);
                assertEquals(LONG_TAG, longLabel.getTooltip().getText());
                assertTrue(longLabel.getTooltip().isWrapText());
                assertEquals(400, longLabel.getTooltip().getMaxWidth());
                assertEquals("Friend", renderedText(tag(root, "Friend")));
                assertEquals(Color.web("#30363c"), longLabel.getBackground().getFills().get(0).getFill());
            }
            for (String reminder : List.of("update", "incomplete")) {
                assertTrue(tag(root, reminder + " (custom)").getTooltip().getText().contains("t/" + reminder));
                Label systemLabel = tag(root, reminder);
                assertTrue(systemLabel.getStyleClass().contains("system-tag"));
                assertEquals(Color.web("#433425"), systemLabel.getBackground().getFills().get(0).getFill());
            }
            Label heart = (Label) root.lookup("#favouriteIcon");
            assertTrue(heart.isVisible());
            assertEquals(Color.web("#e63946"), heart.getTextFill());
            assertTrue(root.lookup("#noteIcon").isVisible());
            assertTrue(person.getTags().stream().anyMatch(value -> value.tagName.equals(LONG_TAG)));
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    @Test
    public void tagCommandAndRefresh_keepFullTagAndRecreateTooltip() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Model model = new ModelManager();
            Person original = new PersonBuilder().withTags("Friend").build();
            model.addPerson(original);
            Path data = directory.resolve("contacts.json");
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model,
                    new StorageManager(new JsonAddressBookStorage(data),
                            new JsonUserPrefsStorage(directory.resolve("prefs.json")))), data);
            try {
                window.show();
                window.fillInnerParts();
                Parent root = window.getRoot().getScene().getRoot();
                root.applyCss();
                root.layout();
                TextField input = (TextField) root.lookup("#commandTextField");
                input.setText("tag 1 t/" + LONG_TAG);
                input.fireEvent(new ActionEvent());
                root.applyCss();
                root.layout();
                assertEquals("A".repeat(20) + "...", renderedText(tag(root, LONG_TAG)));
                window.getPersonListPanel().refresh();
                root.applyCss();
                root.layout();
                assertEquals(LONG_TAG, tag(root, LONG_TAG).getTooltip().getText());
                Person saved = new JsonAddressBookStorage(data).readAddressBook().orElseThrow()
                        .getPersonList().get(0);
                assertTrue(saved.getTags().stream().anyMatch(value -> value.tagName.equals(LONG_TAG)));
                assertEquals(original.getLastUpdated(), saved.getLastUpdated());
                assertFalse(saved.getTags().stream().anyMatch(value -> value.tagName.contains("...")));
                assertEquals("Friend", renderedText(tag(root, "Friend")));
            } finally {
                window.getRoot().close();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    private Label tag(Parent root, String text) {
        return root.lookupAll("#tags .label").stream().map(node -> (Label) node)
                .filter(label -> label.getText().equals(text)
                        || label.getTooltip() != null && label.getTooltip().getText().equals(text))
                .findFirst().orElseThrow();
    }

    private String renderedText(Label label) {
        return ((Text) label.lookup(".text")).getText();
    }
}
