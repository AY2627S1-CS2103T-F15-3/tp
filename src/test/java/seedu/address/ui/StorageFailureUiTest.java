package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runActionOnFxThread;
import static seedu.address.ui.MainWindowTestUtil.enter;
import static seedu.address.ui.MainWindowTestUtil.feedback;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises help and exit through the command box while contact saving fails. */
public class StorageFailureUiTest {
    @TempDir
    public Path directory;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void commandBox_helpAndExitWithoutChanges_workWithUnwritableStorage() throws Exception {
        runActionOnFxThread(() -> {
            FailingAddressBookStorage storage = new FailingAddressBookStorage(directory.resolve("contacts.json"));
            MainWindow window = createWindow(storage);
            try {
                enter(window, "help");
                assertTrue(isHelpShowing());
                assertEquals("Opened help window.", feedback(window));
                assertTrue(window.getRoot().isShowing());

                enter(window, "exit");
                assertFalse(window.getRoot().isShowing());
                assertFalse(isHelpShowing());
                assertEquals(0, storage.saveAttempts);
            } finally {
                closeWindows(window);
            }
        });
    }

    @Test
    public void commandBox_pendingSaveFailure_opensHelpAndKeepsExitOpenUntilSaved() throws Exception {
        runActionOnFxThread(() -> {
            FailingAddressBookStorage storage = new FailingAddressBookStorage(directory.resolve("contacts.json"));
            MainWindow window = createWindow(storage);
            try {
                enter(window, "add n/Unsaved a/NUS");
                assertTrue(feedback(window).contains("not saved"));
                enter(window, "help");
                assertTrue(isHelpShowing());
                assertEquals(1, storage.saveAttempts);

                enter(window, "exit");
                assertTrue(window.getRoot().isShowing());
                assertTrue(feedback(window).contains(LogicManager.MESSAGE_UNSAVED_EXIT.strip()));
                assertEquals(2, storage.saveAttempts);

                storage.failSaving = false;
                enter(window, "exit");
                assertFalse(window.getRoot().isShowing());
                assertFalse(isHelpShowing());
                assertEquals(3, storage.saveAttempts);
            } finally {
                closeWindows(window);
            }
        });
    }

    private MainWindow createWindow(FailingAddressBookStorage storage) {
        ModelManager model = new ModelManager();
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json")))),
                storage.getAddressBookFilePath());
        window.show();
        window.fillInnerParts();
        FxTestUtil.layout(window.getRoot().getScene().getRoot());
        return window;
    }

    private static boolean isHelpShowing() {
        return Window.getWindows().stream().anyMatch(window -> window.isShowing()
                && window.getScene() != null && window.getScene().lookup("#helpMessage") != null);
    }

    private static void closeWindows(MainWindow mainWindow) {
        for (Window window : List.copyOf(Window.getWindows())) {
            if (window == mainWindow.getRoot()
                    || (window.getScene() != null && window.getScene().lookup("#helpMessage") != null)) {
                window.hide();
            }
        }
    }

    private static class FailingAddressBookStorage extends JsonAddressBookStorage {
        private boolean failSaving = true;
        private int saveAttempts;

        FailingAddressBookStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (failSaving) {
                throw new AccessDeniedException("contacts.json");
            }
            super.saveAddressBook(addressBook);
        }
    }
}
