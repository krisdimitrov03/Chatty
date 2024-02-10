package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.PersonalChatRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Set;
import java.util.function.Predicate;

public class ChatService implements ChatServiceAPI {

    private static ChatServiceAPI instance;

    private ChatService() {
    }

    public static ChatServiceAPI getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }

        return instance;
    }

    @Override
    public PersonalChat getPersonalChat(String left, String right) throws ValueNotFoundException {
        Guard.isNotNull(left);
        Guard.isNotNull(right);

        User leftUser = UserService.getInstance().ensureUserExists(left);
        User rightUser = UserService.getInstance().ensureUserExists(right);

        FriendshipService.getInstance().ensureFriendshipExists(leftUser, rightUser);

        return PersonalChatRepository.getInstance().get(p -> p.getUsers().containsAll(Set.of(leftUser, rightUser)))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ValueNotFoundException("Personal chat not available"));
    }

    @Override
    public void deletePersonalChat(String left, String right) throws ValueNotFoundException {
        Guard.isNotNull(left);
        Guard.isNotNull(right);

        User leftUser = UserService.getInstance().ensureUserExists(left);
        User rightUser = UserService.getInstance().ensureUserExists(right);

        Predicate<PersonalChat> criteria = c -> c.getUsers().containsAll(Set.of(leftUser, rightUser));

        if (!PersonalChatRepository.getInstance().contains(criteria)) {
            throw new ValueNotFoundException("Personal chat not available");
        }

        PersonalChatRepository.getInstance().remove(criteria);
    }

    @Override
    public void sendPersonalMessage(String sender, String reciever, String text) throws ValueNotFoundException {
        Guard.isNotNull(sender);
        Guard.isNotNull(reciever);

        User leftUser = UserService.getInstance().ensureUserExists(sender);
        User rightUser = UserService.getInstance().ensureUserExists(reciever);

        // TODO: Finish the method
    }

    @Override
    public void createGroupChat(String name, String username) {
        
    }

    @Override
    public void deleteGroupChat(String name, String deleter) {

    }

    @Override
    public GroupChat getGroupChat(String name) {
        return null;
    }

    @Override
    public void addToGroupChat(String name, String adder, String added) {

    }

    @Override
    public void removeFromGroupChat(String name, String remover, String removed) {

    }

    @Override
    public void sendGroupMessage(String chatName, String sender, String text) {

    }

}
