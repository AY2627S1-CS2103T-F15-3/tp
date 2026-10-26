package seedu.address.ui;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Map<Person, Set<String>> expandedFields = new WeakHashMap<>();

    @FXML
    private ListView<Person> personListView;

    @FXML
    private Label viewStatus;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, Bindings.createStringBinding(() -> "Showing " + personList.size() + " contacts", personList));
    }

    /**
     * Creates a contact list with a persistent status that follows the current model view.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableValue<String> status) {
        super(FXML);
        viewStatus.textProperty().bind(status);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListCell(listView, expandedFields));
    }

    /**
     * Refreshes visible cards so date-based reminders are recalculated, even if the list is unchanged.
     */
    public void refresh() {
        personListView.refresh();
    }
}
