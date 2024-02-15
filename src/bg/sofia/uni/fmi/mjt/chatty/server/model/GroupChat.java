package bg.sofia.uni.fmi.mjt.chatty.server.model;

import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.*;

public class GroupChat extends Chat implements Entity {

    private final User admin;

    private final String name;

    public GroupChat(String name, User admin) {
        super(new LinkedHashSet<>(Set.of(admin)), new LinkedList<>());

        this.admin = admin;
        this.name = name;
    }

    public GroupChat(String name, Collection<User> users, User admin) {
        super(users, new LinkedList<>());

        this.admin = admin;
        this.name = name;
    }

    public GroupChat(String name, Collection<User> users, User admin, SequencedCollection<Message> messages) {
        super(users, messages);

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
