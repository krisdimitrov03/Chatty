package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.*;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.*;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlockServiceTest {

    private static Repository<Friendship> fRepo;
    private static Repository<FriendRequest> frRepo;
    private static Repository<Block> bRepo;
    private static Repository<Notification> nRepo;
    private static Repository<User> userRepo;
    private static Repository<PersonalChat> pcRepo;
    private static Repository<GroupChat> gcRepo;

    private static BlockServiceAPI service;
    private static UserServiceAPI userService;
    private static FriendshipServiceAPI friendshipService;

    @BeforeAll
    static void setupTests() {
        fRepo = FriendshipRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        frRepo = FriendRequestRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        bRepo = BlockRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        userRepo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        nRepo = NotificationRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        pcRepo = PersonalChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        gcRepo = GroupChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));

        service = BlockService.getInstance();
        userService = UserService.getInstance();
        friendshipService = FriendshipService.getInstance();
    }

    @AfterEach
    void clearRepo() {
        try {
            userRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            fRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            frRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            bRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            nRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            pcRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
        try {
            gcRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
    }

    @Test
    void testBlockWithExistingBlock() throws UserAlreadyExistsException, ValueNotFoundException, UserBlockedException {
        String blockerUsername = "g.patrick";
        String blockedUsername = "s.johnson";

        User blocker = new User("George", "Patrick", blockerUsername, SHA256.hashPassword("Parola123"));
        User blocked = new User("Steven", "Johnson", blockedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", blockerUsername, "Parola123");
        userService.register("Steven", "Johnson", blockedUsername, "Parola123");

        fRepo.add(new Friendship(blocker, blocked));
        pcRepo.add(new PersonalChat(blocker, blocked));

        service.block(blockerUsername, blockedUsername);

        assertThrows(UserBlockedException.class, () -> service.block(blockerUsername, blockedUsername),
                "Block should throw if block already exists");
    }

    @Test
    void testBlockForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException, UserBlockedException {
        String blockerUsername = "g.patrick";
        String blockedUsername = "s.johnson";

        User blocker = new User("George", "Patrick", blockerUsername, SHA256.hashPassword("Parola123"));
        User blocked = new User("Steven", "Johnson", blockedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", blockerUsername, "Parola123");
        userService.register("Steven", "Johnson", blockedUsername, "Parola123");

        fRepo.add(new Friendship(blocker, blocked));
        pcRepo.add(new PersonalChat(blocker, blocked));

        service.block(blockerUsername, blockedUsername);

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(friendshipService.getFriendsOf(blockerUsername)),
                "Block should remove friendship");

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(friendshipService.getFriendsOf(blockedUsername)),
                "Block should remove friendship");

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(pcRepo.get(f -> f.getUsers().contains(blocked) && f.getUsers().contains(blocker))),
                "Block should remove personal chat");
    }

    @Test
    void testUnblockWithNoBlock() throws UserAlreadyExistsException, ValueNotFoundException, UserBlockedException {
        String blockerUsername = "g.patrick";
        String blockedUsername = "s.johnson";

        User blocker = new User("George", "Patrick", blockerUsername, SHA256.hashPassword("Parola123"));
        User blocked = new User("Steven", "Johnson", blockedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", blockerUsername, "Parola123");
        userService.register("Steven", "Johnson", blockedUsername, "Parola123");

        fRepo.add(new Friendship(blocker, blocked));
        pcRepo.add(new PersonalChat(blocker, blocked));

        assertThrows(ValueNotFoundException.class, () -> service.unblock(blockerUsername, blockedUsername),
                "Unlock should throw if no block");
    }

    @Test
    void testUnblockForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException, UserBlockedException {
        String unblockerUsername = "g.patrick";
        String unblockedUsername = "s.johnson";

        User unblocker = new User("George", "Patrick", unblockerUsername, SHA256.hashPassword("Parola123"));
        User unblocked = new User("Steven", "Johnson", unblockedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", unblockerUsername, "Parola123");
        userService.register("Steven", "Johnson", unblockedUsername, "Parola123");

        fRepo.add(new Friendship(unblocker, unblocked));
        pcRepo.add(new PersonalChat(unblocker, unblocked));

        service.block(unblockerUsername, unblockedUsername);

        service.unblock(unblockerUsername, unblockedUsername);

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(bRepo.get(b -> b.blocker().equals(unblocker) && b.blocked().equals(unblocked))),
                "Block should be removed");
    }

    @Test
    void testGetBlockedByForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException, UserBlockedException {
        String blockerUsername = "g.patrick";
        String blockedUsername = "s.johnson";

        User blocker = new User("George", "Patrick", blockerUsername, SHA256.hashPassword("Parola123"));
        User blocked = new User("Steven", "Johnson", blockedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", blockerUsername, "Parola123");
        userService.register("Steven", "Johnson", blockedUsername, "Parola123");

        fRepo.add(new Friendship(blocker, blocked));
        pcRepo.add(new PersonalChat(blocker, blocked));

        service.block(blockerUsername, blockedUsername);

        assertEquals(Set.of(new UserDTO(blocked.getFullName(), blockedUsername)), service.getBlockedBy(blockerUsername),
                "Get blocked should return proper collection of blocked users");
    }

}
