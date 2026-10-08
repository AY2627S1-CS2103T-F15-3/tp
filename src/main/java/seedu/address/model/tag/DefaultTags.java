package seedu.address.model.tag;

import java.util.List;

/**
 * Contains the default tags in their fixed, one-based selection order.
 */
public final class DefaultTags {
    public static final List<Tag> TAGS = List.of(
            new Tag("Friend"), new Tag("Classmate"), new Tag("Teammate"),
            new Tag("Tutor"), new Tag("Prof"));

    private DefaultTags() {}
}
