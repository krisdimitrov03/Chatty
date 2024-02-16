package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.*;
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

public class FriendshipServiceTest {

    private static Repository<Friendship> fRepo;
    private static Repository<FriendRequest> frRepo;
    private static Repository<Block> bRepo;
    private static Repository<Notification> nRepo;
    private static Repository<User> userRepo;
    private static Repository<PersonalChat> pcRepo;

    private static FriendshipServiceAPI service;
    private static UserServiceAPI userService;

    @BeforeAll
    static void setupTests() {
        fRepo = FriendshipRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        frRepo = FriendRequestRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        bRepo = BlockRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        userRepo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        nRepo = NotificationRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        pcRepo = PersonalChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));

        service = FriendshipService.getInstance();
        userService = UserService.getInstance();
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
    }

    @Test
    void testAddFriendForExistingFriendship() throws UserAlreadyExistsException {
        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        fRepo.add(new Friendship(sender, receiver));

        assertThrows(FriendshipAlreadyExistsException.class, () -> service.addFriend(senderUsername, receiverUsername),
                "Add friend should throw if friendship exists");
    }

    @Test
    void testAddFriendForExistingFriendRequestFromSender() throws UserAlreadyExistsException {
        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        frRepo.add(new FriendRequest(sender, receiver));

        assertThrows(FriendRequestAlreadySentException.class, () -> service.addFriend(senderUsername, receiverUsername),
                "Add friend should throw if friend request already sent");
    }

    @Test
    void testAddFriendForExistingFriendRequestFromReceiver() throws UserAlreadyExistsException {
        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        frRepo.add(new FriendRequest(receiver, sender));

        assertThrows(FriendRequestAlreadySentException.class, () -> service.addFriend(senderUsername, receiverUsername),
                "Add friend should throw if friend request already sent from receiver");
    }

    @Test
    void testAddFriendForExistingBlockFromReceiver() throws UserAlreadyExistsException {
        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        bRepo.add(new Block(receiver, sender));

        assertThrows(UserBlockedException.class, () -> service.addFriend(senderUsername, receiverUsername),
                "Add friend should throw if receiver blocked sender");
    }

    @Test
    void testAddFriendForExistingBlockFromSender() throws UserAlreadyExistsException {
        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        bRepo.add(new Block(sender, receiver));

        assertThrows(UserBlockedException.class, () -> service.addFriend(senderUsername, receiverUsername),
                "Add friend should throw if sender blocked receiver");
    }

    @Test
    void testAddFriendForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException,
            FriendRequestAlreadySentException, FriendshipAlreadyExistsException, UserBlockedException {

        String senderUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User sender = new User("George", "Patrick", senderUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", senderUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        service.addFriend(senderUsername, receiverUsername);

        assertEquals(Set.of(new FriendRequest(sender, receiver)),
                new HashSet<>(frRepo.get(fr -> fr.sender().equals(sender) && fr.receiver().equals(receiver))),
                "Add friend should make correct friend request");
    }

    @Test
    void testRemoveFriendWithNoFriendship() throws UserAlreadyExistsException {
        String removerUsername = "g.patrick";
        String removedUsername = "s.johnson";

        userService.register("George", "Patrick", removerUsername, "Parola123");
        userService.register("Steven", "Johnson", removedUsername, "Parola123");

        assertThrows(ValueNotFoundException.class, () -> service.removeFriend(removerUsername, removedUsername),
                "Remove friend should throw if no friendship");
    }

    @Test
    void testRemoveFriendForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String removerUsername = "g.patrick";
        String removedUsername = "s.johnson";

        User remover = new User("George", "Patrick", removerUsername, SHA256.hashPassword("Parola123"));
        User removed = new User("Steven", "Johnson", removedUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", removerUsername, "Parola123");
        userService.register("Steven", "Johnson", removedUsername, "Parola123");

        fRepo.add(new Friendship(remover, removed));

        service.removeFriend(removerUsername, removedUsername);

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(fRepo.get(f -> f.containsUser(remover) && f.containsUser(removed))),
                "Remove friend should remove the friendship between users");
    }

    @Test
    void testGetFriendsOfForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        fRepo.add(new Friendship(left, right));

        assertEquals(Set.of(new UserDTO(right.getFullName(), rightUsername)), service.getFriendsOf(leftUsername),
                "Get friends should return correct set of user friends");
    }

    @Test
    void testGetRequestsForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String requesterUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User requester = new User("George", "Patrick", requesterUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", requesterUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        frRepo.add(new FriendRequest(requester, receiver));

        assertEquals(Set.of(new UserDTO(requester.getFullName(), requesterUsername)), service.getRequests(receiverUsername),
                "Get requests should return correct set of user requesters");
    }

    @Test
    void testAcceptRequestForNoRequest() throws UserAlreadyExistsException {
        String requesterUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        userService.register("George", "Patrick", requesterUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        assertThrows(ValueNotFoundException.class, () -> service.acceptRequest(receiverUsername, requesterUsername));
    }

    @Test
    void testAcceptRequestForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String requesterUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User requester = new User("George", "Patrick", requesterUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", requesterUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        frRepo.add(new FriendRequest(requester, receiver));

        service.acceptRequest(receiverUsername, requesterUsername);

        Collection<User> usersInFriendship = Objects.requireNonNull(
                fRepo.get(f -> f.containsUser(requester) && f.containsUser(receiver))
                        .stream()
                        .findFirst()
                        .get().getUsers());

        Collection<User> usersInChat = Objects.requireNonNull(
                pcRepo.get(f -> f.getUsers().contains(requester) && f.getUsers().contains(receiver))
                        .stream()
                        .findFirst()
                        .get().getUsers());

        assertEquals(Set.of(requester, receiver),
                new HashSet<>(usersInFriendship),
                "Accept request should create friendship");

        assertEquals(Set.of(requester, receiver),
                new HashSet<>(usersInChat),
                "Accept request should create personal chat");

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(service.getRequests(receiverUsername)),
                "Accept request should remove request");
    }

    @Test
    void testDeclineRequestForNoRequest() throws UserAlreadyExistsException {
        String requesterUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        userService.register("George", "Patrick", requesterUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        assertThrows(ValueNotFoundException.class, () -> service.declineRequest(receiverUsername, requesterUsername));
    }

    @Test
    void testDeclineRequestForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String requesterUsername = "g.patrick";
        String receiverUsername = "s.johnson";

        User requester = new User("George", "Patrick", requesterUsername, SHA256.hashPassword("Parola123"));
        User receiver = new User("Steven", "Johnson", receiverUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", requesterUsername, "Parola123");
        userService.register("Steven", "Johnson", receiverUsername, "Parola123");

        frRepo.add(new FriendRequest(requester, receiver));

        service.declineRequest(receiverUsername, requesterUsername);

        assertEquals(Collections.EMPTY_SET,
                new HashSet<>(service.getRequests(receiverUsername)),
                "Accept request should remove request");
    }

}
