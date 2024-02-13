package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record Notification(User user, NotificationType type, String content) implements Entity {
}
