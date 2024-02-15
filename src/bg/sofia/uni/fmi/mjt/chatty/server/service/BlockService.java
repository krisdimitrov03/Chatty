package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.BlockRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.NotificationRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

public class BlockService implements BlockServiceAPI {

    private static BlockServiceAPI instance;

    private BlockService() {
    }

    public static BlockServiceAPI getInstance() {
        if (instance == null) {
            instance = new BlockService();
        }

        return instance;
    }

    @Override
    public void block(String blocker, String blocked) throws ValueNotFoundException {
        Guard.isNotNull(blocker);
        Guard.isNotNull(blocked);

        User blockerUser = UserService.getInstance().ensureUserExists(blocker);
        User blockedUser = UserService.getInstance().ensureUserExists(blocked);

        FriendshipService.getInstance().removeFriend(blocker, blocked);

        ChatService.getInstance().deletePersonalChat(blockerUser.username(), blocked);

        BlockRepository.getInstance().add(new Block(blockerUser, blockedUser));

        String notificationContent = blockerUser.getFullName() + " blocked you";
        NotificationService.getInstance()
                .addNotification(blockedUser, NotificationType.OTHER, notificationContent);
    }

    @Override
    public void unblock(String unblocker, String unblocked) throws ValueNotFoundException {
        Guard.isNotNull(unblocker);
        Guard.isNotNull(unblocked);

        User unblockerUser = UserService.getInstance().ensureUserExists(unblocker);
        User unblockedUser = UserService.getInstance().ensureUserExists(unblocked);

        BlockRepository.getInstance()
                .remove(b -> b.blocker().equals(unblockerUser) && b.blocked().equals(unblockedUser));

        String notificationContent = unblockerUser.getFullName() + " unblocked you";
        NotificationService.getInstance()
                .addNotification(unblockedUser, NotificationType.OTHER, notificationContent);
    }

    @Override
    public boolean checkBlock(User blocker, User blocked) {
        return BlockRepository.getInstance()
                .contains(b -> b.blocker().equals(blocker) && b.blocked().equals(blocked));
    }

}
