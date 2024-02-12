package bg.sofia.uni.fmi.mjt.chatty.client;

import bg.sofia.uni.fmi.mjt.chatty.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
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

public class ChattyClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private static UserDTO user;
    private static Collection<Notification> notifications;
    private static ChatState chatState = ChatState.NOT_IN_CHAT;
    private static String chatRelatedName;

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                String input = scanner.nextLine();

                if ("quit".equals(input)) {
                    break;
                }

                CommandType type = CommandType.of(input.split(" ")[0]);

                if (isLocalCommand(type)) {
                    processLocalCommand(type, input);
                    continue;
                }

                String processedInput = processInputForServer(type, input);

                buffer.clear();
                buffer.put(processedInput.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                processResponse(type, reply);
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private static boolean isLocalCommand(CommandType commandType) {
        return Set.of(
            CommandType.UNKNOWN,
            CommandType.HELP,
            CommandType.LOGOUT,
            CommandType.CLOSE_CHAT
        ).contains(commandType);
    }

    private static void processLocalCommand(CommandType type, String input) {
        switch (type) {
            case UNKNOWN -> System.out.println("Unknown command. Type 'help' to see available commands");
            case HELP -> processHelp();
            case LOGOUT -> processLogout();
            case CLOSE_CHAT -> processCloseChat();
        }
    }

    private static void processHelp() {
        System.out.println("Help");
    }

    private static void processLogout() {
        if (user == null) {
            System.out.println("You are not logged in");
        }

        user = null;
        notifications = null;
        chatState = ChatState.NOT_IN_CHAT;
        chatRelatedName = null;

        System.out.println("Logged out");
    }

    private static void processCloseChat() {
        if (chatState.equals(ChatState.NOT_IN_CHAT)) {
            System.out.println("You are not in chat");
        }

        chatState = ChatState.NOT_IN_CHAT;
        chatRelatedName = null;
    }

    private static String processInputForServer(CommandType type, String input) {
        return user == null ? input : input + " " + user.username();
    }

    private static void processResponse(CommandType type, String reply) {
        switch (type) {
            case REGISTER, ADD_FRIEND, ACCEPT_REQUEST -> System.out.println(reply);
            case LOGIN -> processLogin(reply);
        }
    }

    private static void processLogin(String reply) {
        try {

            SessionDTO session = new Gson().fromJson(reply, SessionDTO.class);
            user = session.user();
            notifications = session.notifications();

            System.out.println("Hello, " + user.fullName() + "\n");
            // TODO: Print notifications
            System.out.println();

        } catch (JsonSyntaxException e) {
            System.out.println(reply);
        }
    }

}