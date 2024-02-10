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

    private final RepositoryAPI<Friendship> friendshipRepo;

    private final RepositoryAPI<FriendRequest> friendRequestRepo;

    private final RepositoryAPI<PersonalChat> personalChatRepo;

    private final RepositoryAPI<Block> blockRepo;

    private final UserServiceAPI userService;

    private FriendshipService() {
        friendshipRepo = FriendshipRepository.getInstance();
        friendRequestRepo = FriendRequestRepository.getInstance();
        personalChatRepo = PersonalChatRepository.getInstance();
        blockRepo = BlockRepository.getInstance();

        userService = UserService.getInstance();
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

        userService.ensureUserExists(user);

        return userService.getByCriteria(
            u -> friendshipRepo.contains(f -> f.containsUser(user) && f.containsUser(u) && !user.equals(u))
        );
    }

    @Override
    public void addFriend(User sender, String targetUsername) throws ValueNotFoundException, UserBlockedException {
        Guard.isNotNull(sender);
        Guard.isNotNull(targetUsername);

        User senderUser = userService.ensureUserExists(sender);
        User targetUser = userService.ensureUserExists(targetUsername);

        if (blockRepo.contains(b -> b.blocker().equals(targetUser) && b.blocked().equals(senderUser))) {
            throw new UserBlockedException("Sender blocked by target user");
        }

        if (blockRepo.contains(b -> b.blocker().equals(senderUser) && b.blocked().equals(targetUser))) {
            throw new UserBlockedException("Sender has blocked target user");
        }

        friendRequestRepo.add(new FriendRequest(senderUser, targetUser));
    }

    @Override
    public void removeFriend(User remover, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(remover);
        Guard.isNotNull(targetUsername);

        User removerUser = userService.ensureUserExists(remover);
        User targetUser = userService.ensureUserExists(targetUsername);

        ensureFriendshipExists(removerUser, targetUser);

        friendshipRepo.remove(
            f -> f.containsUser(removerUser) && f.containsUser(targetUser)
        );
    }

    @Override
    public void acceptRequest(User accepter, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(accepter);
        Guard.isNotNull(targetUsername);

        User accepterUser = userService.ensureUserExists(accepter);
        User targetUser = userService.ensureUserExists(targetUsername);

        friendshipRepo.add(new Friendship(targetUser, accepterUser));
        personalChatRepo.add(new PersonalChat(targetUser, accepterUser));

        friendRequestRepo.remove(f -> f.sender().equals(targetUser) && f.receiver().equals(accepterUser));
    }

    @Override
    public void declineRequest(User decliner, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(decliner);
        Guard.isNotNull(targetUsername);

        User declinerUser = userService.ensureUserExists(decliner);
        User targetUser = userService.ensureUserExists(targetUsername);

        friendRequestRepo.remove(f -> f.sender().equals(targetUser) && f.receiver().equals(declinerUser));
    }

    private void ensureFriendshipExists(User left, User right) throws ValueNotFoundException {

        if (!friendshipRepo.contains(f -> f.containsUser(left) && f.containsUser(right))) {
            throw new ValueNotFoundException("Friendship between users does not exist.");
        }

    }

}
