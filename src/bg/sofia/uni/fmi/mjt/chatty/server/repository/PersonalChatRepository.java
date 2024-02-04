package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Chat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

import java.io.Reader;

public class PersonalChatRepository extends Repository<PersonalChat> {

    public PersonalChatRepository() {
        super();
    }

    public PersonalChatRepository(String path) {
        super(path);
    }

}
