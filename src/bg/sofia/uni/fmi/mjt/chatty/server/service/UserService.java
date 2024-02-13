package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class UserService implements UserServiceAPI {

    private static UserServiceAPI instance;

    private UserService() {
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

        Guard.isValidName(firstName);
        Guard.isValidName(lastName);
        Guard.isValidUsername(username);
        Guard.isValidPassword(password);

        if (UserRepository.getInstance().contains(u -> u.username().equals(username))) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserRepository.getInstance()
            .add(new User(firstName, lastName, username, hashPassword(password)));
    }

    @Override
    public SessionDTO login(String username, String password) throws ValueNotFoundException {
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Predicate<User> loginCriteria =
            u -> u.username().equals(username) && u.passwordHash().equals(hashPassword(password));

        Optional<User> user = UserRepository.getInstance().get(loginCriteria).stream().findAny();

        if (user.isEmpty()) {
            throw new ValueNotFoundException("Incorrect username or password");
        }

        UserDTO userDto = new UserDTO(user.get().getFullName(), user.get().username());

        return new SessionDTO(userDto, NotificationService.getInstance().getNotificationsOf(username));
    }

    @Override
    public User ensureUserExists(User user) throws ValueNotFoundException {
        return UserRepository.getInstance()
            .get(u -> u.equals(user))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    @Override
    public User ensureUserExists(String username) throws ValueNotFoundException {
        return UserRepository.getInstance()
            .get(u -> u.username().equals(username))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    @Override
    public Collection<User> getByCriteria(Predicate<User> criteria) {
        Guard.isNotNull(criteria);

        return UserRepository.getInstance().get(criteria);
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

}
