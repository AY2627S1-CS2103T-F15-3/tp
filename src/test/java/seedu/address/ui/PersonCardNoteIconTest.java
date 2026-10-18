package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URL;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Notes;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises note indicators in real JavaFX contact cards and rendered list cells.
 */
public class PersonCardNoteIconTest {

    @BeforeAll
    public static void startToolkit() throws Exception {
        boolean hasDisplay = !System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null;
        assertTrue(hasDisplay || !Boolean.parseBoolean(System.getenv("CI")),
                "Linux CI must provide a display for note icon tests, for example with xvfb-run");
        assumeTrue(hasDisplay, "JavaFX UI tests require a display");

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
    public void constructor_withoutNotes_hidesNoteIconAndRemovesLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            Label icon = noteIcon(card.getRoot());

            assertFalse(icon.isVisible());
            assertFalse(icon.isManaged());
        });
    }

    @Test
    public void constructor_withNotes_showsNoteIconAndDisplayedIndexTooltip() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withNotes("Ask about the group project").build();
            PersonCard card = new PersonCard(person, 7);

            assertNoteIcon(card.getRoot(), true, 7);
            assertEquals("This contact has a note", noteIcon(card.getRoot()).getAccessibleText());
        });
    }

    @Test
    public void constructor_withNotes_loadsPackagedNoteImage() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withNotes("Follow up next week").build(), 1);
            Label icon = noteIcon(card.getRoot());
            ImageView graphic = assertInstanceOf(ImageView.class, icon.getGraphic());
            Image image = graphic.getImage();
            URL resource = PersonCard.class.getResource("/images/noteicon.png");

            assertNotNull(resource, "The note image must be available on the application classpath");
            assertNotNull(image);
            assertFalse(image.isError());
            assertTrue(image.getWidth() > 0);
            assertTrue(image.getHeight() > 0);
            assertEquals(resource.toExternalForm(), image.getUrl());
            assertEquals(24.0, graphic.getFitWidth());
            assertEquals(24.0, graphic.getFitHeight());
            assertTrue(graphic.isPreserveRatio());
            assertEquals(ContentDisplay.GRAPHIC_ONLY, icon.getContentDisplay());
        });
    }

    @Test
    public void list_noteChanges_refreshesNoteIcon() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person original = new PersonBuilder().build();
            model.addPerson(original);
            Scene scene = createListScene(model);
            ListView<?> list = (ListView<?>) scene.lookup("#personListView");
            list.getSelectionModel().select(0);
            assertNoteIcon(renderedCard(scene, original), false, 1);

            Person withNote = original.withNotes(new Notes("Discuss the assignment"));
            model.setPerson(original, withNote);
            assertNoteIcon(renderedCard(scene, withNote), true, 1);

            Person withUpdatedNote = withNote.withNotes(new Notes("Assignment submitted"));
            model.setPerson(withNote, withUpdatedNote);
            assertNoteIcon(renderedCard(scene, withUpdatedNote), true, 1);

            Person withoutNote = withUpdatedNote.withNotes(Notes.EMPTY);
            model.setPerson(withUpdatedNote, withoutNote);
            assertNoteIcon(renderedCard(scene, withoutNote), false, 1);
        });
    }

    @Test
    public void list_findAndList_updatesTooltipTargets() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").withNotes("Study group").build();
            Person bob = new PersonBuilder().withName("Bob").withNotes("Project teammate").build();
            model.addPerson(alice);
            model.addPerson(bob);
            Scene scene = createListScene(model);
            assertTooltipTarget(scene, model, alice, 1);
            assertTooltipTarget(scene, model, bob, 2);

            executeCommand(model, "find Bob");
            assertEquals(List.of(bob), model.getFilteredPersonList());
            assertTooltipTarget(scene, model, bob, 1);

            executeCommand(model, "list");
            assertEquals(List.of(alice, bob), model.getFilteredPersonList());
            assertTooltipTarget(scene, model, alice, 1);
            assertTooltipTarget(scene, model, bob, 2);
            assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        });
    }

    @Test
    public void list_repeatedFindsAndNoMatches_updatesTooltipTargets() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice Lee").withNotes("Study group").build();
            Person bob = new PersonBuilder().withName("Bob Lee").withNotes("Project teammate").build();
            model.addPerson(alice);
            model.addPerson(bob);
            Scene scene = createListScene(model);
            assertTooltipTarget(scene, model, bob, 2);

            executeCommand(model, "find Bob");
            assertEquals(List.of(bob), model.getFilteredPersonList());
            assertTooltipTarget(scene, model, bob, 1);

            executeCommand(model, "find Alice");
            assertEquals(List.of(alice), model.getFilteredPersonList());
            assertTooltipTarget(scene, model, alice, 1);

            executeCommand(model, "find Nobody");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            ListView<?> list = (ListView<?>) scene.lookup("#personListView");
            assertTrue(list.getItems().isEmpty());
            assertTrue(model.getFilteredPersonList().isEmpty());

            executeCommand(model, "find Lee");
            assertEquals(List.of(alice, bob), model.getFilteredPersonList());
            assertTooltipTarget(scene, model, alice, 1);
            assertTooltipTarget(scene, model, bob, 2);
            assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
        });
    }

    private void executeCommand(Model model, String command) {
        assertDoesNotThrow(() -> new AddressBookParser().parseCommand(command).execute(model));
    }

    private void assertTooltipTarget(Scene scene, Model model, Person expectedPerson, int displayedIndex) {
        Node card = renderedCard(scene, expectedPerson);
        assertNoteIcon(card, true, displayedIndex);
        String tooltip = noteIcon(card).getTooltip().getText();
        int suggestedIndex = Integer.parseInt(tooltip.split(" ")[2]);
        Person target = model.getFilteredPersonList().get(suggestedIndex - 1);
        assertEquals(expectedPerson, target);
        assertEquals(expectedPerson.getNotes(), target.getNotes());
    }

    private Scene createListScene(Model model) {
        PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
        Scene scene = new Scene(panel.getRoot(), 600, 480);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene;
    }

    private Node renderedCard(Scene scene, Person person) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        ListView<?> list = (ListView<?>) scene.lookup("#personListView");
        ListCell<?> cell = list.lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && person.equals(candidate.getItem()))
                .map(node -> (ListCell<?>) node)
                .findFirst().orElseThrow();
        assertNotNull(cell.getGraphic());
        return cell.getGraphic();
    }

    private Label noteIcon(Node card) {
        return assertInstanceOf(Label.class, card.lookup("#noteIcon"));
    }

    private void assertNoteIcon(Node card, boolean expectedVisible, int displayedIndex) {
        Label icon = noteIcon(card);
        assertEquals(expectedVisible, icon.isVisible());
        assertEquals(expectedVisible, icon.isManaged());
        assertNotNull(icon.getTooltip());
        assertEquals("Use shownote " + displayedIndex + " to view this note.", icon.getTooltip().getText());
        assertEquals(displayedIndex + ". ", assertInstanceOf(Label.class, card.lookup("#id")).getText());
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
