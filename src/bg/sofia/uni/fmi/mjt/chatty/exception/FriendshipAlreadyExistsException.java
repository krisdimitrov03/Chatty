package bg.sofia.uni.fmi.mjt.chatty.exception;

public class FriendshipAlreadyExistsException extends Exception {

    public FriendshipAlreadyExistsException() {
    }

    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }

    public FriendshipAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
