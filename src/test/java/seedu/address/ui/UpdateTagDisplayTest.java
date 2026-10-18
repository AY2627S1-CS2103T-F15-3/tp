package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UpdateTagDisplayTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux")
                || System.getenv("DISPLAY") != null, "JavaFX rendering requires a display");
        FutureTask<Void> started = new FutureTask<>(() -> null);
        try {
            Platform.startup(started);
        } catch (IllegalStateException e) {
            Platform.runLater(started);
        }
        started.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void refresh_reminderAppearsAfterAnniversaryAndDoesNotDuplicateCustomTag() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            TestClock clock = new TestClock();
            Person person = new PersonBuilder().withLastUpdated(LocalDate.of(2025, 10, 18))
                    .withTags("Friend", "update").build();
            PersonCard card = new PersonCard(person, 1, clock);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 500, 250);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/view/Extensions.css").toExternalForm());
            root.applyCss();
            root.layout();
            assertTrue(root.lookupAll(".system-tag").isEmpty());
            clock.date = clock.date.plusDays(1);
            card.refreshTags();
            card.refreshTags();
            root.applyCss();
            root.layout();
            assertEquals(1, root.lookupAll(".system-tag").size());
            Label system = (Label) root.lookup(".system-tag");
            assertEquals("update", system.getText());
            assertEquals(Color.web("#F4C542"), system.getBackground().getFills().get(0).getFill());
            assertEquals(Color.web("#222222"), system.getTextFill());
            List<Label> labels = root.lookupAll("#tags .label").stream().map(node -> (Label) node).toList();
            assertEquals(3, labels.size());
            Label custom = labels.stream().filter(label -> label.getText().equals("update (custom)"))
                    .findFirst().orElseThrow();
            assertEquals(Color.web("#3e7b91"), custom.getBackground().getFills().get(0).getFill());
            assertEquals(system.getPadding(), custom.getPadding());
            assertEquals(system.getFont().getSize(), custom.getFont().getSize());
            assertEquals(system.getBackground().getFills().get(0).getRadii(),
                    custom.getBackground().getFills().get(0).getRadii());
            assertEquals(2, person.getTags().size());
            assertFalse(person.getTags().stream().anyMatch(tag -> tag.tagName.contains("custom")));
            Person edited = new PersonBuilder(person).withPhone("91234567").withLastUpdated(clock.date).build();
            PersonCard editedCard = new PersonCard(edited, 1, clock);
            assertTrue(editedCard.getRoot().lookupAll(".system-tag").isEmpty());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }

    private static class TestClock extends Clock {
        private LocalDate date = LocalDate.of(2026, 10, 18);

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant(), zone);
        }

        @Override
        public Instant instant() {
            return date.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }
}
