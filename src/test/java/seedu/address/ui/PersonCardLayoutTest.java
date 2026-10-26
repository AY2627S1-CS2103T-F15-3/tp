package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import seedu.address.testutil.PersonBuilder;

/** Checks readable field labels and complete contact details at different window widths. */
public class PersonCardLayoutTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void card_longDetails_remainWithinCardAndLabelsRemainDistinct() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withFavourite(true)
                    .withAddress("National University of Singapore, 21 Lower Kent Ridge Road, Singapore")
                    .withMajor("Computer Science and Information Systems").build(), 1);
            StackPane root = new StackPane(card.getRoot());
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            for (double width : List.of(1000.0, 450.0, 740.0)) {
                root.resize(width, 600);
                FxTestUtil.layout(root);
                for (String id : List.of("phone", "address", "email", "major", "graduationYear", "lastUpdated")) {
                    Label value = (Label) root.lookup("#" + id);
                    Label caption = (Label) root.lookup("#" + id + "Caption");
                    assertTrue(value.isVisible());
                    assertTrue(value.getHeight() > 0);
                    assertTrue(caption.getText().endsWith(":"));
                    assertNotEquals(caption.getTextFill(), value.getTextFill());
                    assertTrue(value.localToScene(value.getBoundsInLocal()).getMaxX() <= width + 0.5, id);
                    assertTrue(caption.localToScene(caption.getBoundsInLocal()).getMaxX()
                            <= value.localToScene(value.getBoundsInLocal()).getMinX(), id);
                }
                assertEquals("Phone:", ((Label) root.lookup("#phoneCaption")).getText());
                assertEquals("Address:", ((Label) root.lookup("#addressCaption")).getText());
                assertEquals("Email:", ((Label) root.lookup("#emailCaption")).getText());
            }
            return null;
        });
    }
}
