package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;

import java.io.InputStream;

public class NotificationRepository extends Repository<Notification> {

    private static final String DB_PATH = "notifications.dat";

    private static NotificationRepository instance;

    private NotificationRepository(String path) {
        super(path);
    }

    private NotificationRepository(InputStream stream) {
        super(stream);
    }

    public static NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository(DB_PATH);
        }

        return instance;
    }

    public static NotificationRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new NotificationRepository(stream);
        }

        return instance;
    }

}
