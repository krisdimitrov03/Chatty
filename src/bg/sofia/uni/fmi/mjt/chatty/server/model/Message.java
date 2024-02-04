package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record Message(User sender, String text) implements Entity {
}
