package bg.sofia.uni.fmi.mjt.chatty.dto;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;

import java.util.Collection;
import java.util.Optional;

public record Session(Optional<UserDTO> user, Collection<Notification> notifications) {
}
