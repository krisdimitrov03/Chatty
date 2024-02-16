package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.NotificationRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.Repository;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotificationServiceTest {

    private static Repository<Notification> repo;

    private static NotificationServiceAPI service;

    @BeforeAll
    static void setupTests() {
        repo = NotificationRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        service = NotificationService.getInstance();
    }

    @AfterEach
    void clearRepo() {
        try {
            repo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
    }

    @Test
    void testAddNotificationForCorrectResult() {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));

        Notification notification = new Notification(user, NotificationType.PERSONAL_MESSAGE,
                "[s.patrick] Hello, my friend!");

        service.addNotification(user, NotificationType.PERSONAL_MESSAGE, "[s.patrick] Hello, my friend!");

        assertEquals(Set.of(notification), new HashSet<>(service.getNotificationsOf(username)),
                "Add should create new notification for the selected user");
    }

    @Test
    void testGetNotificationsOfForCorrectResult() {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));

        Notification firstNotification = new Notification(user, NotificationType.PERSONAL_MESSAGE,
                "[s.patrick] Hello, my friend!");
        Notification secondNotification = new Notification(user, NotificationType.PERSONAL_MESSAGE,
                "[s.patrick] How are you?");

        service.addNotification(user, NotificationType.PERSONAL_MESSAGE, "[s.patrick] Hello, my friend!");
        service.addNotification(user, NotificationType.PERSONAL_MESSAGE, "[s.patrick] How are you?");

        var expected = Set.of(firstNotification, secondNotification);

        assertEquals(expected, new HashSet<>(service.getNotificationsOf(username)),
                "Add should create new notification for the selected user");
    }

    @Test
    void testRemoveNotificationsForCorrectResult() throws ValueNotFoundException {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));

        service.addNotification(user, NotificationType.PERSONAL_MESSAGE, "[s.patrick] Hello, my friend!");
        service.addNotification(user, NotificationType.PERSONAL_MESSAGE, "[s.patrick] How are you?");

        service.removeNotificationsOf(username);

        assertEquals(Collections.EMPTY_SET, new HashSet<>(service.getNotificationsOf(username)));
    }

}
