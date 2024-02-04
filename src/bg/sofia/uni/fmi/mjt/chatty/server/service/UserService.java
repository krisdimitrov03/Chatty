package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
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
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserService implements UserServiceAPI {

    private final RepositoryAPI<User> userRepo;

    private final RepositoryAPI<Friendship> friendshipRepo;

    private final RepositoryAPI<FriendRequest> friendRequestRepo;

    private final RepositoryAPI<Block> blockRepo;

    private final RepositoryAPI<PersonalChat> personalChatRepo;

    public UserService() {
        userRepo = UserRepository.getInstance();
        friendshipRepo = FriendshipRepository.getInstance();
        friendRequestRepo = FriendRequestRepository.getInstance();
        blockRepo = BlockRepository.getInstance();
        personalChatRepo = PersonalChatRepository.getInstance();
    }

    @Override
    public void register(String username, String password) throws UserAlreadyExistsException {
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Guard.isValidUsername(username);
        Guard.isValidPassword(password);

        if (userRepo.contains(u -> u.username().equals(username))) {
            throw new UserAlreadyExistsException("User already in repo");
        }

        userRepo.add(new User(username, hashPassword(password)));
    }

    @Override
    public Optional<User> login(String username, String password) {
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Predicate<User> loginCriteria =
            u -> u.username().equals(username) && u.passwordHash().equals(hashPassword(password));

        return userRepo.get(loginCriteria).stream().findAny();
    }

    @Override
    public Collection<User> getFriends(String username) {
        Guard.isNotNull(username);

        return friendshipRepo.get(f -> f.isUserInside(username))
            .stream()
            .map(f -> f.getFriendOf(username).orElse(null))
            .collect(Collectors.toSet());
    }

    @Override
    public void addFriend(User sender, String targetUsername) throws ValueNotFoundException, UserBlockedException {
        Guard.isNotNull(sender);
        Guard.isNotNull(targetUsername);

        if (!userRepo.contains(sender)) {
            throw new ValueNotFoundException("Sender not found");
        }

        User senderUser = ensureUserExists(sender);

        User targetUser = ensureUserExists(targetUsername);

        if (blockRepo.contains(b -> b.blocker().equals(targetUser))) {
            throw new UserBlockedException("Sender blocked by target user");
        }

        if (blockRepo.contains(b -> b.blocker().equals(senderUser))) {
            throw new UserBlockedException("Sender has blocked target user");
        }

        friendRequestRepo.add(new FriendRequest(senderUser, targetUser));
    }

    @Override
    public void acceptRequest(User accepter, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(accepter);
        Guard.isNotNull(targetUsername);

        User accepterUser = ensureUserExists(accepter);
        User targetUser = ensureUserExists(targetUsername);

        friendshipRepo.add(new Friendship(targetUser, accepterUser));
        personalChatRepo.add(new PersonalChat(targetUser, accepterUser));

        friendRequestRepo.remove(f -> f.sender().equals(targetUser) && f.receiver().equals(accepterUser));
    }

    @Override
    public void rejectRequest(User rejecter, String targetUsername) throws ValueNotFoundException {
        Guard.isNotNull(rejecter);
        Guard.isNotNull(targetUsername);

        User rejecterUser = ensureUserExists(rejecter);
        User targetUser = ensureUserExists(targetUsername);

        friendRequestRepo.remove(f -> f.sender().equals(targetUser) && f.receiver().equals(rejecterUser));
    }

    @Override
    public void block(User blocker, String blockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(blocker);
        Guard.isNotNull(blockedUsername);

        User blockerUser = ensureUserExists(blocker);
        User blockedUser = ensureUserExists(blockedUsername);

        // TODO: Remove friendship and personalChat

        blockRepo.add(new Block(blockerUser, blockedUser));
    }

    @Override
    public void unblock(User unblocker, String unblockedUsername) throws ValueNotFoundException {
        Guard.isNotNull(unblocker);
        Guard.isNotNull(unblockedUsername);

        User unblockerUser = ensureUserExists(unblocker);
        User unblockedUser = ensureUserExists(unblockedUsername);

        blockRepo.remove(b -> b.blocker().equals(unblockerUser) && b.blocked().equals(unblockedUser));
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    private User ensureUserExists(User user) throws ValueNotFoundException {
        return userRepo
            .get(u -> u.equals(user))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    private User ensureUserExists(String username) throws ValueNotFoundException {
        return userRepo
            .get(u -> u.username().equals(username))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

}
