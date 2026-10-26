package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonFilter;
import seedu.address.model.person.PersonSortComparator;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final AddressBookHistory addressBookHistory;
    private final ReadOnlyStringWrapper viewStatus = new ReadOnlyStringWrapper();

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
        addressBookHistory = new AddressBookHistory(this.addressBook);
        viewStatus.bind(Bindings.createStringBinding(this::describeView, this.addressBook.getPersonList(),
                sortedPersons, filteredPersons.predicateProperty(), sortedPersons.comparatorProperty()));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public void commitAddressBook(ReadOnlyAddressBook previousState, String description) {
        requireAllNonNull(previousState, description);
        addressBookHistory.commit(previousState, description);
    }

    @Override
    public boolean canUndoAddressBook() {
        return addressBookHistory.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return addressBookHistory.canRedo();
    }

    @Override
    public Optional<String> getUndoDescription() {
        return addressBookHistory.getUndoDescription();
    }

    @Override
    public Optional<String> getRedoDescription() {
        return addressBookHistory.getRedoDescription();
    }

    @Override
    public boolean undoAddressBook() {
        return addressBookHistory.undo();
    }

    @Override
    public boolean redoAddressBook() {
        return addressBookHistory.redo();
    }

    /**
     * Returns the observable, unmodifiable filtered and sorted view used by the UI and numbered commands.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public ReadOnlyStringProperty viewStatusProperty() {
        return viewStatus.getReadOnlyProperty();
    }

    private String describeView() {
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        String filter = predicate == null || predicate == PREDICATE_SHOW_ALL_PERSONS ? "All contacts" : "Custom filter";
        if (predicate instanceof PersonFilter personFilter) {
            filter = personFilter.getDescription();
        }
        Comparator<? super Person> comparator = sortedPersons.getComparator();
        String sort = comparator == null ? "Saved order" : "Custom order";
        if (comparator instanceof PersonSortComparator personSort) {
            sort = personSort.getDescription();
        }
        return String.format("Showing %d of %d contacts | Filter: %s | Sort: %s",
                sortedPersons.size(), addressBook.getPersonList().size(), filter, sort);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateSortedPersonList(Comparator<Person> comparator) {
        requireNonNull(comparator);
        sortedPersons.setComparator(comparator);
    }

    @Override
    public void resetSortedPersonList() {
        sortedPersons.setComparator(null);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && sortedPersons.equals(otherModelManager.sortedPersons);
    }

}
