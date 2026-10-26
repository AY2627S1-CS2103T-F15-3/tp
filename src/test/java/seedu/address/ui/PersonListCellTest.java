package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Checks cell reuse and sizing before the list has attached or laid out a cell. */
public class PersonListCellTest {
    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @Test
    public void updateItem_nullOrEmptyItem_clearsPreviousCardAndHeight() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Person person = new PersonBuilder().build();
            PersonListCell cell = new PersonListCell(new ListView<>(), new HashMap<>());
            cell.updateItem(person, false);
            assertNotNull(cell.getGraphic());

            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertEquals(0, cell.computePrefHeight(300));

            cell.updateItem(person, false);
            assertNotNull(cell.getGraphic());
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertEquals(0, cell.computePrefHeight(300));
            return null;
        });
    }

    @Test
    public void detachedCell_measuresAndExpandsWithoutAnAttachedList() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonListCell cell = new PersonListCell(new ListView<>(), new HashMap<>());
            cell.updateItem(new PersonBuilder().withAddress("Long address ".repeat(40).strip()).build(), false);
            assertNull(cell.getListView());
            assertEquals(0, cell.getWidth());
            double height = cell.computePrefHeight(-1);
            assertTrue(Double.isFinite(height));
            assertTrue(height > 0);

            Label address = (Label) cell.getGraphic().lookup("#address");
            assertFalse(address.isWrapText());
            address.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, true, null));
            assertTrue(address.isWrapText());
            assertEquals("Long address ".repeat(40).strip(), address.getText());
            return null;
        });
    }
}
