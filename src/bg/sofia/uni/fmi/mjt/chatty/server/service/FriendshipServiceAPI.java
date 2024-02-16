package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;

public interface FriendshipServiceAPI {

    Collection<UserDTO> getFriendsOf(String username) throws ValueNotFoundException;

    void addFriend(String sender, String target) throws ValueNotFoundException, UserBlockedException,
            FriendshipAlreadyExistsException, FriendRequestAlreadySentException;

    void removeFriend(String remover, String target) throws ValueNotFoundException;

    Collection<UserDTO> getRequests(String username) throws ValueNotFoundException;

    void acceptRequest(String accepter, String target) throws ValueNotFoundException;

    void declineRequest(String decliner, String target) throws ValueNotFoundException;

    void ensureFriendshipExists(User left, User right) throws ValueNotFoundException;

}
