package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;

public class NotificationRepository extends Repository<Notification> {

    private static final String DB_PATH = "notifications.dat";

    private static NotificationRepository instance;

    private NotificationRepository(String path) {
        super(path);
    }

    public static NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository(DB_PATH);
        }

        return instance;
    }

}
