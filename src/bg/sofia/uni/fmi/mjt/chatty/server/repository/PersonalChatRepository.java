package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;

public class PersonalChatRepository extends Repository<PersonalChat> {

    private static final String DB_PATH = "personal_chats.dat";

    private static PersonalChatRepository instance;

    private PersonalChatRepository(String path) {
        super(path);
    }

    public static PersonalChatRepository getInstance() {
        if (instance == null) {
            instance = new PersonalChatRepository(DB_PATH);
        }

        return instance;
    }

}
