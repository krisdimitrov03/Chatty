package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.GroupChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.PersonalChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.exception.AccessDeniedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyInGroupException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendRequestAlreadySentException;
import bg.sofia.uni.fmi.mjt.chatty.exception.FriendshipAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyExistsException;
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

public class CommandExecutor {

    public static final int CHAT_STATE_ARG_INDEX = 3;
    public static final int PASSWORD_ARG_INDEX = 3;
    public static final int ONE_ARG_MAX_ALLOWED_ARG_COUNT = 1;
    public static final int TWO_ARG_MAX_ALLOWED_ARG_COUNT = 2;
    public static final int THREE_ARG_MAX_ALLOWED_ARG_COUNT = 3;
    public static final int FOUR_ARG_MAX_ALLOWED_ARG_COUNT = 4;
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
            case LIST_BLOCKED -> listBlocked(cmd.arguments());
            case OPEN_CHAT -> openChat(cmd.arguments());
            case CLOSE_CHAT -> closeChat();
            case SEND_MESSAGE -> sendMessage(cmd.arguments());
            case CREATE_GROUP -> createGroup(cmd.arguments());
            case DELETE_GROUP -> deleteGroup(cmd.arguments());
            case ADD_TO_GROUP -> addToGroup(cmd.arguments());
            case REMOVE_FROM_GROUP -> removeFromGroup(cmd.arguments());
            case LEAVE_GROUP -> leaveGroup(cmd.arguments());
            case OPEN_GROUP -> openGroup(cmd.arguments());
            case LIST_GROUPS -> listGroups(cmd.arguments());
            case CHECK_INBOX -> checkInbox(cmd.arguments());
            default -> "Unknown command";
        };
    }

    private String register(String[] args) {
        if (args.length != FOUR_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String firstName = args[0];
            String lastName = args[1];
            String username = args[2];
            String password = args[PASSWORD_ARG_INDEX];

            userService.register(firstName, lastName, username, password);

            return "Successful registration";

        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String login(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];
            String password = args[1];

            SessionDTO result = userService.login(username, password);
            String json = gson.toJson(result);

            notificationService.removeNotificationsOf(username);

            return json;
        } catch (IllegalArgumentException | ValueNotFoundException e) {
            return e.getMessage();
        }
    }

    private String addFriend(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String sender = args[1];
            String target = args[0];

            friendshipService.addFriend(sender, target);
            return "Friend request sent to " + target;
        } catch (ValueNotFoundException e) {
            return "No such user exists";
        } catch (UserBlockedException | FriendshipAlreadyExistsException | FriendRequestAlreadySentException e) {
            return e.getMessage();
        }
    }

    private String removeFriend(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String remover = args[1];
            String target = args[0];

            friendshipService.removeFriend(remover, target);
            return "Friend removed successfully";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String checkRequests(String[] args) {
        if (args.length != ONE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];

            Collection<UserDTO> requesters = friendshipService.getRequests(username);

            if (requesters.isEmpty()) {
                return "You have no requests at this moment";
            }

            return gson.toJson(requesters);
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String acceptRequest(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String acceptor = args[1];
            String target = args[0];

            friendshipService.acceptRequest(acceptor, target);
            return "You are now friends with " + target;

        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String listFriends(String[] args) {
        if (args.length != ONE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];

            Collection<UserDTO> friends = friendshipService.getFriendsOf(username);

            if (friends.isEmpty()) {
                return "You have no friends at this moment";
            }

            return gson.toJson(friends);
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String declineRequest(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String decliner = args[1];
            String target = args[0];

            friendshipService.declineRequest(decliner, target);
            return "Request declined";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String block(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String blocker = args[1];
            String blocked = args[0];

            blockService.block(blocker, blocked);
            return blocked + " blocked";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String unblock(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String unblocker = args[1];
            String unblocked = args[0];

            blockService.unblock(unblocker, unblocked);
            return unblocked + " unblocked";
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String listBlocked(String[] args) {
        if (args.length != ONE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];

            Collection<UserDTO> blockedUsers = blockService.getBlockedBy(username);

            if (blockedUsers.isEmpty()) {
                return "You haven't blocked anybody";
            }

            return gson.toJson(blockedUsers);
        } catch (ValueNotFoundException e) {
            return e.getMessage();
        }
    }

    private String openChat(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String left = args[0];
            String right = args[1];

            PersonalChat chat = chatService.getPersonalChat(left, right);
            return gson.toJson(new PersonalChatDTO(left, chat.getMessages()));
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String closeChat() {
        return "closed";
    }

    private String sendMessage(String[] args) {
        if (args.length != FOUR_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String message = args[0];
            String sender = args[1];
            String target = args[2];
            String chatState = args[CHAT_STATE_ARG_INDEX];

            switch (Integer.parseInt(chatState)) {
                case 1 -> chatService.sendPersonalMessage(sender, target, message);
                case 2 -> chatService.sendGroupMessage(target, sender, message);
            }

            return "[" + args[1] + "] " + args[0];
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String createGroup(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[0];
            String username = args[1];

            chatService.createGroupChat(groupName, username);
            return "Group chat created with admin " + username;
        } catch (ValueNotFoundException | UserAlreadyInGroupException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String deleteGroup(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[0];
            String username = args[1];

            chatService.deleteGroupChat(groupName, username);
            return "Group chat deleted";
        } catch (ValueNotFoundException | AccessDeniedException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String addToGroup(String[] args) {
        if (args.length != THREE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[1];
            String adder = args[2];
            String added = args[0];

            chatService.addToGroupChat(groupName, adder, added);
            return added + " added to " + groupName;

        } catch (ValueNotFoundException |
                 AccessDeniedException |
                 UserAlreadyInGroupException |
                 IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String removeFromGroup(String[] args) {
        if (args.length != THREE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[1];
            String remover = args[2];
            String removed = args[0];

            chatService.removeFromGroupChat(groupName, remover, removed);
            return removed + " kicked from " + groupName;
        } catch (ValueNotFoundException | AccessDeniedException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String leaveGroup(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[0];
            String username = args[1];

            chatService.leaveGroupChat(groupName, username);
            return "You left from " + groupName;
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String openGroup(String[] args) {
        if (args.length != TWO_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String groupName = args[0];
            String username = args[1];

            GroupChat chat = chatService.getGroupChat(groupName, username);
            String[] usernames = chat.getUsers().stream().map(User::username).toArray(String[]::new);

            return gson.toJson(new GroupChatDTO(chat.getName(), usernames, chat.getMessages()));
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String listGroups(String[] args) {
        if (args.length != ONE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];

            Collection<String> groups = chatService.getGroupChatsForUser(username);

            if (groups.isEmpty()) {
                return "You are not part of any group chats";
            }

            return gson.toJson(groups);
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String checkInbox(String[] args) {
        if (args.length != ONE_ARG_MAX_ALLOWED_ARG_COUNT) {
            return INCORRECT_FORMAT_MESSAGE;
        }

        try {
            String username = args[0];

            Collection<Notification> notifications = notificationService.getNotificationsOf(username);
            String json = gson.toJson(notifications);

            notificationService.removeNotificationsOf(username);

            return json;
        } catch (ValueNotFoundException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

}
