package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.FriendRequest;

public class FriendRequestRepository extends Repository<FriendRequest> {

    private static final String DB_PATH = "";

    private static final FriendRequestRepository instance = new FriendRequestRepository(DB_PATH);

    private FriendRequestRepository(String path) {
        super(path);
    }

    public static FriendRequestRepository getInstance() {
        return instance;
    }

}
