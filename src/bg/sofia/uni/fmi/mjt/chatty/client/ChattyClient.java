package bg.sofia.uni.fmi.mjt.chatty.client;

import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
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

                sendRequest(socketChannel, processInputForServer(input));
                String reply = getResponse(socketChannel);
                processResponse(type, reply);
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

    private boolean isLocalCommand(CommandType commandType) {
        return Set.of(
            CommandType.UNKNOWN,
            CommandType.HELP,
            CommandType.LOGOUT,
            CommandType.CLOSE_CHAT
        ).contains(commandType);
    }

    private void processLocalCommand(CommandType type, String input) {
        switch (type) {
            case UNKNOWN -> System.out.println("Unknown command. Type 'help' to see available commands");
            case HELP -> processHelp();
            case LOGOUT -> processLogout();
            case CLOSE_CHAT -> processCloseChat();
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

    private void processCloseChat() {
        if (chatState.equals(ChatState.NOT_IN_CHAT)) {
            System.out.println("You are not in chat");
            return;
        }

        chatState = ChatState.NOT_IN_CHAT;
        chatRelatedName = null;
    }

    private String processInputForServer(String input) {
        return user == null ? input : input + " " + user.username();
    }

    private void processResponse(CommandType type, String reply) {
        switch (type) {
            case REGISTER,
                ADD_FRIEND,
                REMOVE_FRIEND,
                ACCEPT_REQUEST,
                DECLINE_REQUEST,
                BLOCK,
                UNBLOCK -> System.out.println(reply);
            case LOGIN -> processLogin(reply);
            case CHECK_REQUESTS -> processCheckRequests(reply);
            case LIST_FRIENDS -> processListFriends(reply);
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