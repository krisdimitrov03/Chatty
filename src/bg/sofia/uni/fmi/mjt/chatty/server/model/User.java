package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record User(String username, String passwordHash) implements Entity {
}
