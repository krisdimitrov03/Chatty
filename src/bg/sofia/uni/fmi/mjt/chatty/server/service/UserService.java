package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.dto.Session;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.NotificationRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.RepositoryAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Predicate;

public class UserService implements UserServiceAPI {

    private static UserServiceAPI instance;

    private final RepositoryAPI<User> userRepo;

    private final RepositoryAPI<Notification> notificationRepo;

    private UserService() {
        this.userRepo = UserRepository.getInstance();
        this.notificationRepo = NotificationRepository.getInstance();
    }

    public static UserServiceAPI getInstance() {
        if (instance == null) {
            instance = new UserService();
        }

        return instance;
    }

    @Override
    public void register(String firstName, String lastName, String username, String password)
        throws UserAlreadyExistsException {
        Guard.isNotNull(firstName);
        Guard.isNotNull(lastName);
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Guard.isValidUsername(username);
        Guard.isValidPassword(password);

        if (userRepo.contains(u -> u.username().equals(username))) {
            throw new UserAlreadyExistsException("User already in repo");
        }

        userRepo.add(new User(firstName, lastName, username, hashPassword(password)));
    }

    @Override
    public Session login(String username, String password) {
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Predicate<User> loginCriteria =
            u -> u.username().equals(username) && u.passwordHash().equals(hashPassword(password));

        Optional<User> user = userRepo.get(loginCriteria).stream().findAny();

        Optional<UserDTO> userDto = user.map(value -> new UserDTO(value.getFullName(), value.username()));

        if (user.isEmpty()) {
            return new Session(userDto, new LinkedHashSet<>());
        }

        var notifications = notificationRepo.get(n -> n.user().equals(user.get()));

        return new Session(userDto, notifications);
    }

    @Override
    public User ensureUserExists(User user) throws ValueNotFoundException {
        return userRepo
            .get(u -> u.equals(user))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    @Override
    public User ensureUserExists(String username) throws ValueNotFoundException {
        return userRepo
            .get(u -> u.username().equals(username))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    @Override
    public Collection<User> getByCriteria(Predicate<User> criteria) {
        Guard.isNotNull(criteria);

        return userRepo.get(criteria);
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

}
