package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

public interface BlockServiceAPI {

    void block(String blocker, String blocked) throws ValueNotFoundException;

    void unblock(String unblocker, String unblocked) throws ValueNotFoundException;

    boolean checkBlock(User blocker, User blocked);

}
