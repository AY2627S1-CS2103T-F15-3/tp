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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
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
        Person person = new PersonBuilder().withTags(LONG_TAG, "Friend", "update", "incomplete")
                .withPhone("").withEmail("").withFavourite(true).withNotes("Keep note")
                .withLastUpdated(LocalDate.now().minusYears(2)).build();
        Model model = new ModelManager();
        model.addPerson(person);
        FutureTask<MainWindow> setup = new FutureTask<>(() -> {
            MainWindow window = createWindow(model);
            window.show();
            window.fillInnerParts();
            return window;
        });
        Platform.runLater(setup);
        MainWindow window = setup.get(20, TimeUnit.SECONDS);
        double[] narrowWidth = {0};
        try {
            for (double width : List.of(740.0, 560.0, 1100.0, 740.0)) {
                runOnFx(() -> window.getRoot().setWidth(width));
                awaitResize(window, width);
                runOnFx(() -> {
                    pulse(window);
                    Label longLabel = tag(window, LONG_TAG);
                    FlowPane pane = (FlowPane) longLabel.getParent();
                    assertEquals(width, window.getRoot().getWidth(), 1);
                    assertTrue(longLabel.getWidth() <= pane.getWidth() + 0.01);
                    assertTrue(longLabel.localToScene(longLabel.getBoundsInLocal()).getMaxX()
                            <= pane.localToScene(pane.getBoundsInLocal()).getMaxX() + 0.01);
                    assertTrue(pane.localToScene(pane.getBoundsInLocal()).getMaxX()
                            <= window.getRoot().getScene().getWidth() + 0.01);
                    assertTrue(renderedText(longLabel).endsWith("..."));
                    assertEquals(LONG_TAG, longLabel.getText());
                    assertEquals(LONG_TAG, longLabel.getTooltip().getText());
                    assertTrue(longLabel.getTooltip().isWrapText());
                    assertEquals(400, longLabel.getTooltip().getMaxWidth());
                    assertEquals("Friend", renderedText(tag(window, "Friend")));
                    assertEquals("Friend", tag(window, "Friend").getTooltip().getText());
                    assertEquals(Color.web("#3e7b91"), longLabel.getBackground().getFills().get(0).getFill());
                    if (width == 560) {
                        narrowWidth[0] = longLabel.getWidth();
                    } else if (width == 1100) {
                        assertTrue(longLabel.getWidth() > narrowWidth[0], "long=" + longLabel.getWidth()
                                + ", narrow=" + narrowWidth[0] + ", pane=" + pane.getWidth()
                                + ", scene=" + window.getRoot().getScene().getWidth());
                    }
                });
            }
            runOnFx(() -> {
                for (String reminder : List.of("update", "incomplete")) {
                    assertTrue(tag(window, reminder + " (custom)").getTooltip().getText().contains("t/" + reminder));
                    Label systemLabel = tag(window, reminder);
                    assertTrue(systemLabel.getStyleClass().contains("system-tag"));
                    assertEquals(Color.web("#F4C542"), systemLabel.getBackground().getFills().get(0).getFill());
                }
                Label heart = (Label) window.getRoot().getScene().lookup("#favouriteIcon");
                assertTrue(heart.isVisible());
                assertEquals(Color.web("#e63946"), heart.getTextFill());
                assertTrue(window.getRoot().getScene().lookup("#noteIcon").isVisible());
                assertEquals(person, model.getAddressBook().getPersonList().get(0));
            });
        } finally {
            runOnFx(() -> window.getRoot().close());
        }
    }
    @Test
    public void tagCommandAndRefresh_keepFullTagAndRecreateTooltip() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Model model = new ModelManager();
            Person original = new PersonBuilder().withTags("Friend").build();
            model.addPerson(original);
            MainWindow window = createWindow(model);
            try {
                window.show();
                window.fillInnerParts();
                window.getRoot().setWidth(740);
                pulse(window);
                TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                input.setText("tag 1 t/" + LONG_TAG);
                input.fireEvent(new ActionEvent());
                pulse(window);
                assertTrue(renderedText(tag(window, LONG_TAG)).endsWith("..."));
                window.getPersonListPanel().refresh();
                pulse(window);
                assertEquals(LONG_TAG, tag(window, LONG_TAG).getTooltip().getText());
                Person saved = new JsonAddressBookStorage(directory.resolve("contacts.json"))
                        .readAddressBook().orElseThrow().getPersonList().get(0);
                assertTrue(saved.getTags().stream().anyMatch(value -> value.tagName.equals(LONG_TAG)));
                assertEquals(original.getLastUpdated(), saved.getLastUpdated());
                assertFalse(saved.getTags().stream().anyMatch(value -> value.tagName.contains("...")));
                assertEquals("Friend", renderedText(tag(window, "Friend")));
            } finally {
                window.getRoot().close();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    private void awaitResize(MainWindow window, double width) throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            FutureTask<Boolean> resized = new FutureTask<>(() -> {
                double sceneWidth = window.getRoot().getScene().getWidth();
                // Allow for native window borders on the different CI platforms.
                return sceneWidth <= width && sceneWidth >= width - 40;
            });
            Platform.runLater(resized);
            if (resized.get(10, TimeUnit.SECONDS)) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Window did not finish resizing to " + width);
    }
    private void runOnFx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
    private MainWindow createWindow(Model model) {
        Path data = directory.resolve("contacts.json");
        return new MainWindow(new Stage(), new LogicManager(model,
                new StorageManager(new JsonAddressBookStorage(data),
                        new JsonUserPrefsStorage(directory.resolve("prefs.json")))), data);
    }

    private Label tag(MainWindow window, String text) {
        return window.getRoot().getScene().getRoot().lookupAll("#tags .label").stream()
                .map(node -> (Label) node).filter(label -> label.getText().equals(text)).findFirst().orElseThrow();
    }

    private String renderedText(Label label) {
        return ((Text) label.lookup(".text")).getText();
    }

    private void pulse(MainWindow window) {
        window.getRoot().getScene().getRoot().applyCss();
        window.getRoot().getScene().getRoot().layout();
    }
}
