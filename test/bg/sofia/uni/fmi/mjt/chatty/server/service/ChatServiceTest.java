package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.AccessDeniedException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyInGroupException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.*;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.*;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest {

    private static Repository<Friendship> fRepo;
    private static Repository<FriendRequest> frRepo;
    private static Repository<Block> bRepo;
    private static Repository<Notification> nRepo;
    private static Repository<User> userRepo;
    private static Repository<PersonalChat> pcRepo;
    private static Repository<GroupChat> gcRepo;

    private static ChatServiceAPI service;
    private static UserServiceAPI userService;

    @BeforeAll
    static void setupTests() {
        fRepo = FriendshipRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        frRepo = FriendRequestRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        bRepo = BlockRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        userRepo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        nRepo = NotificationRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        pcRepo = PersonalChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        gcRepo = GroupChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));

        service = ChatService.getInstance();
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
        try {
            gcRepo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
    }

    @Test
    void testGetPersonalChatWithNoChat() throws UserAlreadyExistsException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        fRepo.add(new Friendship(left, right));

        assertThrows(ValueNotFoundException.class, () -> service.getPersonalChat(leftUsername, rightUsername),
                "Get chat should throw in case of no chat");
    }

    @Test
    void testGetPersonalChatForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        PersonalChat chat = new PersonalChat(left, right,
                List.of(new Message(left, "Hi"), new Message(right, "Hi to you")));

        fRepo.add(new Friendship(left, right));
        pcRepo.add(chat);

        assertEquals(chat, service.getPersonalChat(leftUsername, rightUsername),
                "Get chat should return proper chat in case of correct input data");
    }

    @Test
    void testDeletePersonalChatWithNoChat() throws UserAlreadyExistsException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        fRepo.add(new Friendship(left, right));

        assertThrows(ValueNotFoundException.class, () -> service.deletePersonalChat(leftUsername, rightUsername),
                "Delete should throw in case of no chat");
    }

    @Test
    void testDeletePersonalChatForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        PersonalChat chat = new PersonalChat(left, right,
                List.of(new Message(left, "Hi"), new Message(right, "Hi to you")));

        fRepo.add(new Friendship(left, right));
        pcRepo.add(chat);

        service.deletePersonalChat(leftUsername, rightUsername);

        assertFalse(pcRepo.contains(chat), "Delete should have proper result in case of correct input data");
    }

    @Test
    void testSendPersonalMessageWithNoChat() throws UserAlreadyExistsException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        fRepo.add(new Friendship(left, right));

        assertThrows(ValueNotFoundException.class,
                () -> service.sendPersonalMessage(leftUsername, rightUsername, "Some message"),
                "Send should throw in case of incorrect data");
    }

    @Test
    void testSendPersonalMessageForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String leftUsername = "g.patrick";
        String rightUsername = "s.johnson";

        User left = new User("George", "Patrick", leftUsername, SHA256.hashPassword("Parola123"));
        User right = new User("Steven", "Johnson", rightUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", leftUsername, "Parola123");
        userService.register("Steven", "Johnson", rightUsername, "Parola123");

        PersonalChat chat = new PersonalChat(left, right,
                new LinkedList<>(List.of(new Message(left, "Hi"), new Message(right, "Hi to you"))));

        fRepo.add(new Friendship(left, right));
        pcRepo.add(chat);

        service.sendPersonalMessage(leftUsername, rightUsername, "Some message");

        assertEquals(3, chat.getMessages().size(), "Send should add message to the chat");
    }

    @Test
    void testCreateGroupChatWithGroupAlreadyExisting() throws UserAlreadyExistsException {
        String username = "g.patrick";

        User user = new User("George", "Patrick", username, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", username, "Parola123");

        gcRepo.add(new GroupChat("chat-name", user));

        assertThrows(UserAlreadyInGroupException.class, () -> service.createGroupChat("chat-name", username),
                "Create should throw if group exists for user");
    }

    @Test
    void testCreateGroupChatForCorrectResult()
            throws UserAlreadyExistsException, ValueNotFoundException, UserAlreadyInGroupException {
        String username = "g.patrick";

        userService.register("George", "Patrick", username, "Parola123");

        service.createGroupChat("chat-name", username);

        assertTrue(gcRepo.contains(c -> c.getName().equals("chat-name")), "Create group should add group chat");
    }

    @Test
    void testDeleteGroupChatWithNoGroup() throws UserAlreadyExistsException {
        String username = "g.patrick";

        User user = new User("George", "Patrick", username, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", username, "Parola123");

        assertThrows(ValueNotFoundException.class, () -> service.deleteGroupChat("chat-name", username),
                "Delete should throw if group does not exist");
    }

    @Test
    void testDeleteGroupChatForCorrectResult()
            throws UserAlreadyExistsException, ValueNotFoundException, UserAlreadyInGroupException, AccessDeniedException {
        String username = "g.patrick";

        User user = new User("George", "Patrick", username, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", username, "Parola123");

        gcRepo.add(new GroupChat("chat-name", user));

        service.deleteGroupChat("chat-name", username);

        assertFalse(gcRepo.contains(c -> c.getName().equals("chat-name")), "Delete should remove group chat");
    }

    @Test
    void testGetGroupChatWithNoGroup() throws UserAlreadyExistsException {
        String username = "g.patrick";

        userService.register("George", "Patrick", username, "Parola123");

        assertThrows(ValueNotFoundException.class, () -> service.getGroupChat("chat-name", username),
                "Get should throw if group does not exist");
    }

    @Test
    void testGetGroupChatForCorrectResult()
            throws UserAlreadyExistsException, ValueNotFoundException, UserAlreadyInGroupException, AccessDeniedException {
        String username = "g.patrick";

        User user = new User("George", "Patrick", username, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", username, "Parola123");

        GroupChat chat = new GroupChat("chat-name", user);

        gcRepo.add(chat);

        assertEquals(chat, service.getGroupChat("chat-name", username),
                "Get should return proper group chat");
    }

    @Test
    void testGetGroupChatsForUserForCorrectResult()
            throws UserAlreadyExistsException, ValueNotFoundException, UserAlreadyInGroupException, AccessDeniedException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);

        chat.addUser(secondUser);

        gcRepo.add(chat);

        assertEquals(Set.of("chat-name"), service.getGroupChatsForUser(secondUsername),
                "Get for user should return proper group chats");
    }

    @Test
    void testAddToGroupChatWithNoChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        assertThrows(ValueNotFoundException.class,
                () -> service.addToGroupChat("chat-name", firstUsername, secondUsername),
                "Add to group should throw if group does not exist");
    }

    @Test
    void testAddToGroupChatWithUserNotAdmin() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";
        String thirdUsername = "p.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");
        userService.register("Peter", "Patrick", thirdUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        chat.addUser(secondUser);

        gcRepo.add(chat);

        assertThrows(AccessDeniedException.class,
                () -> service.addToGroupChat("chat-name", secondUsername, thirdUsername),
                "Add to group should throw if adder is not admin");
    }

    @Test
    void testAddToGroupChatWithUserAlreadyIn() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        chat.addUser(secondUser);

        gcRepo.add(chat);

        assertThrows(UserAlreadyInGroupException.class,
                () -> service.addToGroupChat("chat-name", firstUsername, secondUsername),
                "Add to group should throw if added is already in");
    }

    @Test
    void testAddToGroupChatForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException,
            AccessDeniedException, UserAlreadyInGroupException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);

        gcRepo.add(chat);

        service.addToGroupChat("chat-name", firstUsername, secondUsername);

        assertTrue(chat.getUsers().contains(secondUser),
                "Add to group should return proper result for correct data");
    }

    @Test
    void testRemoveFromGroupChatWithNoChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        assertThrows(ValueNotFoundException.class,
                () -> service.removeFromGroupChat("chat-name", firstUsername, secondUsername),
                "Remove from group should throw if group does not exist");
    }

    @Test
    void testRemoveFromGroupChatWithUserNotAdmin() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";
        String thirdUsername = "p.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));
        User thirdUser = new User("Pater", "Patrick", thirdUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");
        userService.register("Peter", "Patrick", thirdUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        chat.addUser(secondUser);
        chat.addUser(thirdUser);

        gcRepo.add(chat);

        assertThrows(AccessDeniedException.class,
                () -> service.removeFromGroupChat("chat-name", secondUsername, thirdUsername),
                "Remove from group should throw if adder is not admin");
    }

    @Test
    void testRemoveFromGroupChatWithUserNotInChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        gcRepo.add(chat);

        assertThrows(ValueNotFoundException.class,
                () -> service.removeFromGroupChat("chat-name", firstUsername, secondUsername),
                "Remove from group should throw if removed user is not in the chat");
    }

    @Test
    void testRemoveFromGroupChatForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException, AccessDeniedException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);

        gcRepo.add(chat);
        chat.addUser(secondUser);

        service.removeFromGroupChat("chat-name", firstUsername, secondUsername);

        assertFalse(chat.getUsers().contains(secondUser),
                "Remove from group should return proper result for correct data");
    }

    @Test
    void testLeaveFromChatWithNoChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        assertThrows(ValueNotFoundException.class,
                () -> service.leaveGroupChat("chat-name", firstUsername),
                "Leave chat should throw if chat does not exist");
    }

    @Test
    void testLeaveFromChatWithUserNotInChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);

        gcRepo.add(chat);

        assertThrows(ValueNotFoundException.class,
                () -> service.leaveGroupChat("chat-name", secondUsername),
                "Leave chat should throw if user not in chat");
    }

    @Test
    void testLeaveFromChatForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));
        User secondUser = new User("Steven", "Patrick", secondUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        chat.addUser(secondUser);

        gcRepo.add(chat);

        service.leaveGroupChat("chat-name", secondUsername);

        assertFalse(chat.getUsers().contains(secondUser));
    }

    @Test
    void testSendGroupMessageWithNoChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        assertThrows(ValueNotFoundException.class,
                () -> service.sendGroupMessage("chat-name", firstUsername, "Some message"),
                "Send to group should throw if group does not exist");
    }

    @Test
    void testSendGroupMessageWithUserNotInChat() throws UserAlreadyExistsException {
        String firstUsername = "g.patrick";
        String secondUsername = "s.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");
        userService.register("Steven", "Patrick", secondUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        gcRepo.add(chat);

        assertThrows(ValueNotFoundException.class,
                () -> service.sendGroupMessage("chat-name", secondUsername, "Some message"),
                "Send to group should throw if user not in chat");
    }

    @Test
    void testSendGroupMessageForCorrectResult() throws UserAlreadyExistsException, ValueNotFoundException {
        String firstUsername = "g.patrick";

        User firstUser = new User("George", "Patrick", firstUsername, SHA256.hashPassword("Parola123"));

        userService.register("George", "Patrick", firstUsername, "Parola123");

        GroupChat chat = new GroupChat("chat-name", firstUser);
        gcRepo.add(chat);

        service.sendGroupMessage("chat-name", firstUsername, "Some message");

        assertTrue(chat.getMessages().contains(new Message(firstUser, "Some message")),
                "Send message should add the message to the chat");
    }

}
