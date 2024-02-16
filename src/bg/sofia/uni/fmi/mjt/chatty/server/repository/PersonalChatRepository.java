package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

import java.io.InputStream;

public class PersonalChatRepository extends Repository<PersonalChat> {

    private static final String DB_PATH = "personal_chats.dat";

    private static PersonalChatRepository instance;

    private PersonalChatRepository(String path) {
        super(path);
    }

    private PersonalChatRepository(InputStream stream) {
        super(stream);
    }

    public static PersonalChatRepository getInstance() {
        if (instance == null) {
            instance = new PersonalChatRepository(DB_PATH);
        }

        return instance;
    }

    public static PersonalChatRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new PersonalChatRepository(stream);
        }

        return instance;
    }

}
