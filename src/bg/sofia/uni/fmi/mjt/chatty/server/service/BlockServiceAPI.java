package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

public interface BlockServiceAPI {

    void block(User blocker, String blockedUsername) throws ValueNotFoundException;

    void unblock(User unblocker, String unblockedUsername) throws ValueNotFoundException;

    boolean checkBlock(User blocker, User blocked);

}
