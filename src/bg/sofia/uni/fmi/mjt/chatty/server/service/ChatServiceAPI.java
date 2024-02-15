package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.AccessDeniedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyInGroupException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

public interface ChatServiceAPI {

    PersonalChat getPersonalChat(String left, String right) throws ValueNotFoundException;

    void deletePersonalChat(String left, String right) throws ValueNotFoundException;

    void sendPersonalMessage(String sender, String reciever, String text) throws ValueNotFoundException;

    void createGroupChat(String name, String username) throws ValueNotFoundException, UserAlreadyInGroupException;

    void deleteGroupChat(String name, String deleter) throws ValueNotFoundException, AccessDeniedException;

    GroupChat getGroupChat(String name, String username) throws ValueNotFoundException;

    void addToGroupChat(String name, String adder, String added) throws ValueNotFoundException, AccessDeniedException, UserAlreadyInGroupException;

    void removeFromGroupChat(String name, String remover, String removed) throws ValueNotFoundException, AccessDeniedException;

    void sendGroupMessage(String chatName, String sender, String text) throws ValueNotFoundException;

}
