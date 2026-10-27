package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Common window setup and command interaction for tests running on the JavaFX thread. */
final class MainWindowTestUtil {
    private MainWindowTestUtil() {
    }

    static MainWindow createWindow(Path directory, Person... contacts) {
        ModelManager model = new ModelManager();
        for (Person contact : contacts) {
            model.addPerson(contact);
        }
        Path data = directory.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), data);
        window.show();
        window.fillInnerParts();
        FxTestUtil.layout(window.getRoot().getScene().getRoot());
        return window;
    }

    static void enter(MainWindow window, String command) {
        TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        FxTestUtil.layout(window.getRoot().getScene().getRoot());
    }

    static ListView<?> list(MainWindow window) {
        return (ListView<?>) window.getRoot().getScene().lookup("#personListView");
    }

    static String feedback(MainWindow window) {
        return ((TextArea) window.getRoot().getScene().lookup("#resultDisplay")).getText();
    }

    static void assertCard(MainWindow window, Person person, String index) {
        ListCell<?> cell = list(window).lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> candidate && person.equals(candidate.getItem()))
                .map(node -> (ListCell<?>) node).findFirst().orElseThrow(() -> new AssertionError(
                        "Expected card " + person.getName() + "; visible indices: "
                        + list(window).lookupAll(".list-cell").stream()
                                .map(node -> ((ListCell<?>) node).getIndex()).toList()));
        assertEquals(index, ((Label) cell.getGraphic().lookup("#id")).getText());
        assertEquals(person.getName().fullName, ((Label) cell.getGraphic().lookup("#name")).getText());
    }
}
