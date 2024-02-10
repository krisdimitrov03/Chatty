package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class Friendship implements Entity {

    private final Collection<User> users;

    public Friendship(User left, User right) {
        users = Set.of(left, right);
    }

    public boolean containsUser(User user) {
        return users.stream().anyMatch(u -> u.equals(user));
    }

    public Optional<User> getFriendOf(User user) {
        if (!containsUser(user)) {
            return Optional.empty();
        }

        return users.stream().filter(u -> !u.equals(user)).findFirst();
    }

}