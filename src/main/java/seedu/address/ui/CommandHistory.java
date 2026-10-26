package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores submitted commands and preserves the draft while browsing a session's history.
 */
public class CommandHistory {
    private final List<String> commands = new ArrayList<>();
    private int position;
    private String draft = "";
    private String searchPrefix;

    /**
     * Records a non-empty submission and resets navigation to the end.
     */
    public void add(String command) {
        if (command.isEmpty()) {
            return;
        }
        commands.add(command);
        position = commands.size();
        draft = "";
        resetPrefixSearch();
    }

    /**
     * Returns the previous command, or null when history is empty.
     * Saves the current input when leaving the draft position.
     */
    public String previous(String currentInput) {
        resetPrefixSearch();
        if (commands.isEmpty()) {
            return null;
        }
        if (position == commands.size()) {
            draft = currentInput;
        }
        position = Math.max(0, position - 1);
        return commands.get(position);
    }

    /**
     * Returns the next command or saved draft, or null when already at the draft position.
     */
    public String next() {
        resetPrefixSearch();
        if (position == commands.size()) {
            return null;
        }
        position++;
        return position == commands.size() ? draft : commands.get(position);
    }

    /**
     * Returns the previous command matching the input prefix, or null if none is older.
     * Repeated searches keep the initial prefix until editing or chronological navigation resets it.
     */
    public String previousMatching(String currentInput) {
        if (searchPrefix == null) {
            searchPrefix = currentInput;
        }
        if (position == commands.size()) {
            draft = currentInput;
        }
        for (int index = position - 1; index >= 0; index--) {
            if (commands.get(index).startsWith(searchPrefix)) {
                position = index;
                return commands.get(position);
            }
        }
        return null;
    }

    /**
     * Returns the next matching command, or the saved draft after the last match.
     * Without an active prefix search, leaves the input unchanged.
     */
    public String nextMatching() {
        if (searchPrefix == null || position == commands.size()) {
            return null;
        }
        for (int index = position + 1; index < commands.size(); index++) {
            if (commands.get(index).startsWith(searchPrefix)) {
                position = index;
                return commands.get(position);
            }
        }
        position = commands.size();
        resetPrefixSearch();
        return draft;
    }

    /**
     * Returns the one-based chronological position, or zero while at the draft.
     */
    public int getPosition() {
        return position == commands.size() ? 0 : position + 1;
    }

    /**
     * Returns the number of submitted commands, including duplicates.
     */
    public int size() {
        return commands.size();
    }

    /**
     * Lets the next prefix search use the current input after editing or changing navigation mode.
     */
    public void resetPrefixSearch() {
        searchPrefix = null;
    }
}
