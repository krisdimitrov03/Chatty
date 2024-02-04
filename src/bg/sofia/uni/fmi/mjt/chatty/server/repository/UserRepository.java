package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Entity;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;

public class UserRepository extends Repository<User> {

    private static final String DB_PATH = "";

    private static final UserRepository instance = new UserRepository(DB_PATH);

    private UserRepository(String path) {
        super(path);
    }

    public static UserRepository getInstance() {
        return instance;
    }

}
