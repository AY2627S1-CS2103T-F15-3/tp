package seedu.address.commons.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    private static final FileOperations FILE_OPERATIONS = new FileOperations() {
        @Override
        public OutputStream openOutputStream(Path file) throws IOException {
            return Files.newOutputStream(file);
        }

        @Override
        public void replace(Path source, Path target) throws IOException {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public void deleteIfExists(Path file) throws IOException {
            Files.deleteIfExists(file);
        }
    };

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes and closes a sibling temporary file before atomically replacing the destination.
     * Creates missing parent directories, but leaves the destination untouched if writing fails.
     * An unsupported atomic replacement is reported as an error instead of risking a partial save.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, FILE_OPERATIONS);
    }

    /**
     * Performs the same save using the supplied file operations, allowing failures to be tested in isolation.
     */
    static void writeToFile(Path file, String content, FileOperations operations) throws IOException {
        Path target = file.toAbsolutePath().normalize();
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            target = target.toRealPath();
            if (!Files.isRegularFile(target) || !Files.isWritable(target)) {
                throw new AccessDeniedException(file.toString());
            }
        }
        createParentDirsOfFile(target);
        Path temporary = Files.createTempFile(target.getParent(), ".unimate-save-", ".tmp");
        try {
            try (OutputStream output = operations.openOutputStream(temporary)) {
                output.write(content.getBytes(CHARSET));
            }
            operations.replace(temporary, target);
        } catch (IOException | RuntimeException | Error failure) {
            try {
                operations.deleteIfExists(temporary);
            } catch (IOException | RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    /**
     * File operations used after a temporary file has been created for a save.
     */
    interface FileOperations {
        OutputStream openOutputStream(Path file) throws IOException;

        void replace(Path source, Path target) throws IOException;

        void deleteIfExists(Path file) throws IOException;
    }
}
