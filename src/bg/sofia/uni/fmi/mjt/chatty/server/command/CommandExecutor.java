package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.service.BlockServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.ChatServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.FriendshipServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.UserServiceAPI;
import com.google.gson.Gson;

public class CommandExecutor {

    private static final String INCORRECT_FORMAT_MESSAGE = "Input is not in correct format";

    private final UserServiceAPI userService;
    private final FriendshipServiceAPI friendshipService;
    private final ChatServiceAPI chatService;
    private final BlockServiceAPI blockService;

    public CommandExecutor(
        UserServiceAPI userService,
        FriendshipServiceAPI friendshipService,
        ChatServiceAPI chatService,
        BlockServiceAPI blockService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.chatService = chatService;
        this.blockService = blockService;
    }

    public String execute(Command cmd) {
        CommandType cmdType = CommandType.of(cmd.command());

        return switch (cmdType) {
            case REGISTER -> register(cmd.arguments());
            case LOGIN -> login(cmd.arguments());
            case ADD_FRIEND -> addFriend(cmd.arguments());
            case ACCEPT_REQUEST -> acceptRequest(cmd.arguments());
            default -> "Unknown command";
        };
    }

    private String register(String[] args) {
        if (args.length != 4) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            userService.register(args[0], args[1], args[2], args[3]);

            return "Successful registration";

        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String login(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            SessionDTO result = userService.login(args[0], args[1]);
            return new Gson().toJson(result);
        } catch (IllegalArgumentException | ValueNotFoundException e) {
            return e.getMessage();
        }
    }

    private String addFriend(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            friendshipService.addFriend(args[1], args[0]);
            return "Friend request sent to " + args[0];
        } catch (ValueNotFoundException e) {
            return "No such user exists";
        } catch (UserBlockedException | FriendshipAlreadyExistsException | FriendRequestAlreadySentException e) {
            return e.getMessage();
        }
    }

    private String acceptRequest(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            friendshipService.acceptRequest(args[1], args[0]);
            return "You are now friends with " + args[0];

        } catch (ValueNotFoundException e) {
            return e.getMessage();
        }
    }

}
