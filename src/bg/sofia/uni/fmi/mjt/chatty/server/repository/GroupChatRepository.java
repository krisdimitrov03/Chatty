package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;

public class GroupChatRepository extends Repository<GroupChat> {

    private static final String DB_PATH = "";

    private static final GroupChatRepository instance = new GroupChatRepository(DB_PATH);

    private GroupChatRepository(String path) {
        super(path);
    }

    public static GroupChatRepository getInstance() {
        return instance;
    }

}
