package bg.sofia.uni.fmi.mjt.chatty.server.exception;

public class UserAlreadyInGroupException extends Exception {

    public UserAlreadyInGroupException() {
    }

    public UserAlreadyInGroupException(String message) {
        super(message);
    }

    public UserAlreadyInGroupException(String message, Throwable cause) {
        super(message, cause);
    }

}
