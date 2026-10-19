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
    }

    /**
     * Returns the previous command, or null when history is empty.
     * Saves the current input when leaving the draft position.
     */
    public String previous(String currentInput) {
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
        if (position == commands.size()) {
            return null;
        }
        position++;
        return position == commands.size() ? draft : commands.get(position);
    }
}
