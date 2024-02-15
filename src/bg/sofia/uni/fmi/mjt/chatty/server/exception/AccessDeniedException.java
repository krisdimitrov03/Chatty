package bg.sofia.uni.fmi.mjt.chatty.server.exception;

public class AccessDeniedException extends Exception {

    public AccessDeniedException() {
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
