package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.DataRecoveryRequiredException;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks the initial recovery warning and real command-box saves against rejected data. */
public class DataRecoveryUiTest {
    @TempDir
    public Path directory;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void start_failedLoad_showsWarningAndProtectsFileAcrossCommands() throws Exception {
        Path dataPath = directory.resolve("contacts.json");
        String original = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\","
                + "\"graduationYear\":\"1999\"}]}";
        Files.writeString(dataPath, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        FxTestUtil.runOnFxThread(() -> {
            String warning = String.format(DataRecoveryRequiredException.MESSAGE, dataPath);
            Stage stage = new Stage();
            stage.setOpacity(0);
            UiManager ui = new UiManager(new LogicManager(new ModelManager(), storage), dataPath, warning);
            try {
                ui.start(stage);
                FxTestUtil.layout(stage.getScene().getRoot());
                TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals(warning, result.getText());
                assertTrue(result.getStyleClass().contains(ResultDisplay.WARNING_STYLE_CLASS));
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");

                for (String command : List.of("list", "add n/RecoveryProbe a/NUS", "exit")) {
                    input.setText(command);
                    input.fireEvent(new ActionEvent());
                    assertTrue(stage.isShowing());
                    assertTrue(result.getText().contains("disabled for this session"));
                    assertEquals(command, input.getText());
                    assertEquals(original, Files.readString(dataPath));
                }
            } finally {
                stage.hide();
            }
            return null;
        });
    }
}
