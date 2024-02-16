package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.NotificationRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.security.Guard;

import java.util.Collection;

public class NotificationService implements NotificationServiceAPI {

    private static NotificationServiceAPI instance;

    private NotificationService() {
    }

    public static NotificationServiceAPI getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }

        return instance;
    }

    @Override
    public Collection<Notification> getNotificationsOf(String username) {
        Guard.isNotNull(username);

        return NotificationRepository.getInstance()
                .get(n -> n.user().username().equals(username));
    }

    @Override
    public void removeNotificationsOf(String username) throws ValueNotFoundException {
        Guard.isNotNull(username);

        if (!getNotificationsOf(username).isEmpty()) {
            NotificationRepository.getInstance()
                    .remove(n -> n.user().username().equals(username));
        }
    }

    @Override
    public void addNotification(User user, NotificationType type, String content) {
        Guard.isNotNull(user);
        Guard.isNotNull(type);
        Guard.isNotNull(content);

        NotificationRepository.getInstance()
                .add(new Notification(user, type, content));
    }

}
