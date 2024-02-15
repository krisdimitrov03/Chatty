package bg.sofia.uni.fmi.mjt.chatty.client;

import bg.sofia.uni.fmi.mjt.chatty.dto.GroupChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.PersonalChatDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Message;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ChattyClient {

    private static final int SERVER_PORT = 3000;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private final ByteBuffer buffer;
    private final Gson gson;
    private UserDTO user;
    private ChatState chatState;
    private String chatRelatedName;

    public ChattyClient() {
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        chatState = ChatState.NOT_IN_CHAT;
        gson = new Gson();
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                String input = scanner.nextLine();
                if ("quit".equals(input)) {
                    if (!chatState.equals(ChatState.NOT_IN_CHAT)) {
                        System.out.println("Close the chat before quitting.");
                        continue;
                    }

                    break;
                }

                CommandType type = CommandType.of(input.split(" ")[0]);

                if (!checkAuthorization(type)) {
                    continue;
                }

                if (isLocalCommand(type)) {
                    processLocalCommand(type, input);
                    continue;
                }

                if (!checkInChatForSend(type)) {
                    continue;
                }

                if (type.equals(CommandType.CLOSE_CHAT)) {
                    if (chatState.equals(ChatState.NOT_IN_CHAT)) {
                        System.out.println("You are not in chat");
                        continue;
                    }
                }

                sendRequest(socketChannel, processInputForServer(input));

                if (type.equals(CommandType.CLOSE_CHAT)) {
                    processCloseChat();
                }

                if (chatState.equals(ChatState.NOT_IN_CHAT)) {
                    String reply = getResponse(socketChannel);
                    processResponse(type, reply, socketChannel);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private void sendRequest(SocketChannel socketChannel, String input) throws IOException {
        buffer.clear();
        buffer.put(input.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    private String getResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private boolean checkAuthorization(CommandType type) {
        return switch (type) {
            case LOGIN, REGISTER -> {
                if (user != null) {
                    System.out.println("You are already in your account");
                    yield false;
                }
                yield true;
            }
            case HELP, UNKNOWN -> true;
            default -> {
                if (user == null) {
                    System.out.println("You need to log into your account for this command");
                    yield false;
                }
                yield true;
            }
        };
    }

    private boolean checkInChatForSend(CommandType type) {
        if (type.equals(CommandType.SEND_MESSAGE)) {
            if (chatState.equals(ChatState.NOT_IN_CHAT)) {
                System.out.println("You need to be in chat to send message");
                return false;
            }
        }

        return true;
    }

    private boolean isLocalCommand(CommandType commandType) {
        return Set.of(
                CommandType.UNKNOWN,
                CommandType.HELP,
                CommandType.LOGOUT
        ).contains(commandType);
    }

    private void processLocalCommand(CommandType type, String input) {
        switch (type) {
            case UNKNOWN -> System.out.println("Unknown command. Type 'help' to see available commands");
            case HELP -> processHelp();
            case LOGOUT -> processLogout();
        }
    }

    private void processHelp() {
        System.out.println("Help");
    }

    private void processLogout() {
        user = null;
        chatState = ChatState.NOT_IN_CHAT;
        chatRelatedName = null;

        System.out.println("Logged out");
    }

    private String processInputForServer(String input) {
        if (!input.startsWith(CommandType.SEND_MESSAGE.toString()) &&
                !input.startsWith(CommandType.CLOSE_CHAT.toString())) {
            return user == null ? input : input + " " + user.username();
        }

        return input + " " + user.username() + " " + chatRelatedName + " " + chatState.getIntValue();
    }

    private void processResponse(CommandType type, String reply, SocketChannel channel) {
        switch (type) {
            case REGISTER,
                    ADD_FRIEND,
                    REMOVE_FRIEND,
                    ACCEPT_REQUEST,
                    DECLINE_REQUEST,
                    BLOCK,
                    UNBLOCK,
                    CREATE_GROUP,
                    DELETE_GROUP,
                    ADD_TO_GROUP,
                    REMOVE_FROM_GROUP -> System.out.println(reply);
            case LOGIN -> processLogin(reply);
            case CHECK_REQUESTS -> processCheckRequests(reply);
            case LIST_FRIENDS -> processListFriends(reply);
            case OPEN_CHAT -> processOpenChat(reply, channel);
            case CLOSE_CHAT -> processCloseChat();
            case OPEN_GROUP -> processOpenGroup(reply, channel);
            case CHECK_INBOX -> processCheckInbox(reply);
        }
    }

    private void processLogin(String reply) {
        try {
            SessionDTO session = gson.fromJson(reply, SessionDTO.class);
            user = session.user();

            System.out.println("Hello, " + user.fullName() + "\n");
            printNotifications(session.notifications());
            System.out.println();

        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

    private void printNotifications(Collection<Notification> notifications) {
        if (notifications.isEmpty()) {
            System.out.println("You have no notifications");
            return;
        }

        System.out.println("*** Notifications ***");

        Set<String> personalMessages = notifications.stream()
                .filter(n -> n.type().equals(NotificationType.PERSONAL_MESSAGE))
                .map(Notification::content)
                .collect(Collectors.toSet());

        Set<String> groupMessages = notifications.stream()
                .filter(n -> n.type().equals(NotificationType.GROUP_MESSAGE))
                .map(Notification::content)
                .collect(Collectors.toSet());

        Set<String> friendRequests = notifications.stream()
                .filter(n -> n.type().equals(NotificationType.FRIEND_REQUEST))
                .map(Notification::content)
                .collect(Collectors.toSet());

        Set<String> others = notifications.stream()
                .filter(n -> n.type().equals(NotificationType.OTHER))
                .map(Notification::content)
                .collect(Collectors.toSet());

        if (!personalMessages.isEmpty()) {
            System.out.println("* Personal messages:\n" + String.join("\n", personalMessages));
        }

        if (!groupMessages.isEmpty()) {
            System.out.println("* Group messages:\n" + String.join("\n", groupMessages));
        }

        if (!friendRequests.isEmpty()) {
            System.out.println("* Friend requests:\n" + String.join("\n", friendRequests));
        }

        if (!others.isEmpty()) {
            System.out.println("* Other:\n" + String.join("\n", others));
        }
    }

    private void processListFriends(String reply) {
        try {
            UserDTO[] friendsArr = gson.fromJson(reply, UserDTO[].class);
            Collection<UserDTO> friends = Set.of(friendsArr);

            System.out.println("Friends:");
            friends.forEach(f -> System.out.println(f.fullName() + " [" + f.username() + "]"));
        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

    private void processCheckRequests(String reply) {
        try {
            UserDTO[] requestersArr = gson.fromJson(reply, UserDTO[].class);
            Collection<UserDTO> requesters = Set.of(requestersArr);

            System.out.println("Friend requests:");
            requesters.forEach(f -> System.out.println("From " + f.fullName() + " [" + f.username() + "]"));
        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

    private void processOpenChat(String reply, SocketChannel channel) {
        try {
            PersonalChatDTO chat = gson.fromJson(reply, PersonalChatDTO.class);

            chatState = ChatState.PERSONAL;
            chatRelatedName = chat.friend();

            printChat(chat.messages());

            new ChatObserverThread(channel, chatState, buffer).start();
        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

    private void processOpenGroup(String reply, SocketChannel channel) {
        try {
            GroupChatDTO chat = gson.fromJson(reply, GroupChatDTO.class);

            chatState = ChatState.GROUP;
            chatRelatedName = chat.name();

            printChat(chat.messages());

            new ChatObserverThread(channel, chatState, buffer).start();
        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

    private void printChat(SequencedCollection<Message> messages) {
        System.out.println("*** Chat ***");

        if (messages.isEmpty()) {
            System.out.println("There are no messages in this chat yet. Say Hi!");
        } else {
            messages.forEach(m -> System.out.println("[" + m.sender().username() + "] " + m.text()));
        }

        System.out.println();
    }

    private void processCloseChat() {
        chatState = ChatState.NOT_IN_CHAT;
        chatRelatedName = null;
    }

    private void processCheckInbox(String reply) {
        try {
            Notification[] notificationsArr = gson.fromJson(reply, Notification[].class);
            Collection<Notification> notifications = Set.of(notificationsArr);

            printNotifications(notifications);
        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

}