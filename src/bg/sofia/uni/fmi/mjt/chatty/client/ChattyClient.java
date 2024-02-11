package bg.sofia.uni.fmi.mjt.chatty.client;

import bg.sofia.uni.fmi.mjt.chatty.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserBlockedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Notification;
import bg.sofia.uni.fmi.mjt.chatty.server.service.FriendshipService;
import bg.sofia.uni.fmi.mjt.chatty.server.service.FriendshipServiceAPI;
import bg.sofia.uni.fmi.mjt.chatty.server.service.UserService;
import bg.sofia.uni.fmi.mjt.chatty.server.service.UserServiceAPI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;

public class ChattyClient {

    private static final int SERVER_PORT = 8000;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static UserDTO user;

    private static Collection<Notification> notifications;

    private static boolean isInChat = false;

    public static void main(String[] args) throws IOException, ValueNotFoundException, UserBlockedException {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                String message = scanner.nextLine();

                if ("quit".equals(message)) {
                    break;
                }

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println(reply);
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}


//        String path = "./src/bg/sofia/uni/fmi/mjt/chatty/server/db/users.dat";
//
//        Collection<User> users = Set.of(
//            new User("Ivan", "Ivanov", "ivan_ii", "63160655"),
//            new User("Kristian", "Dimitrov", "KrisDMT", "63160655"),
//            new User("Martin", "Karbovski", "martoK", "63160655"),
//            new User("Boris", "Kasabov", "bobiK", "63160655")
//        );
//
//        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path))) {
//            stream.writeInt(4);
//
//            users.forEach(u -> {
//                try {
//                    u.saveTo(stream);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }