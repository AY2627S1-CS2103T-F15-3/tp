package seedu.address.ui;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.Parent;

/** Shared JavaFX startup, thread handling and layout helpers for contact presentation tests. */
final class FxTestUtil {
    private static final int TIMEOUT_SECONDS = 45;
    private static final int LAYOUT_PASSES = 5;

    private FxTestUtil() {
    }

    static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX rendering requires a display");
        FutureTask<Void> started = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        try {
            Platform.startup(started);
        } catch (IllegalStateException e) {
            Platform.runLater(started);
        }
        started.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static void layout(Parent root) {
        // Bindings and virtualized cells may request another layout while a resize is being applied.
        for (int pass = 0; pass < LAYOUT_PASSES; pass++) {
            root.applyCss();
            root.layout();
        }
    }
}
