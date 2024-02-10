package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;
import bg.sofia.uni.fmi.mjt.chatty.server.model.FriendRequest;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Friendship;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.BlockRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendRequestRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendshipRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.PersonalChatRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.RepositoryAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;

public class FriendshipService implements FriendshipServiceAPI {

    private static FriendshipServiceAPI instance;

    private FriendshipService() {
    }

    public static FriendshipServiceAPI getInstance() {
        if (instance == null) {
            instance = new FriendshipService();
        }

        return instance;
    }

    @Override
    public Collection<User> getFriendsOf(User user) throws ValueNotFoundException {
        Guard.isNotNull(user);

        UserService.getInstance().ensureUserExists(user);

        return UserService.getInstance().getByCriteria(
            u -> FriendshipRepository.getInstance()
                .contains(f -> f.containsUser(user) && f.containsUser(u) && !user.equals(u))
        );
    }

    @Override
    public void addFriend(User sender, String targetUsername) throws ValueNotFoundException, UserBlockedException {
        Guard.isNotNull(sender);
        Guard.isNotNull(targetUsername);

        User senderUser = UserService.getInstance().ensureUserExists(sender);
        User targetUser = UserService.getInstance().ensureUserExists(targetUsername);

        if (BlockService.getInstance().checkBlock(targetUser, senderUser)) {
            throw new UserBlockedException("Sender blocked by target user");
        }

        if (BlockService.getInstance().checkBlock(senderUser, targetUser)) {
            throw new UserBlockedException("Sender has blocked target user");
        }

        FriendRequestRepository.getInstance()
            .add(new FriendRequest(senderUser, targetUser));
    }

    @Override
    public void removeFriend(User remover, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(remover);
        Guard.isNotNull(targetUsername);

        User removerUser = UserService.getInstance().ensureUserExists(remover);
        User targetUser = UserService.getInstance().ensureUserExists(targetUsername);

        ensureFriendshipExists(removerUser, targetUser);

        FriendshipRepository.getInstance().remove(
            f -> f.containsUser(removerUser) && f.containsUser(targetUser)
        );
    }

    @Override
    public void acceptRequest(User accepter, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(accepter);
        Guard.isNotNull(targetUsername);

        User accepterUser = UserService.getInstance().ensureUserExists(accepter);
        User targetUser = UserService.getInstance().ensureUserExists(targetUsername);

        FriendshipRepository.getInstance().add(new Friendship(targetUser, accepterUser));
        PersonalChatRepository.getInstance().add(new PersonalChat(targetUser, accepterUser));

        FriendRequestRepository.getInstance()
            .remove(f -> f.sender().equals(targetUser) && f.receiver().equals(accepterUser));
    }

    @Override
    public void declineRequest(User decliner, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(decliner);
        Guard.isNotNull(targetUsername);

        User declinerUser = UserService.getInstance().ensureUserExists(decliner);
        User targetUser = UserService.getInstance().ensureUserExists(targetUsername);

        FriendRequestRepository.getInstance()
            .remove(f -> f.sender().equals(targetUser) && f.receiver().equals(declinerUser));
    }

    @Override
    public void ensureFriendshipExists(User left, User right) throws ValueNotFoundException {

        if (!FriendshipRepository.getInstance().contains(f -> f.containsUser(left) && f.containsUser(right))) {
            throw new ValueNotFoundException("Friendship between users does not exist.");
        }

    }

}
