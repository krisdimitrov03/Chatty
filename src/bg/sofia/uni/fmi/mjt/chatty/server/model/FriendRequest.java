package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record FriendRequest(User sender, User receiver) implements Entity {
}
