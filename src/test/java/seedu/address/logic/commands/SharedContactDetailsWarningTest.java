package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Checks that advisory warnings append to, rather than replace, command-specific success feedback.
 */
public class SharedContactDetailsWarningTest {
    @Test
    public void appendTo_noMatches_preservesCompleteSuccessFeedback() {
        String success = "Edited person: Bob\nTags replaced. Removed: Old. Added: Project.";

        CommandResult result = SharedContactDetailsWarning.appendTo(success, "");

        assertEquals(success, result.getFeedbackToUser());
        assertFalse(result.isWarning());
    }

    @Test
    public void appendTo_multipleSuccessLines_keepsWarningLastWithoutDroppingSummaries() {
        String success = "Edited person: Bob\nTags replaced. Removed: Old. Added: Project.";

        CommandResult result = SharedContactDetailsWarning.appendTo(success, "#1 Alice (phone)");

        assertEquals(success + "\nWarning - duplicate details detected: #1 Alice (phone)", result.getFeedbackToUser());
        assertEquals("Edited person: Bob", result.getLogSummary());
        assertTrue(result.isWarning());
    }

    @Test
    public void appendTo_alternativeAddSuccessPrefix_doesNotReplaceItWithOrdinaryAddFeedback() {
        String success = "New favourite contact added: Alice";

        CommandResult result = SharedContactDetailsWarning.appendTo(success, "#2 Bob (email)");

        assertEquals(success + "\nWarning - duplicate details detected: #2 Bob (email)", result.getFeedbackToUser());
        assertTrue(result.isWarning());
    }
}
