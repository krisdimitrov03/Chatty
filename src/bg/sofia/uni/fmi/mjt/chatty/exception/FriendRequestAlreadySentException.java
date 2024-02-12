package bg.sofia.uni.fmi.mjt.chatty.exception;

public class FriendRequestAlreadySentException extends Exception {

    public FriendRequestAlreadySentException() {
    }

    public FriendRequestAlreadySentException(String message) {
        super(message);
    }

    public FriendRequestAlreadySentException(String message, Throwable cause) {
        super(message, cause);
    }

}
