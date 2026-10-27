package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Covers every optional-field combination and valid long values without clipping or overlap. */
public class PersonCardEdgeCaseTest {
    private static final List<String> FIELDS = List.of("phone", "address", "email", "major", "graduationYear",
            "lastUpdated");

    @BeforeAll
    public static void startToolkit() throws Exception {
        FxTestUtil.startToolkit();
    }

    @TestFactory
    public Stream<DynamicTest> optionalFields_everyCombination_hasReadableValuesAndCorrectIcons() {
        List<String> names = List.of("phone", "email", "major", "year", "tags", "notes", "favourite");
        return IntStream.range(0, 128).mapToObj(mask -> DynamicTest.dynamicTest("Present fields: "
                + IntStream.range(0, names.size()).filter(bit -> present(mask, bit))
                        .mapToObj(names::get).toList(), () -> checkOptionalCombination(mask)));
    }

    private void checkOptionalCombination(int mask) throws Exception {
        Person person = new PersonBuilder().withPhone(present(mask, 0) ? "91234567" : "")
                .withEmail(present(mask, 1) ? "alice@example.com" : "")
                .withMajor(present(mask, 2) ? "Computer Science" : "")
                .withGraduationYear(present(mask, 3) ? "2027" : "")
                .withTags(present(mask, 4) ? new String[]{"Classmate"} : new String[0])
                .withNotes(present(mask, 5) ? "Follow up after class" : "")
                .withFavourite(present(mask, 6)).withLastUpdated(LocalDate.of(2024, 2, 29)).build();
        checkCard(person);
    }

    private static Stream<LayoutCase> longValues() {
        return Stream.of(
                new LayoutCase("unbroken name", new PersonBuilder().withName("A".repeat(300)).build()),
                new LayoutCase("multiword name", new PersonBuilder().withName("Long Contact ".repeat(30).strip())
                        .build()),
                new LayoutCase("long phone", new PersonBuilder().withPhone("9".repeat(300)).build()),
                new LayoutCase("long email", new PersonBuilder().withEmail("a".repeat(300) + "@example.com").build()),
                new LayoutCase("unbroken address", new PersonBuilder().withAddress("A".repeat(500)).build()),
                new LayoutCase("multiword address", new PersonBuilder().withAddress("Lower Kent Ridge Road ".repeat(30))
                        .build()),
                new LayoutCase("unbroken major", new PersonBuilder().withMajor("M".repeat(500)).build()),
                new LayoutCase("multiword major", new PersonBuilder().withMajor("Computer Science ".repeat(30))
                        .build()),
                new LayoutCase("long tag", new PersonBuilder().withTags("T".repeat(500)).build()),
                new LayoutCase("many tags", new PersonBuilder().withTags(IntStream.range(0, 25)
                        .mapToObj(index -> "Category" + index).toArray(String[]::new)).build()),
                new LayoutCase("maximum note", new PersonBuilder().withNotes("N".repeat(500)).build()),
                new LayoutCase("all long values", new PersonBuilder().withName("Long Contact ".repeat(20).strip())
                        .withPhone("9".repeat(300)).withEmail("a".repeat(300) + "@example.com")
                        .withAddress("Kent Ridge Road ".repeat(30)).withMajor("Computer Science ".repeat(20))
                        .withGraduationYear("2099").withTags("T".repeat(500), "Friend", "update", "incomplete")
                        .withNotes("N".repeat(500)).withFavourite(true).build()));
    }

    @TestFactory
    public Stream<DynamicTest> longValues_wrappingKeepsFieldsSeparateAndComplete() {
        return longValues().map(value -> DynamicTest.dynamicTest(value.description(), () -> checkCard(value.person())));
    }

    private static boolean present(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }

