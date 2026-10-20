package seedu.address.ui;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.GraduationYear;
import seedu.address.model.person.Major;
import seedu.address.model.person.Person;
import seedu.address.model.tag.SystemTag;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;
    private final Clock clock;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label favouriteIcon;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label major;
    @FXML
    private Label graduationYear;
    @FXML
    private Label lastUpdated;
    @FXML
    private FlowPane tags;
    @FXML
    private Label noteIcon;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a card with a clock used to recalculate date-based reminders.
     */
    public PersonCard(Person person, int displayedIndex, Clock clock) {
        super(FXML);
        this.person = person;
        this.clock = java.util.Objects.requireNonNull(clock);
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        favouriteIcon.setVisible(person.isFavourite());
        favouriteIcon.setManaged(person.isFavourite());
        phone.setText(getValueOrFallback(person.getPhone().value));
        address.setText(person.getAddress().value);
        email.setText(getValueOrFallback(person.getEmail().value));
        setAcademicFieldTexts(person, major::setText, graduationYear::setText);
        lastUpdated.setText("Last updated: " + person.getLastUpdated());
        lastUpdated.setTooltip(new Tooltip("Date added or last change to name, phone, email, address, "
                + "major or class year. Tags, notes and favourites do not change this date. "
                + "For older contacts without a saved date, the loading date is used as a baseline, "
                + "not a known historical add or edit date."));
        boolean hasNotes = !person.getNotes().isEmpty();
        noteIcon.setVisible(hasNotes);
        noteIcon.setManaged(hasNotes);
        noteIcon.setTooltip(new Tooltip("Use shownote " + displayedIndex + " to view this note."));
        refreshTags();
    }

    /**
     * Rebuilds user tags and current system reminders without changing stored contact data.
     */
    public void refreshTags() {
        tags.getChildren().clear();
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    boolean conflictsWithReminder = SystemTag.isSystemLabel(tag.tagName);
                    Label label = new Label(conflictsWithReminder ? tag.tagName + " (custom)" : tag.tagName);
                    if (conflictsWithReminder) {
                        label.setTooltip(new Tooltip("Custom tag: " + tag.tagName + ". Use t/" + tag.tagName
                                + " to manage this tag."));
                    }
                    tags.getChildren().add(label);
                });
        for (SystemTag reminder : SystemTag.forPerson(person, LocalDate.now(clock))) {
            Label label = new Label(reminder.label);
            label.getStyleClass().add("system-tag");
            label.setTooltip(new Tooltip(reminder == SystemTag.INCOMPLETE
                    ? "Add a phone number or email address to complete this contact."
                    : "Basic profile last updated: " + person.getLastUpdated()));
            tags.getChildren().add(label);
        }
    }

    static void setAcademicFieldTexts(Person person, Consumer<String> majorTextSetter,
            Consumer<String> graduationYearTextSetter) {
        majorTextSetter.accept(formatMajor(person.getMajor()));
        graduationYearTextSetter.accept(formatGraduationYear(person.getGraduationYear()));
    }

    private static String formatMajor(Major major) {
        return "Major: " + getValueOrFallback(major.value);
    }

    private static String formatGraduationYear(GraduationYear graduationYear) {
        return "Class year: " + getValueOrFallback(graduationYear.value);
    }

    private static String getValueOrFallback(String value) {
        return value.isEmpty() ? "Not specified" : value;
    }
}
