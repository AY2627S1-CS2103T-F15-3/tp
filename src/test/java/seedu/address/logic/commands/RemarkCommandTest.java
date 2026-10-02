package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addAndReplaceRemark_preservesOtherDetails() {
        assertRemarkSuccess("Met at orientation");
        assertRemarkSuccess("Project teammate");
    }

    @Test
    public void execute_clearRemark_success() {
        assertRemarkSuccess("Existing note");
        assertRemarkSuccess("");
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess("Found contact");
    }

    @Test
    public void execute_outOfBoundsIndex_doesNotModifyModel() {
        Index index = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(index, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_editContact_preservesRemark() throws Exception {
        assertRemarkSuccess("Keep this note");
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withPhone("91234567").build())
                .execute(model);
        assertEquals(new Remark("Keep this note"), model.getFilteredPersonList().getFirst().getRemark());
    }

    private void assertRemarkSuccess(String value) {
        Person original = model.getFilteredPersonList().getFirst();
        Person edited = new PersonBuilder(original).withRemark(value).build();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setPerson(original, edited);
        String template = value.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(value)), model,
                String.format(template, Messages.format(edited)), expected);
    }
}
