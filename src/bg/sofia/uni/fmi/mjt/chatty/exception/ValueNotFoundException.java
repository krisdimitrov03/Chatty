package bg.sofia.uni.fmi.mjt.chatty.exception;

public class ValueNotFoundException extends Exception {

    public ValueNotFoundException() {
    }

    public ValueNotFoundException(String message) {
        super(message);
    }

    public ValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
