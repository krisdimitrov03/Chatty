package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;

public interface NotificationServiceAPI {

    Collection<Notification> getNotificationsOf(String username);

    void removeNotificationsOf(String username) throws ValueNotFoundException;

    void addNotification(User user, NotificationType type, String content);

}
