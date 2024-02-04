package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserServiceAPI {

    void register(String username, String password) throws UserAlreadyExistsException;

    Optional<User> login(String username, String password);

    Collection<User> getFriends(String username);

    void addFriend(User sender, String targetUsername) throws ValueNotFoundException, UserBlockedException;

    void acceptRequest(User accepter, String targetUsername) throws ValueNotFoundException;

    void rejectRequest(User rejecter, String targetUsername) throws ValueNotFoundException;

    void block(User blocker, String blockedUsername) throws ValueNotFoundException;

    void unblock(User unblocker, String unblockedUsername) throws ValueNotFoundException;

}
