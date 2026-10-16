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
 * (e.g. by a command that is not undoable) and the history is stale
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

}
