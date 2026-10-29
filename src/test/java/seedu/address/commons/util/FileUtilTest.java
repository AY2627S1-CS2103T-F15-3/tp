package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    @TempDir
    public Path directory;

    @Test
    public void createIfMissing_missingParents_createsEmptyFile() throws IOException {
        Path target = directory.resolve("new/data/contacts.json");

        FileUtil.createIfMissing(target);

        assertTrue(Files.isRegularFile(target));
        assertEquals(0, Files.size(target));
    }

    @Test
    public void createIfMissing_existingFile_preservesContents() throws IOException {
        Path target = directory.resolve("contacts.json");
        byte[] original = "Previously saved contacts".getBytes(StandardCharsets.UTF_8);
        Files.write(target, original);

        FileUtil.createIfMissing(target);

        assertArrayEquals(original, Files.readAllBytes(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void createIfMissing_relativePathWithoutParent_createsEmptyFile() throws IOException {
        // A relative archive path exercises the no-parent case without writing to the working directory.
        try (FileSystem archive = FileSystems.newFileSystem(directory.resolve("test.zip"), Map.of("create", "true"))) {
            Path target = archive.getPath("contacts.json");

            FileUtil.createIfMissing(target);

            assertTrue(Files.isRegularFile(target));
            assertEquals(0, Files.size(target));
        }
    }

    @Test
    public void createIfMissing_parentIsFile_preservesParentAndReportsFailure() throws IOException {
        Path parent = Files.writeString(directory.resolve("data"), "keep this file");
        Path target = parent.resolve("contacts.json");

        assertThrows(IOException.class, () -> FileUtil.createIfMissing(target));

        assertEquals("keep this file", Files.readString(parent));
        assertFalse(Files.exists(target));
        assertEquals(List.of(parent), filesInDirectory());
    }

    @Test
    public void writeToFile_success_replacesExistingContentsAndRemovesTemporaryFile() throws IOException {
        Path target = directory.resolve("contacts.json");
        Files.writeString(target, "old data that must not remain after the shorter replacement");

        FileUtil.writeToFile(target, "{\"persons\":[]}");

        assertEquals("{\"persons\":[]}", Files.readString(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_missingParents_createsCompleteFile() throws IOException {
        Path target = directory.resolve("new/data/contacts.json");

        FileUtil.writeToFile(target, "{\"persons\":[]}");

        assertEquals("{\"persons\":[]}", Files.readString(target));
        try (var siblings = Files.list(target.getParent())) {
            assertEquals(List.of(target), siblings.toList());
        }
    }

    @Test
    public void writeToFile_unicodeContent_roundTripsUsingUtf8() throws IOException {
        Path target = directory.resolve("contacts.txt");
        String contents = "Café — 你好 😀\nSaved contact notes\n";

        FileUtil.writeToFile(target, contents);

        assertArrayEquals(contents.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(target));
        assertEquals(contents, FileUtil.readFromFile(target));
    }

    @Test
    public void writeToFile_nullContent_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path target = Files.writeString(directory.resolve("contacts.json"), "original saved data");

        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(target, null));

        assertEquals("original saved data", Files.readString(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_failureDuringWrite_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path target = directory.resolve("contacts.json");
        byte[] original = "{\"persons\":[{\"name\":\"Saved Contact\",\"address\":\"NUS\"}]}".getBytes("UTF-8");
        Files.write(target, original);
        IOException failure = new IOException("simulated failure after writing a prefix");
        FileUtil.FileOperations operations = failingWriter(failure);

        IOException thrown = assertThrows(IOException.class, () ->
                FileUtil.writeToFile(target, "replacement content".repeat(1000), operations));

        assertSame(failure, thrown);
        assertArrayEquals(original, Files.readAllBytes(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_firstSaveFailsDuringWrite_doesNotCreateDestination() throws IOException {
        Path target = directory.resolve("contacts.json");
        IOException failure = new IOException("simulated partial write");

        assertSame(failure, assertThrows(IOException.class, () ->
                FileUtil.writeToFile(target, "replacement content", failingWriter(failure))));

        assertFalse(Files.exists(target));
        assertTrue(filesInDirectory().isEmpty());
    }

    @Test
    public void writeToFile_atomicReplacementUnsupported_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path target = directory.resolve("contacts.json");
        Files.writeString(target, "original saved data");
        FileUtil.FileOperations operations = new TestFileOperations() {
            @Override
            public void replace(Path source, Path destination) throws IOException {
                assertEquals("complete replacement", Files.readString(source));
                throw new AtomicMoveNotSupportedException(source.toString(), destination.toString(), "unsupported");
            }
        };

        assertThrows(AtomicMoveNotSupportedException.class, () ->
                FileUtil.writeToFile(target, "complete replacement", operations));

        assertEquals("original saved data", Files.readString(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_closeFails_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path target = directory.resolve("contacts.json");
        Files.writeString(target, "original saved data");
        IOException failure = new IOException("simulated close failure");
        FileUtil.FileOperations operations = new TestFileOperations() {
            @Override
            public OutputStream openOutputStream(Path file) throws IOException {
                return new FilterOutputStream(super.openOutputStream(file)) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        throw failure;
                    }
                };
            }
        };

        assertSame(failure, assertThrows(IOException.class, () ->
                FileUtil.writeToFile(target, "complete replacement", operations)));

        assertEquals("original saved data", Files.readString(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_cleanupFails_preservesPrimaryFailureAndOriginal() throws IOException {
        Path target = directory.resolve("contacts.json");
        Files.writeString(target, "original saved data");
        IOException failure = new IOException("simulated write failure");
        IOException cleanupFailure = new IOException("simulated cleanup failure");
        FileUtil.FileOperations operations = new TestFileOperations() {
            @Override
            public OutputStream openOutputStream(Path file) throws IOException {
                throw failure;
            }

            @Override
            public void deleteIfExists(Path file) throws IOException {
                super.deleteIfExists(file);
                throw cleanupFailure;
            }
        };

        IOException thrown = assertThrows(IOException.class, () ->
                FileUtil.writeToFile(target, "replacement", operations));

        assertSame(failure, thrown);
        assertArrayEquals(new Throwable[]{cleanupFailure}, thrown.getSuppressed());
        assertEquals("original saved data", Files.readString(target));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_directoryDestination_rejectsWithoutChangingIt() throws IOException {
        Path target = Files.createDirectory(directory.resolve("contacts.json"));
        Path child = Files.writeString(target.resolve("keep.txt"), "keep");

        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, "replacement"));

        assertEquals("keep", Files.readString(child));
        assertEquals(List.of(target), filesInDirectory());
    }

    @Test
    public void writeToFile_readOnlyDestination_preservesOriginalWithoutTemporaryFile() throws IOException {
        Path target = Files.writeString(directory.resolve("contacts.json"), "original saved data");
        assertTrue(target.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(target), "This account can still write read-only files");

            assertThrows(AccessDeniedException.class, () -> FileUtil.writeToFile(target, "replacement"));

            assertEquals("original saved data", Files.readString(target));
            assertEquals(List.of(target), filesInDirectory());
        } finally {
            assertTrue(target.toFile().setWritable(true));
        }
    }

    @Test
    public void writeToFile_symbolicLink_updatesTargetAndRetainsLink() throws IOException {
        Path target = Files.writeString(directory.resolve("saved.json"), "original saved data");
        Path link = directory.resolve("contacts.json");
        try {
            Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException e) {
            assumeTrue(false, "Symbolic links are unavailable: " + e.getMessage());
        }

        FileUtil.writeToFile(link, "replacement");

        assertTrue(Files.isSymbolicLink(link));
        assertEquals("replacement", Files.readString(target));
        assertEquals("replacement", Files.readString(link));
        assertEquals(2, filesInDirectory().size());
    }

    private FileUtil.FileOperations failingWriter(IOException failure) {
        return new TestFileOperations() {
            @Override
            public OutputStream openOutputStream(Path file) throws IOException {
                return new FilterOutputStream(super.openOutputStream(file)) {
                    @Override
                    public void write(byte[] data, int offset, int length) throws IOException {
                        out.write(data, offset, Math.min(length, 8));
                        assertTrue(Files.size(file) > 0);
                        throw failure;
                    }
                };
            }
        };
    }

    private List<Path> filesInDirectory() throws IOException {
        try (var files = Files.list(directory)) {
            return files.toList();
        }
    }

    private static class TestFileOperations implements FileUtil.FileOperations {
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
    }
}
