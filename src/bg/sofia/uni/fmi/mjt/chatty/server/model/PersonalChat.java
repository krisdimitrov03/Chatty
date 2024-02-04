package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.SequencedCollection;
import java.util.Set;

public class PersonalChat extends Chat implements Entity {

    public PersonalChat(User left, User right) {
        super(new HashSet<>(Set.of(left, right)), new LinkedList<>());
    }

    public PersonalChat(User left, User right, SequencedCollection<Message> messages) {
        super(new HashSet<>(Set.of(left, right)), messages);
    }
    
}
