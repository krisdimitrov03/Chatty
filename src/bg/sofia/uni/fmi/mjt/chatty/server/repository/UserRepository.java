package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

public class UserRepository extends Repository<User> {

    private static final String DB_PATH = "users.dat";

    private static UserRepository instance;

    private UserRepository(String path) {
        super(path);
    }

    public static UserRepository getInstance() {
        if(instance == null) {
            instance = new UserRepository(DB_PATH);
        }

        return instance;
    }

}
