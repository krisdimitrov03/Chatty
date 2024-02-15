package bg.sofia.uni.fmi.mjt.chatty.server.exception;

public class UserBlockedException extends Exception {

    public UserBlockedException() {
    }

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException(String message, Throwable cause) {
        super(message, cause);
    }

}
