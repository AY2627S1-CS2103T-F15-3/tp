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
import org.junit.jupiter.api.BeforeEach;
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

    private Person alice;
    private Person bob;
    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private Scene scene;

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

    @BeforeEach
    public void setUp() throws Exception {
        runOnFxThread(() -> {
            alice = new PersonBuilder().withName("Alice").withLastUpdated(OLD_DATE).build();
            bob = new PersonBuilder().withName("Bob").withLastUpdated(TODAY).build();
            model = new ModelManager();
            model.addPerson(alice);
            model.addPerson(bob);
            storage = new JsonAddressBookStorage(directory.resolve("contacts.json"));
            logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            scene = listScene(model);
            return null;
        });
    }

    @Test
    public void constructor_recentAndOldProfiles_alwaysShowsSavedDate() throws Exception {
        runOnFxThread(() -> {
            for (LocalDate date : List.of(TODAY, OLD_DATE)) {
                Person person = new PersonBuilder().withLastUpdated(date).build();
                PersonCard card = new PersonCard(person, 1, CLOCK);
                Label label = dateLabel(card.getRoot());

                assertEquals(date.toString(), label.getText());
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
    public void constructor_unknownDate_showsUnknownWithoutUpdateReminder() throws Exception {
        runOnFxThread(() -> {
            Person unknown = new PersonBuilder(alice).withLastUpdated(null).build();
            PersonCard card = new PersonCard(unknown, 1, CLOCK);
            String tooltip = dateLabel(card.getRoot()).getTooltip().getText();

            assertEquals("Unknown", dateLabel(card.getRoot()).getText());
            assertTrue(tooltip.contains("Older contacts without a saved date show Unknown"));
            assertTrue(tooltip.contains("sort last in both date directions"));
            assertEquals(0, card.getRoot().lookupAll(".system-tag").size());
            return null;
        });
    }

    @Test
    public void list_afterSearch_preservesDisplayedDates() throws Exception {
        runOnFxThread(() -> {
            logic.execute("find Alice");
            assertDate(scene, alice);
            logic.execute("list");

            assertDate(scene, alice);
            assertDate(scene, bob);
            assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
            return null;
        });
    }

    @Test
    public void annotationsAndSort_preserveDisplayedProfileDate() throws Exception {
        runOnFxThread(() -> {
            for (String command : List.of("note 1 Follow up", "fav 1", "tag 1 t/Friend", "sort name asc")) {
                logic.execute(command);
                Person current = model.getFilteredPersonList().get(0);
                assertEquals(OLD_DATE, current.getLastUpdated());
                assertDate(scene, current);
            }
            return null;
        });
    }

    @Test
    public void edit_refreshesDisplayedDate() throws Exception {
        runOnFxThread(() -> {
            LocalDate before = LocalDate.now();
            Person edited = editAlice();

            assertTrue(edited.getLastUpdated().equals(before) || edited.getLastUpdated().equals(LocalDate.now()));
            assertEquals(new PersonBuilder(alice).withPhone("91234567")
                    .withLastUpdated(edited.getLastUpdated()).build(), edited);
            assertDate(scene, edited);
            assertDate(scene, bob);
            return null;
        });
    }

    @Test
    public void undo_restoresOriginalDisplayedAndSavedDate() throws Exception {
        runOnFxThread(() -> {
            editAlice();

            logic.execute("undo");

            assertDate(scene, alice);
            assertEquals(alice, storage.readAddressBook().orElseThrow().getPersonList().get(0));
            return null;
        });
    }

    @Test
    public void redo_restoresEditedDisplayedAndSavedDate() throws Exception {
        runOnFxThread(() -> {
            Person edited = editAlice();
            logic.execute("undo");

            logic.execute("redo");

            assertDate(scene, edited);
            assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().get(0));
            return null;
        });
    }

    @Test
    public void restart_preservesSavedDates() throws Exception {
        runOnFxThread(() -> {
            Person edited = editAlice();

            AddressBook saved = new AddressBook(new JsonAddressBookStorage(storage.getAddressBookFilePath())
                    .readAddressBook().orElseThrow());
            Model reopened = new ModelManager(saved, new UserPrefs());
            Scene reopenedScene = listScene(reopened);

            assertEquals(edited, reopened.getFilteredPersonList().get(0));
            assertDate(reopenedScene, edited);
            assertDate(reopenedScene, bob);
            return null;
        });
    }

    private Person editAlice() throws Exception {
        logic.execute("edit 1 p/91234567");
        return model.getFilteredPersonList().get(0);
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
        assertEquals(person.getLastUpdated().toString(), label.getText());
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
