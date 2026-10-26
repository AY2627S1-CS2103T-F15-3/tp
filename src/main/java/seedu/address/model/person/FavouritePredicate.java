package seedu.address.model.person;

/**
 * Selects favourite contacts and describes that view.
 */
public class FavouritePredicate implements PersonFilter {
    @Override
    public boolean test(Person person) {
        return person.isFavourite();
    }

    @Override
    public String getDescription() {
        return "Favourites";
    }
}
