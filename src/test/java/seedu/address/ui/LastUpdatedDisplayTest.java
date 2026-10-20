package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies the saved profile date in actual contact cards and observable list cells.
 */
public class LastUpdatedDisplayTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final LocalDate OLD_DATE = LocalDate.of(2024, 2, 29);
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

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
    public void constructor_recentAndOldProfiles_alwaysShowsSavedDate() throws Exception {
        runOnFxThread(() -> {
            for (LocalDate date : List.of(TODAY, OLD_DATE)) {
                Person person = new PersonBuilder().withLastUpdated(date).build();
                PersonCard card = new PersonCard(person, 1, CLOCK);
                Label label = dateLabel(card.getRoot());

                assertEquals("Last updated: " + date, label.getText());
                assertTrue(label.isVisible());
                assertTrue(label.isManaged());
                assertNotNull(label.getTooltip());
                assertTrue(label.getTooltip().getText().contains("Tags, notes and favourites do not change"));
                assertEquals(date.equals(OLD_DATE) ? 1 : 0, card.getRoot().lookupAll(".system-tag").size());
            }
            return null;
        });
    }

    @Test
    public void list_editUndoRedoAndRestart_displaysPersistedDates() throws Exception {
        runOnFxThread(() -> {
            Person alice = new PersonBuilder().withName("Alice").withLastUpdated(OLD_DATE).build();
            Person bob = new PersonBuilder().withName("Bob").withLastUpdated(TODAY).build();
            Model model = new ModelManager();
            model.addPerson(alice);
            model.addPerson(bob);
            Path file = directory.resolve("contacts.json");
            LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(file),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            Scene scene = listScene(model);
            assertDate(scene, alice);
            assertDate(scene, bob);

            logic.execute("find Alice");
            assertDate(scene, alice);
            logic.execute("list");
            assertDate(scene, bob);
            for (String command : List.of("note 1 Follow up", "fav 1", "tag 1 t/Friend", "sort name asc")) {
                logic.execute(command);
                Person current = model.getFilteredPersonList().get(0);
                assertEquals(OLD_DATE, current.getLastUpdated());
                assertDate(scene, current);
            }

            Person beforeEdit = model.getFilteredPersonList().get(0);
            LocalDate before = LocalDate.now();
            logic.execute("edit 1 p/91234567");
            Person edited = model.getFilteredPersonList().get(0);
            assertTrue(edited.getLastUpdated().equals(before) || edited.getLastUpdated().equals(LocalDate.now()));
            assertDate(scene, edited);
            logic.execute("undo");
            assertDate(scene, beforeEdit);
            assertEquals(beforeEdit, new JsonAddressBookStorage(file).readAddressBook().orElseThrow()
                    .getPersonList().get(0));
            logic.execute("redo");
            assertDate(scene, edited);

            AddressBook saved = new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
            Model reopened = new ModelManager(saved, new UserPrefs());
            Scene reopenedScene = listScene(reopened);
            assertEquals(edited, reopened.getFilteredPersonList().get(0));
            assertDate(reopenedScene, edited);
            assertDate(reopenedScene, bob);
            return null;
        });
    }

    private Scene listScene(Model model) {
        PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
        Scene scene = new Scene(panel.getRoot(), 600, 600);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene;
    }

    private void assertDate(Scene scene, Person person) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        ListView<?> list = (ListView<?>) scene.lookup("#personListView");
        ListCell<?> cell = list.lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && person.equals(candidate.getItem()))
                .map(node -> (ListCell<?>) node)
                .findFirst().orElseThrow();
        Label label = dateLabel(cell.getGraphic());
        assertEquals("Last updated: " + person.getLastUpdated(), label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
        assertTrue(label.getHeight() > 0);
    }

    private Label dateLabel(Node card) {
        return assertInstanceOf(Label.class, card.lookup("#lastUpdated"));
    }

    private void runOnFxThread(java.util.concurrent.Callable<Void> action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
