package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks first-time logger initialization in isolated JVMs without changing the test runner's logging. */
public class LogsCenterTest {
    @TempDir
    public Path directory;

    @Test
    public void startup_writableLogFile_logsToConsoleAndFile() throws Exception {
        String output = runStartupProbe();

        assertTrue(output.contains("console=true;file=true"), output);
        assertTrue(output.contains("Log message after startup."), output);
        assertTrue(Files.readString(directory.resolve("unimate.log.0")).contains("Log message after startup."));
    }

    @Test
    public void startup_logPathIsDirectory_keepsConsoleLoggingAvailable() throws Exception {
        Path blockedLog = Files.createDirectory(directory.resolve("unimate.log.0"));

        String output = runStartupProbe();

        assertTrue(output.contains("continuing with console logging only"), output);
        assertTrue(output.contains("console=true;file=false"), output);
        assertTrue(output.contains("Log message after startup."), output);
        assertTrue(Files.isDirectory(blockedLog));
    }

    private String runStartupProbe() throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        // Gradle's java.class.path may contain only its worker bootstrap, not the application and test classes.
        String classPath = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(StartupProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> command = new ArrayList<>();
        command.add(java.toString());
        // Child JVMs do not inherit the coverage agent; reuse it to append to Gradle's execution data.
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .map(LogsCenterTest::withAbsoluteCoverageDestination)
                .forEach(command::add);
        command.addAll(List.of("-cp", classPath, StartupProbe.class.getName()));
        Path outputFile = directory.resolve("startup-output.txt");
        Process process = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Logger startup probe timed out.");
            String output = Files.readString(outputFile);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private static String withAbsoluteCoverageDestination(String argument) {
        // The probe runs in a temporary directory, but must write to the original coverage file.
        return Pattern.compile("destfile=([^,]+)").matcher(argument)
                .replaceFirst(match -> Matcher.quoteReplacement(
                        "destfile=" + Path.of(match.group(1)).toAbsolutePath()));
    }

    /** Initializes logging in a fresh JVM, then reports the installed handlers and emits a message. */
    public static class StartupProbe {
        public static void main(String[] args) {
            Logger logger = LogsCenter.getLogger("StartupProbe");
            boolean console = Arrays.stream(logger.getParent().getHandlers())
                    .anyMatch(ConsoleHandler.class::isInstance);
            boolean file = Arrays.stream(logger.getParent().getHandlers())
                    .anyMatch(FileHandler.class::isInstance);
            System.out.println("console=" + console + ";file=" + file);
            logger.info("Log message after startup.");
        }
    }
}
