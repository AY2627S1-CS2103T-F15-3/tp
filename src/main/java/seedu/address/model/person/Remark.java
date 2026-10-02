package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents an optional remark about a person. Immutable; an empty value means no remark.
 */
public class Remark {
    public final String value;

    /**
     * Constructs a {@code Remark} with no restrictions on its contents.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof Remark otherRemark && value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
