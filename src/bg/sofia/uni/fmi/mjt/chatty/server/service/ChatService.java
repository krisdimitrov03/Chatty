package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.exception.AccessDeniedException;
import bg.sofia.uni.fmi.mjt.chatty.exception.UserAlreadyInGroupException;
import bg.sofia.uni.fmi.mjt.chatty.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.Message;
import bg.sofia.uni.fmi.mjt.chatty.server.model.NotificationType;
import bg.sofia.uni.fmi.mjt.chatty.server.model.PersonalChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.GroupChat;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.GroupChatRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.PersonalChatRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        return PersonalChatRepository.getInstance()
                .get(p -> p.getUsers().containsAll(Set.of(leftUser, rightUser)))
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

        User senderUser = UserService.getInstance().ensureUserExists(sender);
        User recieverUser = UserService.getInstance().ensureUserExists(reciever);

        ensurePersonalChatExists(senderUser, recieverUser).addMessage(new Message(senderUser, text));
        PersonalChatRepository.getInstance().saveEntities();

        NotificationService.getInstance()
                .addNotification(recieverUser, NotificationType.PERSONAL_MESSAGE, "[" + sender + "] " + text);
    }

    @Override
    public void createGroupChat(String name, String username)
            throws ValueNotFoundException, UserAlreadyInGroupException {

        Guard.isNotNull(name);
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);

        ensureNoGroupChatForUser(name, user);

        GroupChatRepository.getInstance().add(new GroupChat(name, user));

    }

    @Override
    public void deleteGroupChat(String name, String deleter) throws ValueNotFoundException, AccessDeniedException {
        Guard.isNotNull(name);
        Guard.isNotNull(deleter);

        User deleterUser = UserService.getInstance().ensureUserExists(deleter);
        GroupChat chat = ensureGroupChatExists(name);

        ensureUserIsAdmin(deleterUser, chat);

        GroupChatRepository.getInstance().remove(chat);
    }

    @Override
    public GroupChat getGroupChat(String name, String username) throws ValueNotFoundException {
        Guard.isNotNull(name);
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);
        GroupChat chat = ensureGroupChatExists(name);
        ensureUserInGroupChat(chat, user);

        return chat;
    }

    @Override
    public Collection<String> getGroupChatsForUser(String username) throws ValueNotFoundException {
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);

        return GroupChatRepository.getInstance()
                .get(c -> c.getUsers().contains(user))
                .stream()
                .map(GroupChat::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public void addToGroupChat(String name, String adder, String added)
            throws ValueNotFoundException, AccessDeniedException, UserAlreadyInGroupException {

        Guard.isNotNull(name);
        Guard.isNotNull(adder);
        Guard.isNotNull(added);

        User adderUser = UserService.getInstance().ensureUserExists(adder);
        User addedUser = UserService.getInstance().ensureUserExists(added);
        GroupChat chat = ensureGroupChatExists(name);

        ensureUserIsAdmin(adderUser, chat);
        ensureUserNotInGroupChat(chat, addedUser);

        GroupChatRepository.getInstance()
                .get(c -> c.equals(chat))
                .stream()
                .findFirst()
                .ifPresent(c -> c.addUser(addedUser));

        GroupChatRepository.getInstance().saveEntities();

        NotificationService.getInstance()
                .addNotification(addedUser, NotificationType.OTHER, adder + " added you to group " + name);
    }

    @Override
    public void removeFromGroupChat(String name, String remover, String removed)
            throws ValueNotFoundException, AccessDeniedException {

        Guard.isNotNull(name);
        Guard.isNotNull(remover);
        Guard.isNotNull(removed);

        User removerUser = UserService.getInstance().ensureUserExists(remover);
        User removedUser = UserService.getInstance().ensureUserExists(removed);
        GroupChat chat = ensureGroupChatExists(name);

        ensureUserIsAdmin(removerUser, chat);
        ensureUserInGroupChat(chat, removedUser);

        GroupChatRepository.getInstance()
                .get(c -> c.equals(chat))
                .stream()
                .findFirst()
                .ifPresent(c -> c.removeUser(removedUser));

        GroupChatRepository.getInstance().saveEntities();

        NotificationService.getInstance()
                .addNotification(removedUser, NotificationType.OTHER, "You have been kicked from " + name);

    }

    @Override
    public void leaveGroupChat(String name, String username) throws ValueNotFoundException {
        Guard.isNotNull(name);
        Guard.isNotNull(username);

        User user = UserService.getInstance().ensureUserExists(username);
        GroupChat chat = ensureGroupChatExists(name);

        ensureUserInGroupChat(chat, user);

        chat.removeUser(user);
        GroupChatRepository.getInstance().saveEntities();
    }

    @Override
    public void sendGroupMessage(String chatName, String sender, String text) throws ValueNotFoundException {
        Guard.isNotNull(chatName);
        Guard.isNotNull(sender);
        Guard.isNotNull(text);

        User senderUser = UserService.getInstance().ensureUserExists(sender);
        GroupChat chat = ensureGroupChatExists(chatName);

        ensureUserInGroupChat(chat, senderUser);

        chat.addMessage(new Message(senderUser, text));
        GroupChatRepository.getInstance().saveEntities();

        var receivers = chat.getUsers()
                .stream()
                .filter(u -> !u.equals(senderUser))
                .collect(Collectors.toSet());

        receivers.forEach(r -> NotificationService.getInstance()
                .addNotification(r, NotificationType.GROUP_MESSAGE, chatName + " -> [" + sender + "] " + text));
    }

    private PersonalChat ensurePersonalChatExists(User left, User right) throws ValueNotFoundException {
        return PersonalChatRepository.getInstance()
                .get(p -> p.getUsers().containsAll(Set.of(left, right)))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValueNotFoundException("Personal chat does not exist"));
    }

    private GroupChat ensureGroupChatExists(String name) throws ValueNotFoundException {
        return GroupChatRepository.getInstance()
                .get(c -> c.getName().equals(name))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValueNotFoundException("Group chat does not exist"));
    }

    private void ensureUserInGroupChat(GroupChat group, User user) throws ValueNotFoundException {
        if (!group.getUsers().contains(user)) {
            throw new ValueNotFoundException("Group chat does not exist");
        }
    }

    private void ensureUserNotInGroupChat(GroupChat group, User user) throws UserAlreadyInGroupException {
        if (group.getUsers().contains(user)) {
            throw new UserAlreadyInGroupException(user.username() + " is already in this group");
        }
    }

    private void ensureNoGroupChatForUser(String name, User user) throws UserAlreadyInGroupException {
        if (GroupChatRepository.getInstance()
                .contains(c -> c.getName().equals(name) && c.getUsers().contains(user))) {
            throw new UserAlreadyInGroupException("You are already in chat named " + name);
        }
    }

    private void ensureUserIsAdmin(User user, GroupChat group) throws AccessDeniedException {
        if (!group.getAdmin().equals(user)) {
            throw new AccessDeniedException("You do not have permissions to delete " + group.getName());
        }
    }

}
