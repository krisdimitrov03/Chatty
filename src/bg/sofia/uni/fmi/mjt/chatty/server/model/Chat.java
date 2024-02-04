package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.util.Collection;
import java.util.SequencedCollection;

public abstract class Chat {

    private final Collection<User> users;

    private final SequencedCollection<Message> messages;

    public Chat(Collection<User> users, SequencedCollection<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public SequencedCollection<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message is null");
        }

        messages.add(message);
    }

}