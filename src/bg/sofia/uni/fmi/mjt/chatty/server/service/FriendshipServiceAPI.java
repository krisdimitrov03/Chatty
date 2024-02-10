package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;

public interface FriendshipServiceAPI {

    Collection<User> getFriendsOf(User user) throws ValueNotFoundException;

    void addFriend(User sender, String targetUsername) throws ValueNotFoundException, UserBlockedException;

    void removeFriend(User remover, String targetUsername) throws ValueNotFoundException;

    void acceptRequest(User accepter, String targetUsername) throws ValueNotFoundException;

    void declineRequest(User decliner, String targetUsername) throws ValueNotFoundException;

    public void ensureFriendshipExists(User left, User right) throws ValueNotFoundException;

}
