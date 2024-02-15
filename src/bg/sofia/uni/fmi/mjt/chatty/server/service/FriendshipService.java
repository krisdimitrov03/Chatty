package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.FriendRequest;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Friendship;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendRequestRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendshipRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.NotificationRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.PersonalChatRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Collection<UserDTO> getFriendsOf(String username) throws ValueNotFoundException {
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);

        return UserService.getInstance().getByCriteria(
                        u -> FriendshipRepository.getInstance()
                                .contains(f -> f.containsUser(user) && f.containsUser(u) && !user.equals(u)))
                .stream()
                .map(u -> new UserDTO(u.getFullName(), u.username()))
                .collect(Collectors.toSet());
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

        String notificationContent = "From " + senderUser.getFullName() + " [" + sender + "]";
        NotificationService.getInstance()
                .addNotification(targetUser, NotificationType.FRIEND_REQUEST, notificationContent);
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
    public Collection<UserDTO> getRequests(String username) throws ValueNotFoundException {
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);

        return FriendRequestRepository.getInstance()
                .get(r -> r.receiver().equals(user))
                .stream()
                .map(r -> new UserDTO(r.sender().getFullName(), r.sender().username()))
                .collect(Collectors.toSet());
    }

    @Override
    public void acceptRequest(String accepter, String target) throws ValueNotFoundException {
        var userPair = validateRequestOperation(accepter, target);

        User accepterUser = userPair.getKey();
        User targetUser = userPair.getValue();

        FriendshipRepository.getInstance().add(new Friendship(targetUser, accepterUser));
        PersonalChatRepository.getInstance().add(new PersonalChat(targetUser, accepterUser));

        FriendRequestRepository.getInstance()
                .remove(f -> f.sender().equals(targetUser) && f.receiver().equals(accepterUser));

        String notificationContent = accepterUser.getFullName() + " accepted your friend request";
        NotificationService.getInstance()
                .addNotification(targetUser, NotificationType.OTHER, notificationContent);
    }

    @Override
    public void declineRequest(String decliner, String target) throws ValueNotFoundException {
        var userPair = validateRequestOperation(decliner, target);

        User declinerUser = userPair.getKey();
        User targetUser = userPair.getValue();

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

    public Map.Entry<User, User> validateRequestOperation(String actor, String target) throws ValueNotFoundException {
        Guard.isNotNull(actor);
        Guard.isNotNull(target);

        User actorUser = UserService.getInstance().ensureUserExists(actor);
        User targetUser = UserService.getInstance().ensureUserExists(target);

        if (!FriendRequestRepository.getInstance()
                .contains(r -> r.sender().equals(targetUser) && r.receiver().equals(actorUser))) {
            throw new ValueNotFoundException("You have no friend request from " + target);
        }

        return Map.entry(actorUser, targetUser);
    }

}
