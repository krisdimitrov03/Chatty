package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Chat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

import java.io.Reader;

public class PersonalChatRepository extends Repository<PersonalChat> {

    private static final String DB_PATH = "";

    private static final PersonalChatRepository instance = new PersonalChatRepository(DB_PATH);

    private PersonalChatRepository(String path) {
        super(path);
    }

    public static PersonalChatRepository getInstance() {
        return instance;
    }

}
