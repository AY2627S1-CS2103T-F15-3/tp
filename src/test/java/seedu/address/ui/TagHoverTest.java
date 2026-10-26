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
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.testutil.PersonBuilder;

public class TagHoverTest {
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
    public void tag_twentyCharacterBoundaryAndHoverPopup_preserveFullValue() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            String fullName = "A".repeat(500);
            PersonCard card = new PersonCard(new PersonBuilder()
                    .withTags(fullName, "B".repeat(20), "C".repeat(21), "friends", "colleagues").build(), 1);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 740, 450);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            try {
                root.applyCss();
                root.layout();
                assertEquals("B".repeat(20), find(root, "B".repeat(20)).getText());
                assertEquals("C".repeat(20) + "...", find(root, "C".repeat(21)).getText());
                assertEquals("colleagues", find(root, "colleagues").getText());
                Label longTag = find(root, fullName);
                assertEquals("A".repeat(20) + "...", longTag.getText());
                assertEquals(fullName, longTag.getTooltip().getText());
                assertFalse(longTag.isMouseTransparent());
                assertFalse(longTag.getTooltip().isShowing());
                Event.fireEvent(longTag, mouse(MouseEvent.MOUSE_ENTERED));
                assertTrue(longTag.getTooltip().isShowing());
                assertEquals(fullName, longTag.getTooltip().getText());
                Event.fireEvent(longTag, mouse(MouseEvent.MOUSE_EXITED));
                assertFalse(longTag.getTooltip().isShowing());
                assertTrue(card.person.getTags().stream().anyMatch(tag -> tag.tagName.equals(fullName)));
            } finally {
                find(root, fullName).getTooltip().hide();
                stage.close();
            }
            return null;
        });
        Platform.runLater(check);
        check.get(20, TimeUnit.SECONDS);
    }

    private Label find(StackPane root, String fullName) {
        return root.lookupAll("#tags .label").stream().map(node -> (Label) node)
                .filter(label -> label.getTooltip() != null && label.getTooltip().getText().equals(fullName))
                .findFirst().orElseThrow();
    }

    private MouseEvent mouse(EventType<MouseEvent> type) {
        return new MouseEvent(type, 1, 1, 100, 100, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null);
    }
}
