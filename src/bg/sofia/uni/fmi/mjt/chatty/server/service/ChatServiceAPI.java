package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

public interface ChatServiceAPI {

    PersonalChat getPersonalChat(String left, String right) throws ValueNotFoundException;

    void deletePersonalChat(String left, String right) throws ValueNotFoundException;

    void sendPersonalMessage(String sender, String reciever, String text) throws ValueNotFoundException;

    void createGroupChat(String name, String username);

    void deleteGroupChat(String name, String deleter);

    GroupChat getGroupChat(String name);

    void addToGroupChat(String name, String adder, String added);

    void removeFromGroupChat(String name, String remover, String removed);

    void sendGroupMessage(String chatName, String sender, String text);

}
