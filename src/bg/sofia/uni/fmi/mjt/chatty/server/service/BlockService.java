package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.BlockRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.RepositoryAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

public class BlockService implements BlockServiceAPI {

    private final RepositoryAPI<Block> blockRepo;

    private final FriendshipServiceAPI friendshipService;

    private final UserServiceAPI userService;

    public BlockService() {
        blockRepo = BlockRepository.getInstance();
        userService = new UserService();
        friendshipService = new FriendshipService();
    }

    @Override
    public void block(User blocker, String blockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(blocker);
        Guard.isNotNull(blockedUsername);

        User blockerUser = userService.ensureUserExists(blocker);
        User blockedUser = userService.ensureUserExists(blockedUsername);

        friendshipService.removeFriend(blocker, blockedUsername);

        // TODO: remove personal chat

        blockRepo.add(new Block(blockerUser, blockedUser));
    }

    @Override
    public void unblock(User unblocker, String unblockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(unblocker);
        Guard.isNotNull(unblockedUsername);

        User unblockerUser = userService.ensureUserExists(unblocker);
        User unblockedUser = userService.ensureUserExists(unblockedUsername);

        blockRepo.remove(b -> b.blocker().equals(unblockerUser) && b.blocked().equals(unblockedUser));
    }

    @Override
    public boolean checkBlock(User blocker, User blocked) {
        return blockRepo.contains(b -> b.blocker().equals(blocker) && b.blocked().equals(blocked));
    }

}
