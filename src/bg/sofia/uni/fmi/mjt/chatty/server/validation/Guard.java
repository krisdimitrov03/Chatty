package bg.sofia.uni.fmi.mjt.chatty.server.validation;

public interface Guard {

    static <T> void isNotNull(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Null argument");
        }
    }

    static void isValidUsername(String username) {

    }

    static void isValidPassword(String password) {

    }

}
