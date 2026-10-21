package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    @Test
    public void emptyHistory_navigationDoesNotReplaceInput() {
        CommandHistory history = new CommandHistory();
        history.add("");
        assertNull(history.previous("draft"));
        assertNull(history.next());
    }

    @Test
    public void navigation_preservesOrderBoundariesAndDraft() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("invalid command");
        assertEquals("invalid command", history.previous("unfinished"));
        assertEquals("list", history.previous("invalid command"));
        assertEquals("list", history.previous("list"));
        assertEquals("invalid command", history.next());
        assertEquals("unfinished", history.next());
        assertNull(history.next());
    }

    @Test
    public void submissionWhileBrowsing_resetsNavigationAndKeepsDuplicates() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.previous("old draft");
        history.add("list");
        assertEquals("list", history.previous("new draft"));
        assertEquals("list", history.previous("list"));
        assertEquals("list", history.next());
        assertEquals("new draft", history.next());
    }
}
