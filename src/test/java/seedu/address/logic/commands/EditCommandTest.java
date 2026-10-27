package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_replaceTags_reportsActualChanges() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person tagged = new PersonBuilder(original).withTags("Friend", "Classmate").build();
        model.setPerson(original, tagged);
        String feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withTags("Project").build()).execute(model).getFeedbackToUser();
        assertTrue(feedback.endsWith("\nTags replaced. Removed: Classmate, Friend. Added: Project."));
        assertEquals(new PersonBuilder(tagged).withTags("Project").build(), model.getFilteredPersonList().get(0));

        feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withTags("Project", "Friend").build()).execute(model).getFeedbackToUser();
        assertTrue(feedback.endsWith("\nTags replaced. Removed: None. Added: Friend."));

        feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withTags().build()).execute(model).getFeedbackToUser();
        assertTrue(feedback.endsWith("\nTags replaced. Removed: Friend, Project. Added: None."));
        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
    }

    @Test
    public void execute_clearOptionalFields_listsEveryClearedField() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person complete = new PersonBuilder(original).withMajor("Computer Science").withGraduationYear("2027").build();
        model.setPerson(original, complete);
        String feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone("").withEmail("").withMajor("").withGraduationYear("").build())
                .execute(model).getFeedbackToUser();
        assertEquals("Edited Alice Pauline:\nPhone: 94351253 → Not specified"
                + "\nEmail: alice@example.com → Not specified\nMajor: Computer Science → Not specified"
                + "\nClass year: 2027 → Not specified", feedback);
    }

    @Test
    public void execute_mixedChangedAndUnchangedValues_listsOnlyActualChanges() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        String feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone(original.getPhone().value).withMajor("Mathematics").withTags("friends").build())
                .execute(model).getFeedbackToUser();
        assertEquals("Edited Alice Pauline:\nMajor: Not specified → Mathematics", feedback);
    }

    @Test
    public void execute_sameValues_keepsOldUpdateDate() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person old = new PersonBuilder(original).withLastUpdated(LocalDate.of(2025, 1, 1)).build();
        model.setPerson(original, old);
        Clock clock = Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"), ZoneOffset.UTC);
        String feedback = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder(old).build(), clock)
                .execute(model).getFeedbackToUser();
        assertEquals("No changes made to Alice Pauline. The supplied values match the existing details.", feedback);
        assertEquals(old, model.getFilteredPersonList().get(0));
        assertEquals(LocalDate.of(2025, 1, 1), model.getFilteredPersonList().get(0).getLastUpdated());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_caseOnlyDuplicateName_failure() {
        String otherName = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased())
                .getName().fullName.toUpperCase(java.util.Locale.ROOT);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(otherName).build());
        assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_changeOwnNameCapitalization_success() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String newName = original.getName().fullName.toUpperCase(java.util.Locale.ROOT);
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(newName).build()).execute(model);
        assertEquals(newName, model.getAddressBook().getPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = "Edited Amy Bee:"
                + "\nName: Alice Pauline → Amy Bee\nPhone: 94351253 → 85355255"
                + "\nEmail: alice@example.com → amy@gmail.com"
                + "\nTags replaced. Removed: friends. Added: None.";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = "Edited Bob Choo:\nName: George Best → Bob Choo\nPhone: 9482442 → 22222222"
                + "\nTags replaced. Removed: None. Added: husband.";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_academicFields_success() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person academicPerson = new PersonBuilder(original).withMajor("Mathematics")
                .withGraduationYear("2026").build();
        model.setPerson(original, academicPerson);

        // Editing one academic field preserves the other field and all contact details.
        Person updated = new PersonBuilder(academicPerson).withMajor("Computer Science").build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withMajor("Computer Science").build()).execute(model);
        assertEquals(updated, model.getFilteredPersonList().get(0));

        updated = new PersonBuilder(updated).withGraduationYear("2027").build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withGraduationYear("2027").build()).execute(model);
        assertEquals(updated, model.getFilteredPersonList().get(0));

        // Editing a contact field preserves both academic fields.
        updated = new PersonBuilder(updated).withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withPhone(VALID_PHONE_BOB).build()).execute(model);
        assertEquals(updated, model.getFilteredPersonList().get(0));

        updated = new PersonBuilder(updated).withMajor("").withGraduationYear("").build();
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder()
                .withMajor("").withGraduationYear("").build()).execute(model);
        assertEquals(updated, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_NO_CHANGES, editedPerson.getName());

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = "Edited Bob Choo:\nName: Alice Pauline → Bob Choo";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_successfulEdit_createsOneUndoableState() throws Exception {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(originalPerson).withPhone(VALID_PHONE_BOB).build();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);
        AddressBook editedAddressBook = new AddressBook(model.getAddressBook());

        assertTrue(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());

        assertTrue(model.undoAddressBook());
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(originalPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertFalse(model.canUndoAddressBook());

        assertTrue(model.redoAddressBook());
        assertEquals(editedAddressBook, model.getAddressBook());
        assertEquals(editedPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_multipleEdits_retainIndependentSnapshots() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withMajor = new PersonBuilder(originalPerson).withMajor("Mathematics").build();
        Person withMajorAndYear = new PersonBuilder(withMajor).withGraduationYear("2026").build();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withMajor("Mathematics").build()).execute(model);
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGraduationYear("2026").build()).execute(model);
        assertEquals(withMajorAndYear, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));

        assertTrue(model.undoAddressBook());
        assertEquals(withMajor, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertTrue(model.undoAddressBook());
        assertEquals(originalPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_editAfterAddAndUndo_invalidatesRedoHistory() throws Exception {
        Person newPerson = new PersonBuilder().withName("Alex Tan").build();
        new AddCommand(newPerson).execute(model);
        model.undoAddressBook();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);

        assertFalse(model.canRedoAddressBook());
        assertFalse(model.hasPerson(newPerson));
        assertTrue(model.canUndoAddressBook());
    }

    @Test
    public void execute_editFilteredList_undoRestoresOriginalPerson() throws Exception {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build()).execute(model);

        assertTrue(model.undoAddressBook());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    @Test
    public void execute_invalidPersonIndex_doesNotCreateHistoryEntry() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_duplicatePerson_doesNotCreateHistoryEntry() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder(firstPerson).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_noFieldSpecified_doesNotCreateHistoryEntry() throws Exception {
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor()).execute(model);

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_sameValues_doesNotCreateHistoryEntry() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(firstPerson).build());
        String expectedMessage = String.format(EditCommand.MESSAGE_NO_CHANGES, firstPerson.getName());

        // No-op feedback must not imply that a change was recorded.
        assertEquals(expectedMessage, editCommand.execute(model).getFeedbackToUser());
        assertEquals(firstPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_sameValuesAfterEdit_keepsExistingHistoryOnly() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()).execute(model);

        assertTrue(model.undoAddressBook());
        assertEquals(originalPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
