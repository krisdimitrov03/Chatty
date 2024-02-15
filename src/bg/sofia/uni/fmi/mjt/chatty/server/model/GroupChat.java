package bg.sofia.uni.fmi.mjt.chatty.server.model;

import bg.sofia.uni.fmi.mjt.chatty.server.security.Guard;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class GroupChat extends Chat implements Entity {

    private final User admin;

    private final String name;

    public GroupChat(String name, User admin) {
        super(new LinkedHashSet<>(Set.of(admin)), new LinkedList<>());

        this.admin = admin;
        this.name = name;
    }

    public void addUser(User user) {
        Guard.isNotNull(user);
        getUsers().add(user);
    }

    public void removeUser(User user) {
        Guard.isNotNull(user);
        getUsers().remove(user);
    }

    public User getAdmin() {
        return this.admin;
    }

    public String getName() {
        return this.name;
    }

}
