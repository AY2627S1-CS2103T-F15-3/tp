package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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

public class UpdateTagDisplayTest {
    @TempDir
    public Path directory;

    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux")
                || System.getenv("DISPLAY") != null, "JavaFX rendering requires a display");
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
    public void refresh_reminderAppearsAfterAnniversaryAndDoesNotDuplicateCustomTag() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            TestClock clock = new TestClock();
            Person person = new PersonBuilder().withLastUpdated(LocalDate.of(2025, 10, 18))
                    .withTags("Friend", "update").build();
            PersonCard card = new PersonCard(person, 1, clock);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 500, 250);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/view/Extensions.css").toExternalForm());
            root.applyCss();
            root.layout();
            assertTrue(root.lookupAll(".system-tag").isEmpty());
            clock.date = clock.date.plusDays(1);
            card.refreshTags();
            card.refreshTags();
            root.applyCss();
            root.layout();
            assertEquals(1, root.lookupAll(".system-tag").size());
            Label system = (Label) root.lookup(".system-tag");
            assertEquals("update", system.getText());
            assertEquals(Color.web("#F4C542"), system.getBackground().getFills().get(0).getFill());
            assertEquals(Color.web("#222222"), system.getTextFill());
            List<Label> labels = root.lookupAll("#tags .label").stream().map(node -> (Label) node).toList();
            assertEquals(3, labels.size());
            Label custom = labels.stream().filter(label -> label.getText().equals("update (custom)"))
                    .findFirst().orElseThrow();
            assertEquals(Color.web("#3e7b91"), custom.getBackground().getFills().get(0).getFill());
            assertEquals(system.getPadding(), custom.getPadding());
            assertEquals(system.getFont().getSize(), custom.getFont().getSize());
            assertEquals(system.getBackground().getFills().get(0).getRadii(),
                    custom.getBackground().getFills().get(0).getRadii());
            assertEquals(2, person.getTags().size());
            assertFalse(person.getTags().stream().anyMatch(tag -> tag.tagName.contains("custom")));
            Person edited = new PersonBuilder(person).withPhone("91234567").withLastUpdated(clock.date).build();
            PersonCard editedCard = new PersonCard(edited, 1, clock);
            assertTrue(editedCard.getRoot().lookupAll(".system-tag").isEmpty());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void commandBox_profileEditRemovesReminderWhileOtherCommandsPreserveIt() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Person original = new PersonBuilder().withLastUpdated(LocalDate.of(2024, 10, 1))
                    .withTags("Friend", "update").withNotes("Existing note").build();
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
                assertEquals(1, window.getRoot().getScene().getRoot().lookupAll(".system-tag").size());
                for (String command : new String[]{"fav 1", "listfav", "unfav 1", "list", "note 1 New note",
                    "tag 1 t/NOC", "untag 1 t/NOC", "list"}) {
                    enter(window, command);
                    assertEquals(original.getLastUpdated(), model.getAddressBook().getPersonList().get(0)
                            .getLastUpdated(), command);
                    assertEquals(model.getFilteredPersonList().size(),
                            window.getRoot().getScene().getRoot().lookupAll(".system-tag").size(), command);
                }
                enter(window, "fav 1");
                enter(window, "edit 1 p/91234567");
                assertTrue(window.getRoot().getScene().getRoot().lookupAll(".system-tag").isEmpty());
                Person edited = model.getAddressBook().getPersonList().get(0);
                assertEquals(LocalDate.now(), edited.getLastUpdated());
                assertEquals(original.getTags(), edited.getTags());
                assertTrue(edited.isFavourite());
                Label heart = (Label) window.getRoot().getScene().lookup("#favouriteIcon");
                heart.applyCss();
                assertTrue(heart.isVisible());
                assertEquals(Color.web("#e63946"), heart.getTextFill());
                List<Label> tags = window.getRoot().getScene().getRoot().lookupAll("#tags .label").stream()
                        .map(node -> (Label) node).toList();
                assertTrue(tags.stream().anyMatch(label -> label.getText().equals("update (custom)")));
                assertTrue(tags.stream().anyMatch(label -> label.getText().equals("Friend")));
                for (Label label : tags) {
                    label.applyCss();
                    assertEquals(Color.web("#3e7b91"), label.getBackground().getFills().get(0).getFill());
                }
                assertEquals(edited, new JsonAddressBookStorage(data).readAddressBook().orElseThrow()
                        .getPersonList().get(0));
            } finally {
                window.getRoot().close();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
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

    private static class TestClock extends Clock {
        private LocalDate date = LocalDate.of(2026, 10, 18);

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant(), zone);
        }

        @Override
        public Instant instant() {
            return date.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }
}
