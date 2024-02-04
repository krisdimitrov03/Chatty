package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendRequestRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.FriendshipRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserService implements UserServiceAPI {

    private final UserRepository userRepo;

    private final FriendshipRepository friendshipRepo;

    private final FriendRequestRepository friendRequestRepo;

    public UserService() {
        userRepo = UserRepository.getInstance();
        friendshipRepo = FriendshipRepository.getInstance();
        friendRequestRepo = FriendRequestRepository.getInstance();
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

        userRepo.add(new User(username, String.valueOf(password.hashCode())));
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
    public void addFriend(User sender, String targetUsername) {
        Guard.isNotNull(sender);
        Guard.isNotNull(targetUsername);


    }

    @Override
    public void acceptRequest(User accepter, String targetUsername) {

    }

    @Override
    public void rejectRequest(User rejecter, String targetUsername) {

    }

    @Override
    public void block(User blocker, String blockedUsername) {

    }

    @Override
    public void unblock(User unblocker, String unblockedUsername) {

    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }
}
