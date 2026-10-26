package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An in-memory undo/redo history of an {@code AddressBook}.
 *
 * <p>Snapshots are stored as defensive copies in two stacks:
 * {@code undoStates} holds the states before each recorded change
 * {@code redoStates} holds the states that were undone
 * Undoing and redoing reset the data of the tracked address book in place,
 * so references to it (and lists backed by it) remain valid.
 *
 * <p>{@code currentState} is a copy of the state that the tracked address book is expected to be in
 * If the tracked address book no longer matches it, a change was made without being recorded
 * (e.g. by a future command that forgets to commit its change) and the history is stale
 * Stale history is never restored, so an old snapshot cannot silently revert unrecorded changes.
 */
public class AddressBookHistory {

    private final AddressBook trackedAddressBook;
    private final Deque<AddressBook> undoStates = new ArrayDeque<>();
    private final Deque<AddressBook> redoStates = new ArrayDeque<>();
    private AddressBook currentState;

    /**
     * Creates an empty history that tracks changes to {@code trackedAddressBook}.
     */
    public AddressBookHistory(AddressBook trackedAddressBook) {
        requireNonNull(trackedAddressBook);
        this.trackedAddressBook = trackedAddressBook;
        currentState = new AddressBook(trackedAddressBook);
    }

    /**
     * Records a completed change from {@code previousState} to the current data of the tracked address book,
     * and clears the redo history.
     * Does nothing if the change did not modify any data.
     */
    public void commit(ReadOnlyAddressBook previousState) {
        requireNonNull(previousState);

        if (hasSameData(previousState, trackedAddressBook)) {
            return;
        }

        if (!hasSameData(previousState, currentState)) {
            // Unrecorded changes were made before this change, so older snapshots are stale.
            undoStates.clear();
        }

        undoStates.push(new AddressBook(previousState));
        redoStates.clear();
        currentState = new AddressBook(trackedAddressBook);
    }

    /**
     * Returns true if there is a valid state to undo to.
     */
    public boolean canUndo() {
        return !isStale() && !undoStates.isEmpty();
    }

    /**
     * Returns true if there is a valid state to redo to.
     */
    public boolean canRedo() {
        return !isStale() && !redoStates.isEmpty();
    }

    /**
     * Restores the tracked address book to the state before the most recent recorded change.
     *
     * @return true if a state was restored, or false (leaving the tracked address book unchanged)
     *     if there is no valid state to undo to.
     */
    public boolean undo() {
        discardIfStale();
        if (undoStates.isEmpty()) {
            return false;
        }

        redoStates.push(currentState);
        restore(undoStates.pop());
        return true;
    }

    /**
     * Restores the tracked address book to the state before the most recent undo.
     *
     * @return true if a state was restored, or false (leaving the tracked address book unchanged)
     *     if there is no valid state to redo to.
     */
    public boolean redo() {
        discardIfStale();
        if (redoStates.isEmpty()) {
            return false;
        }

        undoStates.push(currentState);
        restore(redoStates.pop());
        return true;
    }

    private void restore(AddressBook state) {
        currentState = state;
        trackedAddressBook.resetData(state);
    }

    private boolean isStale() {
        return !hasSameData(currentState, trackedAddressBook);
    }

    private void discardIfStale() {
        if (isStale()) {
            undoStates.clear();
            redoStates.clear();
            currentState = new AddressBook(trackedAddressBook);
        }
    }

    private static boolean hasSameData(ReadOnlyAddressBook first, ReadOnlyAddressBook second) {
        return first.getPersonList().equals(second.getPersonList());
    }
}
