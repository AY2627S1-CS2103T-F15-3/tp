package seedu.address.commons.exceptions;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Prevents saving over a contact file that could not be loaded in the current session.
 */
public class DataRecoveryRequiredException extends IOException {
    public static final String MESSAGE = "Contact data could not be loaded from %s.\n"
            + "Saving contacts is disabled for this session to protect the original file.\n"
            + "Close UniMate using the window's close button, back up and repair or restore the data file, "
            + "then reopen UniMate.";

    public DataRecoveryRequiredException(Path filePath, DataLoadingException cause) {
        super(String.format(MESSAGE, filePath), cause);
    }
}
