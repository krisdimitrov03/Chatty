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
import java.util.Collection;

public class ChattyClient {

    private static UserDTO user;

    private static Collection<Notification> notifications;

    private static boolean isInChat = false;

    private static String draftMessageBuffer = "";

    private static UserServiceAPI userService = UserService.getInstance();
    private static FriendshipServiceAPI friendshipService = FriendshipService.getInstance();

    public static void main(String[] args) throws IOException, ValueNotFoundException, UserBlockedException {
        var result = userService.login("KrisDMT", "Parola123");

        if (result.user().isPresent()) {
            user = result.user().get();
        }

        friendshipService.addFriend(
            userService.getByCriteria(u -> u.username().equals(user.username())).stream().findFirst().get(), "martoK");

        System.out.println("KrisDMT sent friend request to martoK");

        friendshipService.acceptRequest(
            userService.getByCriteria(u -> u.username().equals("martoK")).stream().findFirst().get(), user.username());

        System.out.println("martoK accepted friend request from KrisDMT. They are friends now");

        var friends = friendshipService.getFriendsOf(
            userService.getByCriteria(u -> u.username().equals(user.username())).stream().findFirst().get());

        System.out.println("Friends:");
        friends.forEach(f -> System.out.println(f.username()));

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