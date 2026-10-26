package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Exercises the FXML wiring and keyboard event filters on the JavaFX application thread.
 */
public class CommandBoxTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                ready.countDown();
            });
        } catch (IllegalStateException e) {
            Platform.runLater(ready::countDown);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void navigation_recallsSubmissionsRestoresDraftAndDoesNotExecute() throws Exception {
        runOnFxThread(() -> {
            List<String> executed = new ArrayList<>();
            TextField field = field(new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }));
            submit(field, "list");
            assertEquals("", field.getText());
            submit(field, "help");
            field.setText("find draft");
            press(field, KeyCode.UP);
            assertEquals("help", field.getText());
            assertEquals(4, field.getCaretPosition());
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("help", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("find draft", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("find draft", field.getText());
            assertEquals(List.of("list", "help"), executed);
            press(field, KeyCode.UP);
            submit(field, "find edited");
            press(field, KeyCode.UP);
            assertEquals("find edited", field.getText());
            assertEquals(List.of("list", "help", "find edited"), executed);
        });
    }

    @Test
    public void emptyHistoryAndEmptySubmission_leaveDraftAndDoNotExecute() throws Exception {
        runOnFxThread(() -> {
            List<String> executed = new ArrayList<>();
            TextField field = field(new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }));
            submit(field, "");
            field.setText("draft");
            press(field, KeyCode.UP);
            press(field, KeyCode.DOWN);
            assertEquals("draft", field.getText());
            assertTrue(executed.isEmpty());
            List<KeyCode> propagated = new ArrayList<>();
            field.addEventHandler(KeyEvent.KEY_PRESSED, event -> propagated.add(event.getCode()));
            press(field, KeyCode.F2);
            press(field, KeyCode.UP);
            press(field, KeyCode.DOWN);
            assertEquals(List.of(KeyCode.F2), propagated);
        });
    }

    @Test
    public void failedSubmissions_areRecallableAndEditingClearsError() throws Exception {
        runOnFxThread(() -> {
            TextField field = field(new CommandBox(command -> {
                if (command.equals("invalid")) {
                    throw new ParseException("Invalid command");
                }
                throw new CommandException("Command failed");
            }));
            submit(field, "invalid");
            assertEquals("invalid", field.getText());
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            submit(field, "invalid");
            assertEquals(1, field.getStyleClass().stream()
                    .filter(CommandBox.ERROR_STYLE_CLASS::equals).count());
            field.setText("delete 999");
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            submit(field, "delete 999");
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(field, KeyCode.UP);
            press(field, KeyCode.UP);
            assertEquals("invalid", field.getText());
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(field, KeyCode.DOWN);
            assertEquals("delete 999", field.getText());
        });
    }

    @Test
    public void prefixShortcut_preservesPrefixDraftCaretAndDoesNotExecute() throws Exception {
        runOnFxThread(() -> {
            List<String> executed = new ArrayList<>();
            TextField field = field(new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            }));
            submit(field, "add n/Alice");
            submit(field, "list");
            submit(field, "add n/Bob");
            submit(field, "list");
            field.setText("add ");
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("add ", field.getText());
            List<KeyCode> propagated = new ArrayList<>();
            field.addEventHandler(KeyEvent.KEY_PRESSED, event -> propagated.add(event.getCode()));
            pressAlt(field, KeyCode.UP);
            assertEquals("add n/Bob", field.getText());
            assertEquals(field.getText().length(), field.getCaretPosition());
            pressAlt(field, KeyCode.UP);
            assertEquals("add n/Alice", field.getText());
            pressAlt(field, KeyCode.UP);
            assertEquals("add n/Alice", field.getText());
            pressAlt(field, KeyCode.DOWN);
            assertEquals("add n/Bob", field.getText());
            pressAlt(field, KeyCode.DOWN);
            assertEquals("add ", field.getText());
            pressAlt(field, KeyCode.DOWN);
            assertEquals("add ", field.getText());
            assertTrue(propagated.isEmpty());
            assertEquals(List.of("add n/Alice", "list", "add n/Bob", "list"), executed);
        });
    }

    @Test
    public void prefixShortcut_editRestartsSearchAndOrdinaryArrowsResetIt() throws Exception {
        runOnFxThread(() -> {
            TextField field = field(new CommandBox(command -> new CommandResult("OK")));
            submit(field, "add n/Alice");
            submit(field, "list");
            submit(field, "add n/Bob");
            submit(field, "help");
            field.setText("add ");
            pressAlt(field, KeyCode.UP);
            field.setText("li");
            pressAlt(field, KeyCode.UP);
            assertEquals("list", field.getText());
            pressAlt(field, KeyCode.DOWN);
            assertEquals("add ", field.getText());
            field.setText("missing");
            pressAlt(field, KeyCode.UP);
            assertEquals("missing", field.getText());
            press(field, KeyCode.UP);
            assertEquals("help", field.getText());
            pressAlt(field, KeyCode.DOWN);
            assertEquals("help", field.getText());
            press(field, KeyCode.DOWN);
            assertEquals("missing", field.getText());
        });
    }

    @Test
    public void historyPosition_duplicatesAndBoundaryPressesRestartFeedback() throws Exception {
        runOnFxThread(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("OK");
            });
            TextField field = field(box);
            Label label = (Label) box.getRoot().lookup("#historyPositionLabel");
            assertFalse(label.isVisible());
            assertFalse(label.isManaged());
            press(field, KeyCode.UP);
            assertFalse(label.isVisible());
            submit(field, "list");
            submit(field, "list");
            submit(field, "list");
            field.setText("draft");
            for (int position = 3; position >= 1; position--) {
                press(field, KeyCode.UP);
                assertEquals("list", field.getText());
                assertEquals("History " + position + "/3", label.getText());
                assertTrue(label.isVisible());
                assertTrue(label.isManaged());
            }
            label.setOpacity(1.0);
            press(field, KeyCode.UP);
            assertEquals("History 1/3", label.getText());
            assertEquals(0.35, label.getOpacity(), 0.001);
            press(field, KeyCode.DOWN);
            assertEquals("History 2/3", label.getText());
            press(field, KeyCode.DOWN);
            assertEquals("History 3/3", label.getText());
            press(field, KeyCode.DOWN);
            assertEquals("draft", field.getText());
            assertFalse(label.isVisible());
            assertFalse(label.isManaged());
            press(field, KeyCode.DOWN);
            assertFalse(label.isVisible());
            press(field, KeyCode.UP);
            submit(field, "help");
            assertFalse(label.isVisible());
            assertEquals(List.of("list", "list", "list", "help"), executed);
        });
    }

    @Test
    public void historyPosition_prefixUsesAbsolutePositionAndFailedSubmissionHidesLabel() throws Exception {
        runOnFxThread(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new ParseException("Invalid command");
            });
            TextField field = field(box);
            Label label = (Label) box.getRoot().lookup("#historyPositionLabel");
            submit(field, "add n/Alice");
            submit(field, "list");
            submit(field, "add n/Bob");
            submit(field, "list");
            field.setText("add ");
            pressAlt(field, KeyCode.UP);
            assertEquals("History 3/4", label.getText());
            pressAlt(field, KeyCode.UP);
            assertEquals("History 1/4", label.getText());
            label.setOpacity(1.0);
            pressAlt(field, KeyCode.UP);
            assertEquals(0.35, label.getOpacity(), 0.001);
            pressAlt(field, KeyCode.DOWN);
            assertEquals("History 3/4", label.getText());
            pressAlt(field, KeyCode.DOWN);
            assertFalse(label.isVisible());
            pressAlt(field, KeyCode.UP);
            submit(field, "invalid");
            assertFalse(label.isVisible());
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private static void pressAlt(TextField field, KeyCode code) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, true, false));
    }

    private static TextField field(CommandBox box) {
        return (TextField) box.getRoot().lookup("#commandTextField");
    }

    private static void submit(TextField field, String command) {
        field.setText(command);
        field.fireEvent(new ActionEvent());
    }

    private static void press(TextField field, KeyCode code) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
