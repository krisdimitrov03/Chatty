package bg.sofia.uni.fmi.mjt.chatty.server.command;

import bg.sofia.uni.fmi.mjt.chatty.server.validation.Guard;

import java.util.Arrays;

public enum CommandType {

    REGISTER("register"),
    LOGIN("login"),
    LOGOUT("logout"),
    ADD_FRIEND("add-friend"),
    REMOVE_FRIEND("remove-friend"),
    CHECK_REQUESTS("register"),
    ACCEPT_REQUEST("accept"),
    DECLINE_REQUEST("decline"),
    LIST_FRIENDS("list-friends"),
    BLOCK("block"),
    UNBLOCK("unblock"),
    OPEN_CHAT("open-chat"),
    CLOSE_CHAT("close-chat"),
    SEND_MESSAGE("send"),
    CREATE_GROUP("create-group"),
    DELETE_GROUP("delete-group"),
    ADD_TO_GROUP("add-to-group"),
    REMOVE_FROM_GROUP("remove-from-group"),
    OPEN_GROUP("open-group"),
    CHECK_INBOX("check-inbox"),
    HELP("help"),
    UNKNOWN("unknown");

    private final String value;

    private CommandType(String value) {
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
