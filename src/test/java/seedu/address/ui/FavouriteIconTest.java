package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.testutil.PersonBuilder;

public class FavouriteIconTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux")
                || System.getenv("DISPLAY") != null, "JavaFX rendering requires a display");
        FutureTask<Void> started = new FutureTask<>(() -> null);
        try {
            Platform.startup(started);
        } catch (IllegalStateException e) {
            Platform.runLater(started);
        }
        started.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void render_favourite_displaysCenteredRedHeartWithCircularBorder() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withFavourite(true).build(), 1);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 500, 220);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();
            root.layout();
            Label icon = (Label) root.lookup("#favouriteIcon");
            assertTrue(icon.isVisible());
            assertTrue(icon.isManaged());
            assertEquals("\u2665", icon.getText());
            assertEquals(Color.web("#e63946"), icon.getTextFill());
            assertEquals(Color.web("#e63946"), icon.getBorder().getStrokes().get(0).getTopStroke());
            assertEquals(javafx.geometry.Pos.CENTER, icon.getAlignment());
            assertEquals(24, icon.getWidth(), 0.1);
            assertEquals(24, icon.getHeight(), 0.1);
            assertEquals(12, icon.getBorder().getStrokes().get(0).getRadii().getTopLeftHorizontalRadius(), 0.1);
            assertTrue(icon.getBoundsInParent().getMinX()
                    >= root.lookup("#name").getBoundsInParent().getMaxX());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void execute_addWithFavouriteFlag_showsHeartImmediately() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Model model = new ModelManager();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            Scene scene = new Scene(panel.getRoot(), 500, 300);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

            new AddressBookParser().parseCommand("add n/Alice a/NUS f/").execute(model);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Label icon = (Label) scene.lookup("#favouriteIcon");
            assertTrue(icon.isVisible());
            assertTrue(icon.isManaged());
            assertEquals("\u2665", icon.getText());
            assertEquals(Color.web("#e63946"), icon.getTextFill());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void execute_fav_refreshesVisibleListCard() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            StackPane root = new StackPane(panel.getRoot());
            Scene scene = new Scene(root, 500, 300);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/view/Extensions.css").toExternalForm());
            root.applyCss();
            root.layout();
            Label originalIcon = (Label) root.lookup("#favouriteIcon");
            assertFalse(originalIcon.isVisible());
            assertFalse(originalIcon.isManaged());
            new AddressBookParser().parseCommand("fav 1").execute(model);
            root.applyCss();
            root.layout();
            assertTrue(root.lookup("#favouriteIcon").isVisible());
            ListView<?> list = (ListView<?>) root.lookup(".list-view");
            list.getSelectionModel().select(0);
            root.applyCss();
            root.layout();
            Label selectedIcon = (Label) root.lookup("#favouriteIcon");
            // Virtualized cells can create their graphics during layout; style the new node before inspecting it.
            selectedIcon.applyCss();
            assertEquals(Color.web("#e63946"), selectedIcon.getTextFill());
            assertEquals(Color.web("#e63946"), selectedIcon.getBorder().getStrokes().get(0).getTopStroke());
            assertEquals(Color.TRANSPARENT, selectedIcon.getBackground().getFills().get(0).getFill());
            new AddressBookParser().parseCommand("unfav 1").execute(model);
            root.applyCss();
            root.layout();
            assertFalse(root.lookup("#favouriteIcon").isVisible());
            assertFalse(root.lookup("#favouriteIcon").isManaged());
            new AddressBookParser().parseCommand("fav 1").execute(model);
            root.applyCss();
            root.layout();
            assertTrue(root.lookup("#favouriteIcon").isVisible());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }
}
