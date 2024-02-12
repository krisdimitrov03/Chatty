package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;

public interface FriendshipServiceAPI {

    Collection<User> getFriendsOf(User user) throws ValueNotFoundException;

    void addFriend(String sender, String target)
        throws ValueNotFoundException, UserBlockedException, FriendshipAlreadyExistsException,
        FriendRequestAlreadySentException;

    void removeFriend(String remover, String target) throws ValueNotFoundException;

    void acceptRequest(String accepter, String target) throws ValueNotFoundException;

    void declineRequest(String decliner, String target) throws ValueNotFoundException;

    public void ensureFriendshipExists(User left, User right) throws ValueNotFoundException;

}
