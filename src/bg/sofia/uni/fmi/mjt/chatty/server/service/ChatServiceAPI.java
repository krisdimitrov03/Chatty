package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

public interface ChatServiceAPI {

    PersonalChat getPersonalChat(User left, User right);

    GroupChat getGroupChat(String name);

    void createGroupChat(String name, User admin);

    // TODO: Add more operations

}
