package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.tag.SystemTag;
import seedu.address.model.tag.Tag;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final int MAX_DISPLAYED_TAG_CHARACTERS = 20;

    private static final String LAST_UPDATED_HELP = "Date added or last change to name, phone, email, address, "
            + "major or class year. Tags, notes and favourites do not change this date. "
            + "For older contacts without a saved date, the loading date is used as a baseline, "
            + "not a known historical add or edit date.";

    public final Person person;
    private final Clock clock;
    private final Set<String> expandedFields;
    private Runnable onContentChanged = () -> { };

    @FXML
    private PersonCardPane cardPane;
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
    private TagPane tags;
    @FXML
    private GridPane details;
    @FXML
    private VBox contactDetails;
    @FXML
    private VBox academicDetails;
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
        this(person, displayedIndex, clock, new HashSet<>());
    }

    PersonCard(Person person, int displayedIndex, Clock clock, Set<String> expandedFields) {
        super(FXML);
        this.person = requireNonNull(person);
        this.clock = requireNonNull(clock);
        this.expandedFields = requireNonNull(expandedFields);
        populateDetails(displayedIndex);
        configureIndicators(displayedIndex);
        cardPane.configureDetails(details, contactDetails, academicDetails);
        configureExpandableValues();
        refreshTags();
    }

    void setOnContentChanged(Runnable handler) {
        onContentChanged = requireNonNull(handler);
    }

    private void populateDetails(int displayedIndex) {
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(getValueOrFallback(person.getPhone().value));
        address.setText(person.getAddress().value);
        email.setText(getValueOrFallback(person.getEmail().value));
        setAcademicFieldTexts(person, major::setText, graduationYear::setText);
        lastUpdated.setText(person.getLastUpdated() == null
                ? "Unknown" : person.getLastUpdated().toString());
        lastUpdated.setTooltip(new Tooltip(
                "Date added or last change to name, phone, email, address, "
                + "major or class year. Tags, notes and favourites do not change this date. "
                + "Older contacts without a saved date show Unknown and sort last in both date directions."));
    }

    private void configureIndicators(int displayedIndex) {
        favouriteIcon.setVisible(person.isFavourite());
        favouriteIcon.setManaged(person.isFavourite());
      boolean hasNotes = !person.getNotes().isEmpty();
        noteIcon.setVisible(hasNotes);
        noteIcon.setManaged(hasNotes);
        noteIcon.setTooltip(new Tooltip("Use shownote " + displayedIndex + " to view this note."));
    }

    private void configureExpandableValues() {
        for (Label value : List.of(name, phone, address, email, major, graduationYear, lastUpdated)) {
            new ExpandableText(value, value.getId(), value.getText(), value.getText(),
                    expandedFields, this::requestContentLayout);
        }
    }

    private void requestContentLayout() {
        cardPane.requestLayout();
        onContentChanged.run();
    }

    /**
     * Rebuilds user tags and current system reminders without changing stored contact data.
     */
    public void refreshTags() {
        tags.getChildren().clear();
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(this::createTagLabel)
                .forEach(tags.getChildren()::add);
        for (SystemTag reminder : SystemTag.forPerson(person, LocalDate.now(clock))) {
            Label label = new Label(reminder.label);
            label.getStyleClass().add("system-tag");
            label.setTooltip(new Tooltip(reminder == SystemTag.INCOMPLETE
                    ? "Add a phone number or email address to complete this contact."
                    : "Basic profile last updated: " + person.getLastUpdated()));
            tags.getChildren().add(label);
        }
    }

    private Label createTagLabel(Tag tag) {
        boolean conflictsWithReminder = SystemTag.isSystemLabel(tag.tagName);
        String suffix = conflictsWithReminder ? " (custom)" : "";
        Label label = new Label(formatTagName(tag.tagName) + suffix);
        label.setMinWidth(0);
        Tooltip tooltip = new Tooltip(conflictsWithReminder
                ? "Custom tag: " + tag.tagName + ". Use t/" + tag.tagName + " to manage this tag."
                : tag.tagName);
        installTagTooltip(label, tooltip);
        new ExpandableText(label, "tag:" + tag.tagName, tag.tagName + suffix, label.getText(),
                expandedFields, this::requestContentLayout);
        return label;
    }

    /**
     * Abbreviates only the displayed tag name; the model and tooltip retain the original value.
     */
    private static String formatTagName(String name) {
        return name.length() > MAX_DISPLAYED_TAG_CHARACTERS
                ? name.substring(0, MAX_DISPLAYED_TAG_CHARACTERS) + "..." : name;
    }

    /**
     * Shows the complete tag immediately on hover and hides it when the pointer leaves.
     */
    private static void installTagTooltip(Label label, Tooltip tooltip) {
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(400);
        label.setMouseTransparent(false);
        label.setPickOnBounds(true);
        label.setTooltip(tooltip);
        // Keep the accessible tooltip property, but replace its delayed mouse behaviour.
        Tooltip.uninstall(label, tooltip);
        label.setOnMouseEntered(event -> tooltip.show(label, event.getScreenX() + 10, event.getScreenY() + 10));
        label.setOnMouseExited(event -> tooltip.hide());
        label.setOnMousePressed(event -> tooltip.hide());
    }

    static void setAcademicFieldTexts(Person person, Consumer<String> majorTextSetter,
            Consumer<String> graduationYearTextSetter) {
        majorTextSetter.accept(getValueOrFallback(person.getMajor().value));
        graduationYearTextSetter.accept(getValueOrFallback(person.getGraduationYear().value));
    }

    private static String getValueOrFallback(String value) {
        return value.isEmpty() ? "Not specified" : value;
    }
}
