package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.*;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.*;
import bg.sofia.uni.fmi.mjt.chatty.server.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommandExecutorTest {

    private static Repository<Friendship> fRepo;
    private static Repository<FriendRequest> frRepo;
    private static Repository<Block> bRepo;
    private static Repository<Notification> nRepo;
    private static Repository<User> userRepo;
    private static Repository<PersonalChat> pcRepo;
    private static Repository<GroupChat> gcRepo;

    private static CommandExecutor executor;

    @BeforeAll
    static void setupTests() {
        fRepo = FriendshipRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        frRepo = FriendRequestRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        bRepo = BlockRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        userRepo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        nRepo = NotificationRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        pcRepo = PersonalChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        gcRepo = GroupChatRepository.getInstance(new ByteArrayInputStream("".getBytes()));

        executor = new CommandExecutor(
                UserService.getInstance(),
                FriendshipService.getInstance(),
                ChatService.getInstance(),
                BlockService.getInstance(),
                NotificationService.getInstance()
        );
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
    void testExecuteRegisterWithIncorrectArgsCount() {
        String input = "register";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Register should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRegisterWithIncorrectData() {
        String input = "register Name34 lastName3 username1 Parola123";

        String unexpected = "Successful registration";

        String message = "Register should show if data is incorrect";

        assertNotEquals(unexpected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRegisterWithCorrectData() {
        String input = "register Gorge Peterson g.peterson Parola123";

        String expected = "Successful registration";

        String message = "Register should show if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLoginWithIncorrectArgsCount() {
        String input = "login";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Login should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLoginWithIncorrectData() {
        String input = "login Name34 Parola123";

        String expected = "Incorrect username or password";

        String message = "Login should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLoginWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));

        String input = "login g.peterson Parola123";

        String expected = "{\"user\":{\"fullName\":\"Gorge Peterson\",\"username\":\"g.peterson\"},\"notifications\":[]}";

        String message = "Login get session if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddFriendWithIncorrectArgsCount() {
        String input = "add-friend";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Add friend should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddFriendWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "add-friend g.peterson s.peterson";

        String expected = "You have already sent friend request to g.peterson";

        String message = "Add friend should show if request is already sent";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddFriendWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        String input = "add-friend g.peterson s.peterson";

        String expected = "Friend request sent to g.peterson";

        String message = "Add friend should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFriendWithIncorrectArgsCount() {
        String input = "remove-friend";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Remove friend should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFriendWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "remove-friend g.peterson a.peterson";

        String expected = "User not found";

        String message = "Remove friend should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFriendWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "remove-friend g.peterson s.peterson";

        String expected = "Friend removed successfully";

        String message = "Remove friend should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCheckRequestsWithIncorrectArgsCount() {
        String input = "check-requests";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Check requests should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCheckRequestsWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "check-requests g.peterson";

        String expected = "[{\"fullName\":\"Steven Peterson\",\"username\":\"s.peterson\"}]";

        String message = "Check requests should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAcceptRequestWithIncorrectArgsCount() {
        String input = "accept";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Accept friend should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAcceptRequestWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "accept s.pseterson g.peterson";

        String expected = "User not found";

        String message = "Accept friend should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAcceptRequestWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "accept s.peterson g.peterson";

        String expected = "You are now friends with s.peterson";

        String message = "Accept friend should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeclineRequestWithIncorrectArgsCount() {
        String input = "decline";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Decline friend should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeclineRequestWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "decline s.pseterson g.peterson";

        String expected = "User not found";

        String message = "Decline friend should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeclineRequestWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));

        String input = "decline s.peterson g.peterson";

        String expected = "Request declined";

        String message = "Decline friend should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListFriendsWithIncorrectArgsCount() {
        String input = "list-friends";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "List friends friend should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListFriendsWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "list-friends g.peterson";

        String expected = "[{\"fullName\":\"Steven Peterson\",\"username\":\"s.peterson\"}]";

        String message = "Decline friend should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteBlockWithIncorrectArgsCount() {
        String input = "block";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Block should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteBlockWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "block s.pseterson g.peterson";

        String expected = "User not found";

        String message = "Block should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteBlockWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "block s.peterson g.peterson";

        String expected = "s.peterson blocked";

        String message = "Block should return proper message if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteUnblockWithIncorrectArgsCount() {
        String input = "unblock";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Unblock should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteUnblockWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));
        executor.execute(CommandCreator.newCommand("block s.peterson g.peterson"));

        String input = "unblock s.pesterson g.peterson";

        String expected = "User not found";

        String message = "Unblock should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteUnblockWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));
        executor.execute(CommandCreator.newCommand("block s.peterson g.peterson"));

        String input = "unblock s.peterson g.peterson";

        String expected = "s.peterson unblocked";

        String message = "Unblock should show if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListBlockedWithIncorrectArgsCount() {
        String input = "list-blocked";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "List blocked should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListBlockedWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));
        executor.execute(CommandCreator.newCommand("block s.peterson g.peterson"));

        String input = "list-blocked g.peterson";

        String expected = "[{\"fullName\":\"Steven Peterson\",\"username\":\"s.peterson\"}]";

        String message = "List blocked should show if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenChatWithIncorrectArgsCount() {
        String input = "open-chat";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Open chat should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenChatWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "open-chat s.pesterson g.peterson";

        String expected = "User not found";

        String message = "Open chat should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenChatWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "open-chat s.peterson g.peterson";

        String expected = "{\"friend\":\"s.peterson\",\"messages\":[]}";

        String message = "Unblock should show if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteSendMessageWithIncorrectArgsCount() {
        String input = "send \"dsa\" dsa";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Send should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteSendMessageWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "send \"dsadsas\" g.peterson dsadas 1";

        String expected = "User not found";

        String message = "Send should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteSendMessageWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));

        executor.execute(CommandCreator.newCommand("add-friend g.peterson s.peterson"));
        executor.execute(CommandCreator.newCommand("accept s.peterson g.peterson"));

        String input = "send \"dsadsas\" g.peterson s.peterson 1";

        String expected = "[g.peterson] dsadsas";

        String message = "Send should show if data is correct";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCreateGroupWithIncorrectArgsCount() {
        String input = "create-group";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Create group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCreateGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));

        String input = "create-group chat-name g.peterson";

        String expected = "Group chat created with admin g.peterson";

        String message = "Create group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeleteGroupWithIncorrectArgsCount() {
        String input = "delete-group";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Delete group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeleteGroupWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "delete-group chat-namde g.peterson";

        String expected = "Group chat does not exist";

        String message = "Delete group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteDeleteGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "delete-group chat-name g.peterson";

        String expected = "Group chat deleted";

        String message = "Delete group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddToGroupWithIncorrectArgsCount() {
        String input = "add-to-group";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Add to group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddToGroupWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "add-to-group chat-name g.peterson adas";

        String expected = "User not found";

        String message = "Add to group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteAddToGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "add-to-group s.peterson chat-name g.peterson";

        String expected = "s.peterson added to chat-name";

        String message = "Add to group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFromGroupWithIncorrectArgsCount() {
        String input = "remove-from-group";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Remove from group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFromGroupWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group chat-name g.peterson adas"));

        String input = "remove-from-group g.peterson chat-name adas";

        String expected = "User not found";

        String message = "Remove from group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteRemoveFromGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group chat-name g.peterson adas"));

        String input = "remove-from-group chat-name s.peterson g.peterson";

        String expected = "User not found";

        String message = "Remove from group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLeaveGroupWithIncorrectArgsCount() {
        String input = "leave";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Leave group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLeaveGroupWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group chat-name g.peterson adas"));

        String input = "leave g.peterson chat-name";

        String expected = "User not found";

        String message = "Leave group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteLeaveGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group chat-name g.peterson adas"));

        String input = "leave chat-name g.peterson";

        String expected = "You left from chat-name";

        String message = "Leave group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenGroupWithIncorrectArgsCount() {
        String input = "open-group";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Open group should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenGroupWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "open-group g.peterson chat-name";

        String expected = "User not found";

        String message = "Open group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteOpenGroupWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group chat-name g.peterson adas"));

        String input = "open-group chat-name g.peterson";

        String expected = "{\"name\":\"chat-name\",\"users\":[\"g.peterson\"],\"messages\":[]}";

        String message = "Open group should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListGroupsWithIncorrectArgsCount() {
        String input = "list-groups";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "List groups should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteListGroupsWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "list-groups g.peterson";

        String expected = "[\"chat-name\"]";

        String message = "List groups should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCheckInboxWithIncorrectArgsCount() {
        String input = "check-inbox";

        String expected = CommandExecutor.INCORRECT_FORMAT_MESSAGE;

        String message = "Check inbox should show if args count is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCheckInboxWithIncorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));

        String input = "check-inbox user23424";

        String expected = "[]";

        String message = "Check inbox should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

    @Test
    void testExecuteCheckInboxWithCorrectData() {
        executor.execute(CommandCreator.newCommand("register Gorge Peterson g.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("register Steven Peterson s.peterson Parola123"));
        executor.execute(CommandCreator.newCommand("create-group chat-name g.peterson"));
        executor.execute(CommandCreator.newCommand("add-to-group s.peterson chat-name g.peterson"));

        String input = "check-inbox s.peterson";

        String expected = "[{\"user\":{\"firstName\":\"Steven\",\"lastName\":\"Peterson\",\"username\":\"s.peterson\",\"passwordHash\":\"3a7306a7751a1079497609b718251c4a4d76a375f3d893280f1e50db6cbaf5a8\"},\"type\":\"OTHER\",\"content\":\"g.peterson added you to group chat-name\"}]";

        String message = "Check inbox should show if data is incorrect";

        assertEquals(expected, executor.execute(CommandCreator.newCommand(input)), message);
    }

}
