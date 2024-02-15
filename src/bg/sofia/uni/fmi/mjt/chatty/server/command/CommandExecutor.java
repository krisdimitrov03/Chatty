package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.dto.GroupChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.PersonalChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.*;
import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.service.BlockServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.ChatServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.FriendshipServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.NotificationServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.UserServiceAPI;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.stream.Collectors;

public class CommandExecutor {

    public static final String INCORRECT_FORMAT_MESSAGE = "Input is not in correct format";

    private final UserServiceAPI userService;
    private final FriendshipServiceAPI friendshipService;
    private final ChatServiceAPI chatService;
    private final BlockServiceAPI blockService;
    private final NotificationServiceAPI notificationService;

    private final Gson gson;

    public CommandExecutor(
            UserServiceAPI userService,
            FriendshipServiceAPI friendshipService,
            ChatServiceAPI chatService,
            BlockServiceAPI blockService,
            NotificationServiceAPI notificationService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.chatService = chatService;
        this.blockService = blockService;
        this.notificationService = notificationService;

        this.gson = new Gson();
    }

    public String execute(Command cmd) {
        CommandType cmdType = CommandType.of(cmd.command());

        return switch (cmdType) {
            case REGISTER -> register(cmd.arguments());
            case LOGIN -> login(cmd.arguments());
            case ADD_FRIEND -> addFriend(cmd.arguments());
            case REMOVE_FRIEND -> removeFriend(cmd.arguments());
            case CHECK_REQUESTS -> checkRequests(cmd.arguments());
            case ACCEPT_REQUEST -> acceptRequest(cmd.arguments());
            case DECLINE_REQUEST -> declineRequest(cmd.arguments());
            case LIST_FRIENDS -> listFriends(cmd.arguments());
            case BLOCK -> block(cmd.arguments());
            case UNBLOCK -> unblock(cmd.arguments());
            case OPEN_CHAT -> openChat(cmd.arguments());
            case CLOSE_CHAT -> closeChat();
            case SEND_MESSAGE -> sendMessage(cmd.arguments());
            case CREATE_GROUP -> createGroup(cmd.arguments());
            case DELETE_GROUP -> deleteGroup(cmd.arguments());
            case ADD_TO_GROUP -> addToGroup(cmd.arguments());
            case REMOVE_FROM_GROUP -> removeFromGroup(cmd.arguments());
            case OPEN_GROUP -> openGroup(cmd.arguments());
            case CHECK_INBOX -> checkInbox(cmd.arguments());
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
            String json = gson.toJson(result);

            notificationService.removeNotificationsOf(args[0]);

            return json;
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

    private String removeFriend(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            friendshipService.removeFriend(args[1], args[0]);
            return "Friend removed successfully";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String checkRequests(String[] args) {
        if (args.length != 1) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            Collection<UserDTO> requesters = friendshipService.getRequests(args[0]);

            if (requesters.isEmpty()) {
                return "You have no requests at this moment";
            }

            return gson.toJson(requesters);
        } catch (ValueNotFoundException | IllegalArgumentException e) {
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

        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String listFriends(String[] args) {
        if (args.length != 1) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            Collection<UserDTO> friends = friendshipService.getFriendsOf(args[0]);

            if (friends.isEmpty()) {
                return "You have no friends at this moment";
            }

            return gson.toJson(friends);
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String declineRequest(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            friendshipService.declineRequest(args[1], args[0]);
            return "Request declined";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String block(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            blockService.block(args[1], args[0]);
            return args[0] + " blocked";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String unblock(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            blockService.unblock(args[1], args[0]);
            return args[0] + " unblocked";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String openChat(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            PersonalChat chat = chatService.getPersonalChat(args[0], args[1]);
            return gson.toJson(new PersonalChatDTO(args[0], chat.getMessages()));
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String closeChat() {
        return "closed";
    }

    private String sendMessage(String[] args) {
        if (args.length != 4) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            switch (Integer.parseInt(args[3])) {
                case 1 -> chatService.sendPersonalMessage(args[1], args[2], args[0]);
                case 2 -> chatService.sendGroupMessage(args[2], args[1], args[0]);
            }

            return "[" + args[1] + "] " + args[0];
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String createGroup(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            chatService.createGroupChat(args[0], args[1]);
            return "Group chat created with admin " + args[1];
        } catch (ValueNotFoundException | UserAlreadyInGroupException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String deleteGroup(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            chatService.deleteGroupChat(args[0], args[1]);
            return "Group chat deleted";
        } catch (ValueNotFoundException | AccessDeniedException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String addToGroup(String[] args) {
        if (args.length != 3) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {

            chatService.addToGroupChat(args[1], args[2], args[0]);
            return args[0] + " added to " + args[1];

        } catch (ValueNotFoundException |
                 AccessDeniedException |
                 UserAlreadyInGroupException |
                 IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String removeFromGroup(String[] args) {
        if (args.length != 3) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            chatService.removeFromGroupChat(args[1], args[2], args[0]);
            return args[0] + " kicked from " + args[1];
        } catch (ValueNotFoundException | AccessDeniedException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String openGroup(String[] args) {
        if (args.length != 2) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {

            GroupChat chat = chatService.getGroupChat(args[0], args[1]);
            String[] usernames = chat.getUsers().stream().map(User::username).toArray(String[]::new);

            return gson.toJson(new GroupChatDTO(chat.getName(), usernames, chat.getMessages()));

        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String checkInbox(String[] args) {
        if (args.length != 1) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            Collection<Notification> notifications = notificationService.getNotificationsOf(args[0]);
            String json = gson.toJson(notifications);

            notificationService.removeNotificationsOf(args[0]);

            return json;
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

}