    private void checkCard(Person person) throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(person, 1);
            StackPane root = createScene(card);
            for (double width : List.of(1000.0, 450.0, 610.0, 740.0, 450.0)) {
                root.resize(width, 2000);
                FxTestUtil.layout(root);
                assertEquals(width, root.getWidth(), 0.1);
                expandValues(root);
                FxTestUtil.layout(root);
                assertDetails(root, card.getRoot(), width);
                assertIdentity(root, card.getRoot(), person);
                assertTags(root, card.getRoot());
            }
            return null;
        });
    }

    private StackPane createScene(PersonCard card) {
        card.getRoot().setMaxHeight(Region.USE_PREF_SIZE);
        StackPane root = new StackPane(card.getRoot());
        StackPane.setAlignment(card.getRoot(), Pos.TOP_LEFT);
        Scene scene = new Scene(root, 1000, 2000);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        return root;
    }

    private void expandValues(StackPane root) {
        // Full-value checks apply after expansion; collapsed values intentionally use ellipses.
        for (String id : Stream.concat(FIELDS.stream(), Stream.of("name")).toList()) {
            Label label = (Label) root.lookup("#" + id);
            if (!label.isWrapText() && label.isFocusTraversable()) {
                label.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER,
                        false, false, false, false));
            }
        }
    }

    private void assertDetails(StackPane root, Region card, double width) {
        List<Node> textNodes = new ArrayList<>();
        double bottom = sceneBounds(card).getMinY();
        for (String id : FIELDS) {
            Label value = (Label) root.lookup("#" + id);
            Label caption = (Label) root.lookup("#" + id + "Caption");
            assertCompleteText(value);
            assertWithinCard(value, card);
            assertWithinCard(caption, card);
            textNodes.add(value);
            textNodes.add(caption);
            bottom = Math.max(bottom, sceneBounds(value).getMaxY());
        }
        assertNoOverlaps(textNodes, width);
        assertTrue(sceneBounds(card).getMaxY() - bottom <= 30, "Excess space below contact details");
        for (String id : List.of("phone", "email", "major", "graduationYear")) {
            assertFalse(((Label) root.lookup("#" + id)).getText().isEmpty());
        }
    }

    private void assertNoOverlaps(List<Node> nodes, double width) {
        for (int first = 0; first < nodes.size(); first++) {
            Bounds a = sceneBounds(nodes.get(first));
            for (int second = first + 1; second < nodes.size(); second++) {
                Bounds b = sceneBounds(nodes.get(second));
                boolean overlaps = Math.min(a.getMaxX(), b.getMaxX()) - Math.max(a.getMinX(), b.getMinX()) > 0.5
                        && Math.min(a.getMaxY(), b.getMaxY()) - Math.max(a.getMinY(), b.getMinY()) > 0.5;
                assertFalse(overlaps, nodes.get(first).getId() + " overlaps "
                        + nodes.get(second).getId() + " at width " + width);
            }
        }
    }

    private void assertIdentity(StackPane root, Region card, Person person) {
        Label name = (Label) root.lookup("#name");
        assertCompleteText(name);
        assertWithinCard(name, card);
        assertTrue(sceneBounds(name).getMinY() >= sceneBounds(card).getMinY() + 8,
                "Name must stay below the card border");
        assertEquals(person.getName().fullName, name.getText());
        assertEquals(person.isFavourite(), root.lookup("#favouriteIcon").isVisible());
        assertEquals(person.isFavourite(), root.lookup("#favouriteIcon").isManaged());
        assertEquals(!person.getNotes().isEmpty(), root.lookup("#noteIcon").isVisible());
        assertEquals(!person.getNotes().isEmpty(), root.lookup("#noteIcon").isManaged());
    }

    private void assertTags(StackPane root, Region card) {
        double detailsTop = sceneBounds(root.lookup("#details")).getMinY();
        for (Node tag : root.lookup("#tags").lookupAll(".label")) {
            assertWithinCard(tag, card);
            assertTrue(sceneBounds(tag).getMaxY() <= detailsTop + 0.5, "Tags must not overlap contact details");
        }
    }

    private void assertCompleteText(Label label) {
        Text rendered = (Text) label.lookup(".text");
        assertTrue(label.getHeight() > 0, label.getId());
        assertEquals(label.getText(), rendered.getText(), label.getId() + " is truncated");
        assertTrue(sceneBounds(rendered).getMaxX() <= sceneBounds(label).getMaxX() + 0.5, label.getId());
        assertTrue(sceneBounds(rendered).getMaxY() <= sceneBounds(label).getMaxY() + 0.5, label.getId());
    }

    private void assertWithinCard(Node node, Region card) {
        Bounds bounds = sceneBounds(node);
        Bounds container = sceneBounds(card);
        assertTrue(bounds.getMinX() >= container.getMinX() - 0.5, node.getId());
        assertTrue(bounds.getMaxX() <= container.getMaxX() + 0.5, node.getId());
        assertTrue(bounds.getMinY() >= container.getMinY() - 0.5, node.getId());
        assertTrue(bounds.getMaxY() <= container.getMaxY() + 0.5,
                node.getId() + " bounds=" + bounds + " card=" + container);
    }

    private Bounds sceneBounds(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    private record LayoutCase(String description, Person person) { }
}
