package seedu.address.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Records a completed change from {@code previousState} to the current address book state,
     * so that the change can be undone. Clears any redo history.
     * Does nothing if the change did not modify the address book data.
     */
    void commitAddressBook(ReadOnlyAddressBook previousState);

    /**
     * Returns true if there is a recorded address book state to undo to.
     * Returns false if the address book was changed without being recorded since the last recorded state.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if there is an undone address book state to redo to.
     * Returns false if the address book was changed without being recorded since the last undo.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the address book to its state before the most recent recorded change.
     *
     * @return true if a state was restored, or false (leaving the address book unchanged) if there is none.
     */
    boolean undoAddressBook();

    /**
     * Restores the address book to its state before the most recent undo.
     *
     * @return true if a state was restored, or false (leaving the address book unchanged) if there is none.
     */
    boolean redoAddressBook();

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
