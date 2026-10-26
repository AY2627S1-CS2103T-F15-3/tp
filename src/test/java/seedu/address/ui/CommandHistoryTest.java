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

    @Test
    public void prefixNavigation_skipsUnrelatedEntriesAndRestoresDraft() {
        CommandHistory history = mixedHistory();
        assertNull(history.nextMatching());
        assertEquals("add n/Bob", history.previousMatching("add "));
        assertEquals("add n/Alice", history.previousMatching("add n/Bob"));
        assertNull(history.previousMatching("add n/Alice"));
        assertEquals("add n/Bob", history.nextMatching());
        assertEquals("add ", history.nextMatching());
        assertNull(history.nextMatching());
        assertEquals("add n/Bob", history.previousMatching("add "));
        assertEquals("list", history.next());
        assertEquals("add ", history.next());
    }

    @Test
    public void prefixNavigation_noMatchPreservesCursorAndUsesExactPrefix() {
        CommandHistory history = mixedHistory();
        assertNull(history.previousMatching("Add "));
        assertNull(history.nextMatching());
        history.resetPrefixSearch();
        assertNull(history.previousMatching("add  "));
        assertEquals("list", history.previous("add  "));
        assertEquals("add  ", history.next());
        CommandHistory empty = new CommandHistory();
        assertNull(empty.previousMatching("add "));
        assertNull(empty.nextMatching());
    }

    @Test
    public void prefixNavigation_emptyPrefixAndDuplicatesRemainNavigable() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("list");
        assertEquals("list", history.previousMatching(""));
        assertEquals("list", history.previousMatching("list"));
        assertEquals("list", history.nextMatching());
        assertEquals("", history.nextMatching());
    }

    @Test
    public void prefixNavigation_editAndSubmissionResetSearch() {
        CommandHistory history = mixedHistory();
        assertEquals("add n/Bob", history.previousMatching("add "));
        history.resetPrefixSearch();
        assertEquals("list", history.previousMatching("li"));
        assertEquals("list", history.nextMatching());
        assertEquals("add ", history.nextMatching());
        history.previousMatching("add ");
        history.add("help");
        assertNull(history.nextMatching());
        assertEquals("help", history.previousMatching("he"));
        assertEquals("he", history.nextMatching());
    }

    @Test
    public void chronologicalNavigation_keepsDraftWhenSwitchingToPrefixSearch() {
        CommandHistory history = mixedHistory();
        assertEquals("list", history.previous("draft"));
        assertEquals("add n/Bob", history.previous("list"));
        assertEquals("add n/Alice", history.previousMatching("add "));
        assertEquals("list", history.next());
        assertNull(history.nextMatching());
        assertEquals("add n/Bob", history.next());
        assertEquals("list", history.next());
        assertEquals("draft", history.next());
    }

    @Test
    public void position_reportsAbsoluteEntryAndZeroAtDraft() {
        CommandHistory history = new CommandHistory();
        assertEquals(0, history.getPosition());
        assertEquals(0, history.size());
        history.add("list");
        history.add("list");
        assertEquals(2, history.size());
        assertEquals(0, history.getPosition());
        history.previous("draft");
        assertEquals(2, history.getPosition());
        history.previous("list");
        history.previous("list");
        assertEquals(1, history.getPosition());
        history.next();
        assertEquals(2, history.getPosition());
        history.next();
        assertEquals(0, history.getPosition());
        history.previous("draft");
        history.add("help");
        assertEquals(0, history.getPosition());
        assertEquals(3, history.size());
    }

    private static CommandHistory mixedHistory() {
        CommandHistory history = new CommandHistory();
        history.add("add n/Alice");
        history.add("list");
        history.add("add n/Bob");
        history.add("list");
        return history;
    }
}
