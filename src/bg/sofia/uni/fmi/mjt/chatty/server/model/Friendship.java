package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.util.Optional;

public record Friendship(User left, User right) implements Entity {

    public boolean isUserInside(String username) {
        return left.username().equals(username) || right.username().equals(username);
    }

    public Optional<User> getFriendOf(String username) {
        if (!isUserInside(username)) {
            return Optional.empty();
        }

        return left.username().equals(username) ? Optional.of(right()) : Optional.of(left());
    }

}
