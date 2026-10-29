package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * An in-memory undo/redo history of an {@code AddressBook}.
 *
 * <p>Snapshots are stored as defensive copies in two stacks:
 * <ul>
 *     <li>{@code undoStates} holds the states before each recorded change.</li>
 *     <li>{@code redoStates} holds the states that were undone.</li>
 * </ul>
 * Each snapshot is paired with a short description of the change it reverses or reapplies,
 * so that undo and redo can report which change was affected.
 * Undoing and redoing reset the data of the tracked address book in place,
 * so references to it (and lists backed by it) remain valid.
 *
 * <p>{@code currentState} is a copy of the state that the tracked address book is expected to be in.
 * If the tracked address book no longer matches it, a change was made without being recorded
 * (e.g. by a future command that forgets to commit its change) and the history is stale.
 * Stale history is never restored, so an old snapshot cannot silently revert unrecorded changes.
 */
public class AddressBookHistory {

    private final AddressBook trackedAddressBook;
    private final Deque<Change> undoStates = new ArrayDeque<>();
    private final Deque<Change> redoStates = new ArrayDeque<>();
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
     * described by {@code description}, and clears the redo history.
     * Does nothing if the change did not modify any data.
     */
    public void commit(ReadOnlyAddressBook previousState, String description) {
        requireNonNull(previousState);
        requireNonNull(description);

        if (hasSameData(previousState, trackedAddressBook)) {
            return;
        }

        if (!hasSameData(previousState, currentState)) {
            // Unrecorded changes were made before this change, so older snapshots are stale.
            undoStates.clear();
        }

        undoStates.push(new Change(new AddressBook(previousState), description));
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
     * Returns the description of the change that {@link #undo()} would reverse,
     * or an empty {@code Optional} if there is no valid state to undo to.
     */
    public Optional<String> getUndoDescription() {
        return canUndo() ? Optional.of(undoStates.peek().description()) : Optional.empty();
    }

    /**
     * Returns the description of the change that {@link #redo()} would reapply,
     * or an empty {@code Optional} if there is no valid state to redo to.
     */
    public Optional<String> getRedoDescription() {
        return canRedo() ? Optional.of(redoStates.peek().description()) : Optional.empty();
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

        Change undone = undoStates.pop();
        redoStates.push(new Change(currentState, undone.description()));
        restore(undone.state());
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

        Change redone = redoStates.pop();
        undoStates.push(new Change(currentState, redone.description()));
        restore(redone.state());
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

    /**
     * A snapshot of the address book paired with a description of the change between it and the adjacent state.
     */
    private record Change(AddressBook state, String description) {
    }
}
