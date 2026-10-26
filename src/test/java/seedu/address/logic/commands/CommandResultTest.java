package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandResultTest {
    @Test
    public void warning_marksSuccessfulResultWithoutRequestingHelpOrExit() {
        CommandResult warning = CommandResult.warning("Warning: Contact added.");

        assertEquals("Warning: Contact added.", warning.getFeedbackToUser());
        assertTrue(warning.isWarning());
        assertFalse(warning.isShowHelp());
        assertFalse(warning.isExit());
        assertFalse(new CommandResult("feedback").isWarning());
        assertFalse(new CommandResult("feedback", true, false).isWarning());
    }

    @Test
    public void getLogSummary_omitsDetailedNoteTextWithoutChangingFeedback() {
        String feedback = "Note replaced for Alice. Use undo to restore the previous note."
                + "\nPrevious note: Private previous text\nSaved note: Private saved text";
        CommandResult commandResult = new CommandResult(feedback);

        assertEquals("Note replaced for Alice. Use undo to restore the previous note.", commandResult.getLogSummary());
        assertEquals(feedback, commandResult.getFeedbackToUser());
        assertEquals("Listed all persons.", new CommandResult("Listed all persons.").getLogSummary());
        assertEquals("", new CommandResult("").getLogSummary());
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // warning styling is part of the result, not inferred from the feedback text
        assertFalse(commandResult.equals(CommandResult.warning("feedback")));
        assertTrue(CommandResult.warning("feedback").equals(CommandResult.warning("feedback")));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        assertEquals(CommandResult.warning("feedback").hashCode(), CommandResult.warning("feedback").hashCode());
        assertNotEquals(commandResult.hashCode(), CommandResult.warning("feedback").hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", warning=" + commandResult.isWarning() + "}";
        assertEquals(expected, commandResult.toString());
    }
}
