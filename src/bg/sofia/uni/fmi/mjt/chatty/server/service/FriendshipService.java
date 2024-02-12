package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendshipAlreadyExistsException;
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
    public void addFriend(String sender, String target)
        throws ValueNotFoundException, UserBlockedException, FriendshipAlreadyExistsException,
        FriendRequestAlreadySentException {
        Guard.isNotNull(sender);
        Guard.isNotNull(target);

        User senderUser = UserService.getInstance().ensureUserExists(sender);
        User targetUser = UserService.getInstance().ensureUserExists(target);

        ensureNoFriendship(senderUser, targetUser);

        ensureNoFriendRequest(senderUser, targetUser, "You have already sent friend request to " + target);
        ensureNoFriendRequest(targetUser, senderUser, "You have already friend request from " + target);

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
    public void removeFriend(String remover, String target) throws ValueNotFoundException {
        Guard.isNotNull(remover);
        Guard.isNotNull(target);

        User removerUser = UserService.getInstance().ensureUserExists(remover);
        User targetUser = UserService.getInstance().ensureUserExists(target);

        ensureFriendshipExists(removerUser, targetUser);

        FriendshipRepository.getInstance().remove(
            f -> f.containsUser(removerUser) && f.containsUser(targetUser)
        );
    }

    @Override
    public void acceptRequest(String accepter, String target) throws ValueNotFoundException {
        Guard.isNotNull(accepter);
        Guard.isNotNull(target);

        User accepterUser = UserService.getInstance().ensureUserExists(accepter);
        User targetUser = UserService.getInstance().ensureUserExists(target);

        if (!FriendRequestRepository.getInstance()
            .contains(r -> r.sender().equals(targetUser) && r.receiver().equals(accepterUser))) {
            throw new ValueNotFoundException("You have no friend request from " + target);
        }

        FriendshipRepository.getInstance().add(new Friendship(targetUser, accepterUser));
        PersonalChatRepository.getInstance().add(new PersonalChat(targetUser, accepterUser));

        FriendRequestRepository.getInstance()
            .remove(f -> f.sender().equals(targetUser) && f.receiver().equals(accepterUser));
    }

    @Override
    public void declineRequest(String decliner, String target) throws ValueNotFoundException {
        Guard.isNotNull(decliner);
        Guard.isNotNull(target);

        User declinerUser = UserService.getInstance().ensureUserExists(decliner);
        User targetUser = UserService.getInstance().ensureUserExists(target);

        FriendRequestRepository.getInstance()
            .remove(f -> f.sender().equals(targetUser) && f.receiver().equals(declinerUser));
    }

    @Override
    public void ensureFriendshipExists(User left, User right) throws ValueNotFoundException {

        if (!FriendshipRepository.getInstance().contains(f -> f.containsUser(left) && f.containsUser(right))) {
            throw new ValueNotFoundException("Friendship between users does not exist.");
        }

    }

    public void ensureNoFriendship(User left, User right) throws FriendshipAlreadyExistsException {
        if (FriendshipRepository.getInstance()
            .contains(f -> f.containsUser(left) && f.containsUser(right))) {
            throw new FriendshipAlreadyExistsException("You are already friends with " + right);
        }
    }

    public void ensureNoFriendRequest(User left, User right, String message) throws FriendRequestAlreadySentException {
        if (FriendRequestRepository.getInstance()
            .contains(f -> f.sender().equals(left) && f.receiver().equals(right))) {
            throw new FriendRequestAlreadySentException(message);
        }
    }

}
