package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.io.InputStream;

public class UserRepository extends Repository<User> {

    private static final String DB_PATH = "users.dat";

    private static UserRepository instance;

    private UserRepository(String path) {
        super(path);
    }

    private UserRepository(InputStream stream) {
        super(stream);
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository(DB_PATH);
        }

        return instance;
    }

    public static UserRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new UserRepository(stream);
        }

        return instance;
    }

}
