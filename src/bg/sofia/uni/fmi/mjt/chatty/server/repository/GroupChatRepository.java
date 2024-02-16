package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;

import java.io.InputStream;

public class GroupChatRepository extends Repository<GroupChat> {

    private static final String DB_PATH = "group_chats.dat";

    private static GroupChatRepository instance;

    private GroupChatRepository(String path) {
        super(path);
    }

    private GroupChatRepository(InputStream stream) {
        super(stream);
    }

    public static GroupChatRepository getInstance() {
        if (instance == null) {
            instance = new GroupChatRepository(DB_PATH);
        }

        return instance;
    }

    public static GroupChatRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new GroupChatRepository(stream);
        }

        return instance;
    }

}
