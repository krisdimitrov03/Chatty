package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.BlockRepository;
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
    public void block(User blocker, String blockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(blocker);
        Guard.isNotNull(blockedUsername);

        User blockerUser = UserService.getInstance().ensureUserExists(blocker);
        User blockedUser = UserService.getInstance().ensureUserExists(blockedUsername);

        FriendshipService.getInstance().removeFriend(blocker, blockedUsername);

        ChatService.getInstance().deletePersonalChat(blockerUser.username(), blockedUsername);

        BlockRepository.getInstance().add(new Block(blockerUser, blockedUser));
    }

    @Override
    public void unblock(User unblocker, String unblockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(unblocker);
        Guard.isNotNull(unblockedUsername);

        User unblockerUser = UserService.getInstance().ensureUserExists(unblocker);
        User unblockedUser = UserService.getInstance().ensureUserExists(unblockedUsername);

        BlockRepository.getInstance()
            .remove(b -> b.blocker().equals(unblockerUser) && b.blocked().equals(unblockedUser));
    }

    @Override
    public boolean checkBlock(User blocker, User blocked) {
        return BlockRepository.getInstance()
            .contains(b -> b.blocker().equals(blocker) && b.blocked().equals(blocked));
    }

}
