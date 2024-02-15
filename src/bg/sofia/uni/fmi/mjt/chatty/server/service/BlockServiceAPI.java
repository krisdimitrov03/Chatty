package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;

import java.util.Collection;

public interface BlockServiceAPI {

    void block(String blocker, String blocked) throws ValueNotFoundException;

    void unblock(String unblocker, String unblocked) throws ValueNotFoundException;

    Collection<UserDTO> getBlockedBy(String blocker) throws ValueNotFoundException;

    boolean checkBlock(User blocker, User blocked);

}
