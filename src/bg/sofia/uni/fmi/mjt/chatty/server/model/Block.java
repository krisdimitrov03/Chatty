package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record Block(User blocker, User blocked) implements Entity {
}
