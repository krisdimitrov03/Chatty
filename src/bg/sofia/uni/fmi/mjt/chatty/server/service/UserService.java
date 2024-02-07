package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.RepositoryAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class UserService implements UserServiceAPI {

    private final RepositoryAPI<User> userRepo;

    public UserService() {
        this.userRepo = UserRepository.getInstance();
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
