package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ContactPaneTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void detailGroups_resizingChangesColumnsBeforeCardHeightIsMeasured() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCardPane card = new PersonCardPane();
            VBox contact = new VBox(new Label("Phone"), new Label("Address"), new Label("Email"));
            VBox academic = new VBox(new Label("Major"), new Label("Class year"), new Label("Updated"));
            GridPane details = new GridPane();
            ColumnConstraints first = new ColumnConstraints();
            first.setPercentWidth(50);
            ColumnConstraints second = new ColumnConstraints();
            second.setPercentWidth(50);
            details.getColumnConstraints().addAll(first, second);
            details.add(contact, 0, 0);
            details.add(academic, 1, 0);
            details.setVgap(8);
            card.getChildren().addAll(new Label("Contact"), details);
            card.configureDetails(details, contact, academic);
            new Scene(card);
            card.applyCss();

            double wideHeight = card.prefHeight(900);
            assertEquals(wideHeight, card.prefHeight(560), 0.1);
            double narrowHeight = card.prefHeight(559);
            assertTrue(narrowHeight > wideHeight);
            assertEquals(1, GridPane.getRowIndex(academic));
            assertEquals(narrowHeight, card.prefHeight(400), 0.1);
            assertEquals(wideHeight, card.prefHeight(900), 0.1);
            assertEquals(0, GridPane.getRowIndex(academic));
            return null;
        });
    }

    @Test
    public void unconfiguredCard_measuresAndLaysOutChildrenWithoutExtraBottomSpace() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCardPane card = new PersonCardPane();
            card.setPadding(new Insets(4));
            card.setSpacing(6);
            Region first = new Region();
            first.setPrefHeight(20);
            Region second = new Region();
            second.setPrefHeight(30);
            card.getChildren().addAll(first, second);

            assertEquals(64, card.prefHeight(300), 0.1);
            assertEquals(64, card.minHeight(300), 0.1);
            card.resize(300, card.prefHeight(300));
            card.layout();

            assertEquals(4, first.getLayoutY(), 0.1);
            assertEquals(30, second.getLayoutY(), 0.1);
            assertEquals(4, card.getHeight() - second.getBoundsInParent().getMaxY(), 0.1);
            return null;
        });
    }

    @Test
    public void tags_expandedAndMultipleRows_stayWithinMeasuredHeightAfterResizing() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            TagPane tags = new TagPane();
            tags.setHgap(6);
            tags.setVgap(5);
            Label longTag = new Label("T".repeat(500));
            longTag.setWrapText(true);
            tags.getChildren().add(longTag);
            for (int index = 0; index < 25; index++) {
                tags.getChildren().add(new Label("Category" + index));
            }
            tags.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane root = new StackPane(tags);
            new Scene(root, 1000, 2000);
            for (double width : List.of(1000.0, 450.0, 740.0, 450.0)) {
                root.resize(width, 2000);
                FxTestUtil.layout(root);
                double measured = tags.prefHeight(tags.getWidth());
                assertEquals(measured, tags.getHeight(), 0.5);
                double bottom = 0;
                for (Node tag : tags.getChildren()) {
                    assertTrue(tag.getBoundsInParent().getMaxX() <= tags.getWidth() + 0.5);
                    assertTrue(tag.getBoundsInParent().getMaxY() <= measured + 0.5);
                    bottom = Math.max(bottom, tag.getBoundsInParent().getMaxY());
                }
                assertEquals(measured, bottom, 0.5);
            }
            return null;
        });
    }
}
