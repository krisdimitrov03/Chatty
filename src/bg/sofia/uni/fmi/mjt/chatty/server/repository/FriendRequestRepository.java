package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.FriendRequest;

import java.io.InputStream;

public class FriendRequestRepository extends Repository<FriendRequest> {

    private static final String DB_PATH = "friend_requests.dat";

    private static FriendRequestRepository instance;

    private FriendRequestRepository(String path) {
        super(path);
    }

    private FriendRequestRepository(InputStream stream) {
        super(stream);
    }

    public static FriendRequestRepository getInstance() {
        if (instance == null) {
            instance = new FriendRequestRepository(DB_PATH);
        }

        return instance;
    }

    public static FriendRequestRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new FriendRequestRepository(stream);
        }

        return instance;
    }

}
