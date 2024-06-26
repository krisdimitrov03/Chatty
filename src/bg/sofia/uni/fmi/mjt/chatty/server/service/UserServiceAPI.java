package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;
import java.util.function.Predicate;

public interface UserServiceAPI {

    void register(String firstName, String lastName, String username, String password)
        throws UserAlreadyExistsException;

    SessionDTO login(String username, String password) throws ValueNotFoundException;

    User ensureUserExists(String username) throws ValueNotFoundException;

    Collection<User> getByCriteria(Predicate<User> criteria);

}
