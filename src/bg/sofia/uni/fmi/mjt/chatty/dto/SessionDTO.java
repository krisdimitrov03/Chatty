package bg.sofia.uni.fmi.mjt.chatty.dto;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;

import java.util.Collection;

public record SessionDTO(UserDTO user, Collection<Notification> notifications) {
}
