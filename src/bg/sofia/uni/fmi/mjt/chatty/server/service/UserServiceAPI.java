package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserServiceAPI {

    void register(String username, String password) throws UserAlreadyExistsException;

    Optional<User> login(String username, String password);

    Collection<User> getFriends(String username);

    void addFriend(User sender, String targetUsername);

    void acceptRequest(User accepter, String targetUsername);

    void rejectRequest(User rejecter, String targetUsername);

    void block(User blocker, String blockedUsername);

    void unblock(User unblocker, String unblockedUsername);

}
