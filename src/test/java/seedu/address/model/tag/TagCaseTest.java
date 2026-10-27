package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TagCaseTest {

    @Test
    public void equals_differentCapitalization_sameTag() {
        assertEquals(new Tag("Friend"), new Tag("friend"));
        assertEquals(new Tag("Friend").hashCode(), new Tag("FRIEND").hashCode());
        assertNotEquals(new Tag("Friend"), new Tag("Friends"));
    }

    @Test
    public void set_differentCapitalization_keepsFirstSpelling() {
        Set<Tag> tags = new HashSet<>(List.of(new Tag("NOC")));
        tags.add(new Tag("noc"));
        assertEquals(1, tags.size());
        assertEquals("NOC", tags.iterator().next().tagName);
    }
}
