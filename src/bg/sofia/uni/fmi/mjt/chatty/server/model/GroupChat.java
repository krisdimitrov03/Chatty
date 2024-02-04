package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.SequencedCollection;

public class GroupChat extends Chat implements Entity {

    private final User admin;

    private final String name;

    public GroupChat(String name, User admin) {
        super(new LinkedHashSet<>(), new LinkedList<>());

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
        if (user == null) {
            throw new IllegalArgumentException("Username is null");
        }

        getUsers().add(user);
    }

    public User getAdmin() {
        return this.admin;
    }

    public String getName() {
        return this.name;
    }

}
