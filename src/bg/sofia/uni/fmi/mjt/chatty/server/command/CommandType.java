package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.server.security.Guard;

import java.util.Arrays;

public enum CommandType {

    REGISTER("register"),
    LOGIN("login"),
    LOGOUT("logout"),
    ADD_FRIEND("add-friend"),
    REMOVE_FRIEND("remove-friend"),
    CHECK_REQUESTS("check-requests"),
    ACCEPT_REQUEST("accept"),
    DECLINE_REQUEST("decline"),
    LIST_FRIENDS("list-friends"),
    BLOCK("block"),
    UNBLOCK("unblock"),
    LIST_BLOCKED("list-blocked"),
    OPEN_CHAT("open-chat"),
    CLOSE_CHAT("close-chat"),
    SEND_MESSAGE("send"),
    CREATE_GROUP("create-group"),
    DELETE_GROUP("delete-group"),
    ADD_TO_GROUP("add-to-group"),
    REMOVE_FROM_GROUP("remove-from-group"),
    LEAVE_GROUP("leave"),
    OPEN_GROUP("open-group"),
    LIST_GROUPS("list-groups"),
    CHECK_INBOX("check-inbox"),
    HELP("help"),
    UNKNOWN("unknown");

    private final String value;

    CommandType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static CommandType of(String value) {
        Guard.isNotNull(value);

        return Arrays.stream(values())
                .filter(v -> v.toString().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
