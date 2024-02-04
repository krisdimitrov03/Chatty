package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Friendship;

public class FriendshipRepository extends Repository<Friendship> {

    private static final String DB_PATH = "";

    private static final FriendshipRepository instance = new FriendshipRepository(DB_PATH);

    private FriendshipRepository(String path) {
        super(path);
    }

    public static FriendshipRepository getInstance() {
        return instance;
    }

}
