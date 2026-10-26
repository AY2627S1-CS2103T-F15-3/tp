package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ExpandableTextTest {
    private static final String LONG_VALUE = "Long contact detail ".repeat(20).strip();

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void longValue_clickExpandsAndKeyboardCollapsesWithoutLosingText() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Label label = new Label();
            Set<String> state = new HashSet<>();
            AtomicInteger changes = new AtomicInteger();
            StackPane root = createLabel(label, LONG_VALUE, LONG_VALUE, state, changes);
            assertTrue(((Text) label.lookup(".text")).getText().endsWith("..."));
            assertFalse(label.isWrapText());

            click(label, MouseButton.PRIMARY);
            FxTestUtil.layout(root);
            assertTrue(label.isWrapText());
            assertTrue(state.contains("field"));
            assertEquals(LONG_VALUE, ((Text) label.lookup(".text")).getText());

            press(label, KeyCode.ENTER);
            FxTestUtil.layout(root);
            assertFalse(label.isWrapText());
            assertFalse(state.contains("field"));
            assertEquals(LONG_VALUE, label.getText());
            assertEquals(2, changes.get());
            return null;
        });
    }

    @Test
    public void shortenedTag_spaceTogglesFullTextAndRetainsSharedState() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Set<String> state = new HashSet<>();
            Label label = new Label();
            StackPane root = createLabel(label, LONG_VALUE, "Long...", state, new AtomicInteger());
            press(label, KeyCode.SPACE);
            FxTestUtil.layout(root);
            assertEquals(LONG_VALUE, label.getText());

            Label recreated = new Label();
            createLabel(recreated, LONG_VALUE, "Long...", state, new AtomicInteger());
            assertTrue(recreated.isWrapText());
            assertEquals(LONG_VALUE, recreated.getText());
            press(recreated, KeyCode.SPACE);
            assertEquals("Long...", recreated.getText());
            assertTrue(state.isEmpty());
            return null;
        });
    }

    @Test
    public void valueThatFits_doesNotBecomeAnExpansionControl() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Label label = new Label();
            AtomicInteger changes = new AtomicInteger();
            createLabel(label, "Short", "Short", new HashSet<>(), changes);
            assertFalse(label.isFocusTraversable());
            click(label, MouseButton.PRIMARY);
            press(label, KeyCode.ENTER);
            assertFalse(label.isWrapText());
            assertEquals(0, changes.get());
            return null;
        });
    }

    @Test
    public void otherMouseButtonsAndKeys_doNotChangeExpansion() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Label label = new Label();
            AtomicInteger changes = new AtomicInteger();
            createLabel(label, LONG_VALUE, LONG_VALUE, new HashSet<>(), changes);
            click(label, MouseButton.SECONDARY);
            click(label, MouseButton.PRIMARY, 2);
            press(label, KeyCode.ESCAPE);
            assertFalse(label.isWrapText());
            assertEquals(0, changes.get());
            return null;
        });
    }

    private StackPane createLabel(Label label, String fullText, String collapsedText, Set<String> state,
            AtomicInteger changes) {
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        StackPane root = new StackPane(label);
        new Scene(root, 180, 1000);
        new ExpandableText(label, "field", fullText, collapsedText, state, () -> {
            root.requestLayout();
            changes.incrementAndGet();
        });
        FxTestUtil.layout(root);
        return root;
    }

    private void press(Label label, KeyCode key) {
        label.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
    }

    private void click(Label label, MouseButton button) {
        click(label, button, 1);
    }

    private void click(Label label, MouseButton button, int clickCount) {
        label.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, clickCount,
                false, false, false, false, false, false, false, false, false, true, null));
    }
}
