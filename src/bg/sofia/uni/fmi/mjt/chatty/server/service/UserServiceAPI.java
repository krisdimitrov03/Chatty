package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface UserServiceAPI {

    void register(String username, String password) throws UserAlreadyExistsException;

    Optional<User> login(String username, String password);

    User ensureUserExists(User user) throws ValueNotFoundException;

    User ensureUserExists(String username) throws ValueNotFoundException;

    Collection<User> getByCriteria(Predicate<User> criteria);

}
