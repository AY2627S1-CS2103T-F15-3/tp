package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ContactFilterPredicateTest {
    private final Person computing = new PersonBuilder().withMajor("Computer Science")
            .withGraduationYear("2027").build();

    @Test
    public void constructor_noCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ContactFilterPredicate(
                Optional.empty(), Optional.empty(), Optional.empty()));
    }

    @Test
    public void constructor_nullCriteria_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(null, Optional.of(
                new GraduationYear("2027")), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(Optional.of(
                new Major("Computing")), null, Optional.empty()));
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
                + "{major=Optional[Computer Science], graduationYear=Optional[2027], tag=Optional.empty, "
                + "favourite=Optional.empty}",
                predicate.toString());
    }

    @Test
    public void test_tagOnly_matchesCompleteTagIgnoringCase() {
        ContactFilterPredicate predicate = new ContactFilterPredicate(Optional.empty(), Optional.empty(),
                Optional.of(new Tag("classmate")));
        assertTrue(predicate.test(new PersonBuilder().withTags("Classmate", "NOC").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("Classmates").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags().build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Classmate").withTags("Friend").build()));
    }

    @Test
    public void test_academicAndTagCriteria_requiresAllThree() {
        ContactFilterPredicate predicate = new ContactFilterPredicate(Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.of(new Tag("Classmate")));
        Person matching = new PersonBuilder(computing).withTags("Classmate", "NOC").build();
        assertTrue(predicate.test(matching));
        assertFalse(predicate.test(new PersonBuilder(matching).withMajor("Mathematics").build()));
        assertFalse(predicate.test(new PersonBuilder(matching).withGraduationYear("2028").build()));
        assertFalse(predicate.test(new PersonBuilder(matching).withTags("Friend").build()));
        assertFalse(predicate.equals(new ContactFilterPredicate(Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.of(new Tag("Friend")))));
    }

    @Test
    public void constructor_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(Optional.of(new Major("Computing")),
                Optional.empty(), null));
    }

    @Test
    public void test_favouriteOnly_matchesRequestedStatus() {
        for (boolean favourite : new boolean[]{true, false}) {
            ContactFilterPredicate predicate = new ContactFilterPredicate(Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(favourite));
            assertTrue(predicate.test(new PersonBuilder(computing).withFavourite(favourite).build()));
            assertFalse(predicate.test(new PersonBuilder(computing).withFavourite(!favourite).build()));
        }
        assertTrue(predicate("Computer Science", null).test(
                new PersonBuilder(computing).withFavourite(true).build()));
        assertTrue(predicate("Computer Science", null).test(
                new PersonBuilder(computing).withFavourite(false).build()));
    }

    @Test
    public void test_favouriteWithAcademicAndTagCriteria_requiresEveryCriterion() {
        ContactFilterPredicate predicate = new ContactFilterPredicate(Optional.of(new Major("Computer Science")),
                Optional.of(new GraduationYear("2027")), Optional.of(new Tag("Classmate")), Optional.of(true));
        Person matching = new PersonBuilder(computing).withFavourite(true).withTags("Classmate").build();
        assertTrue(predicate.test(matching));
        assertFalse(predicate.test(new PersonBuilder(matching).withFavourite(false).build()));
        assertFalse(predicate.test(new PersonBuilder(matching).withMajor("Mathematics").build()));
        assertFalse(predicate.test(new PersonBuilder(matching).withGraduationYear("2028").build()));
        assertFalse(predicate.test(new PersonBuilder(matching).withTags("Friend").build()));
    }

    @Test
    public void constructor_favouriteCriteria_rejectsNullAndNoCriteria() {
        assertThrows(NullPointerException.class, () -> new ContactFilterPredicate(Optional.empty(),
                Optional.empty(), Optional.empty(), null));
        assertThrows(IllegalArgumentException.class, () -> new ContactFilterPredicate(Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()));
    }

    @Test
    public void equals_favouriteCriteria_distinguishesTrueFalseAndOmitted() {
        ContactFilterPredicate favourite = new ContactFilterPredicate(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(true));
        assertEquals(favourite, new ContactFilterPredicate(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(true)));
        assertFalse(favourite.equals(new ContactFilterPredicate(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(false))));
        assertFalse(predicate("Computer Science", null).equals(new ContactFilterPredicate(
                Optional.of(new Major("Computer Science")), Optional.empty(), Optional.empty(), Optional.of(false))));
    }

    private ContactFilterPredicate predicate(String major, String year) {
        return new ContactFilterPredicate(Optional.ofNullable(major).map(Major::new),
                Optional.ofNullable(year).map(GraduationYear::new), Optional.empty());
    }
}
