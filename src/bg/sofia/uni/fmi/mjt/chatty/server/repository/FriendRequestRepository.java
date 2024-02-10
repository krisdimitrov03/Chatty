package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.FriendRequest;

public class FriendRequestRepository extends Repository<FriendRequest> {

    private static final String DB_PATH = "friend_requests.dat";

    private static FriendRequestRepository instance;

    private FriendRequestRepository(String path) {
        super(path);
    }

    public static FriendRequestRepository getInstance() {
        if(instance == null) {
            instance = new FriendRequestRepository(DB_PATH);
        }

        return instance;
    }

}
