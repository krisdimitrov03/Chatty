package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Friendship;

import java.io.InputStream;

public class FriendshipRepository extends Repository<Friendship> {

    private static final String DB_PATH = "friendships.dat";

    private static FriendshipRepository instance;

    private FriendshipRepository(String path) {
        super(path);
    }

    private FriendshipRepository(InputStream stream) {
        super(stream);
    }

    public static FriendshipRepository getInstance() {
        if (instance == null) {
            instance = new FriendshipRepository(DB_PATH);
        }

        return instance;
    }

    public static FriendshipRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new FriendshipRepository(stream);
        }

        return instance;
    }

}
