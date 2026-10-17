package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ContactFilterPredicateTest {
    private final Person computing = new PersonBuilder().withMajor("Computer Science")
            .withGraduationYear("2027").build();

    @Test
    public void constructor_noCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ContactFilterPredicate(
                Optional.empty(), Optional.empty()));
    }

    @Test
    public void constructor_nullCriteria_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(null, Optional.of(
                new GraduationYear("2027"))));
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(Optional.of(
                new Major("Computing")), null));
    }

    @Test
    public void test_majorOnly_matchesCompleteValueIgnoringCase() {
        assertTrue(predicate("computer science", null).test(computing));
        assertFalse(predicate("Computer", null).test(computing));
        assertFalse(predicate("Science", null).test(computing));
        Person punctuation = new PersonBuilder().withMajor("Philosophy, Politics & Economics").build();
        assertTrue(predicate("philosophy, politics & economics", null).test(punctuation));
    }

    @Test
    public void test_yearOnly_matchesExactly() {
        assertTrue(predicate(null, "2027").test(computing));
        assertFalse(predicate(null, "2028").test(computing));
        for (String year : new String[]{"2000", "2099"}) {
            assertTrue(predicate(null, year).test(new PersonBuilder().withGraduationYear(year).build()));
        }
    }

    @Test
    public void test_multipleCriteria_requiresEveryCriterion() {
        assertTrue(predicate("Computer Science", "2027").test(computing));
        assertFalse(predicate("Mathematics", "2027").test(computing));
        assertFalse(predicate("Computer Science", "2028").test(computing));
    }

    @Test
    public void test_missingField_doesNotMatchSuppliedCriterion() {
        Person unspecified = new PersonBuilder().withMajor("").withGraduationYear("").build();
        assertFalse(predicate("Computer Science", null).test(unspecified));
        assertFalse(predicate(null, "2027").test(unspecified));
        assertTrue(predicate("Computer Science", null).test(new PersonBuilder(computing)
                .withGraduationYear("").build()));
        assertTrue(predicate(null, "2027").test(new PersonBuilder(computing).withMajor("").build()));
    }

    @Test
    public void test_otherFields_doNotMatchMajorCriterion() {
        Person otherFields = new PersonBuilder().withName("Computer Science").withMajor("Mathematics")
                .withAddress("Computer Science").withTags("Computer").build();
        assertFalse(predicate("Computer Science", null).test(otherFields));
    }

    @Test
    public void equals_comparesAllCriteria() {
        ContactFilterPredicate first = predicate("Computer Science", "2027");
        assertTrue(first.equals(first));
        assertTrue(first.equals(predicate("Computer Science", "2027")));
        assertFalse(first.equals(predicate("Mathematics", "2027")));
        assertFalse(first.equals(predicate("Computer Science", "2028")));
        assertFalse(first.equals(null));
        assertFalse(first.equals("Computer Science"));
    }

    @Test
    public void toStringMethod_containsCriteria() {
        ContactFilterPredicate predicate = predicate("Computer Science", "2027");
        assertEquals(ContactFilterPredicate.class.getCanonicalName()
                + "{major=Optional[Computer Science], graduationYear=Optional[2027]}", predicate.toString());
    }

    private ContactFilterPredicate predicate(String major, String year) {
        return new ContactFilterPredicate(Optional.ofNullable(major).map(Major::new),
                Optional.ofNullable(year).map(GraduationYear::new));
    }
}
