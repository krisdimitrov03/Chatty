package bg.sofia.uni.fmi.mjt.chatty.server.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Guard {

    static <T> void isNotNull(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Null argument");
        }
    }

    static void isValidName(String name) {
        String pattern = "^[a-zA-Z-]+$";
        String errorMessage = "Name must contain only alphabetic and dash";

        validateWithPattern(pattern, name, errorMessage);
    }

    static void isValidUsername(String username) {
        String pattern = "^[\\w-.]+$";
        String errorMessage = "Username must contain only alphanumeric, underscore, dash and dot";

        validateWithPattern(pattern, username, errorMessage);
    }

    static void isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
        String errorMessage = "Password must be at least 8 characters, 1 upper, 1 lower and 1 digit";

        validateWithPattern(pattern, password, errorMessage);
    }

    private static void validateWithPattern(String patternString, String input, String errorMessage) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher match = pattern.matcher(input);

        if (!match.find()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
