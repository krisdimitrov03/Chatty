package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.security.Guard;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;

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
            throw new UserAlreadyExistsException("User with this username already exists");
        }

        UserRepository.getInstance()
                .add(new User(firstName, lastName, username, SHA256.hashPassword(password)));
    }

    @Override
    public SessionDTO login(String username, String password) throws ValueNotFoundException {
        Guard.isNotNull(username);
        Guard.isNotNull(password);

        Predicate<User> loginCriteria =
                u -> u.username().equals(username) && u.passwordHash().equals(SHA256.hashPassword(password));

        Optional<User> user = UserRepository.getInstance().get(loginCriteria).stream().findAny();

        if (user.isEmpty()) {
            throw new ValueNotFoundException("Incorrect username or password");
        }

        UserDTO userDto = new UserDTO(user.get().getFullName(), user.get().username());

        return new SessionDTO(userDto, NotificationService.getInstance().getNotificationsOf(username));
    }

    @Override
    public User ensureUserExists(String username) throws ValueNotFoundException {
        return getByCriteria(u -> u.username().equals(username))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValueNotFoundException("User not found"));
    }

    @Override
    public Collection<User> getByCriteria(Predicate<User> criteria) {
        Guard.isNotNull(criteria);

        return UserRepository.getInstance().get(criteria);
    }

}
