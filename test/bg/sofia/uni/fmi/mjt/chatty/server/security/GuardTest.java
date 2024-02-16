package bg.sofia.uni.fmi.mjt.chatty.server.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuardTest {

    @Test
    void testIsNotNullWithNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> Guard.isNotNull(null),
                "IsNotNull should throw for null argument");
    }

    @Test
    void testIsValidNameWithInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> Guard.isValidName("name213"),
                "IsValidName should throw for incorrect name");
    }

    @Test
    void testIsValidNameWithValidName() {
        assertDoesNotThrow(() -> Guard.isValidName("George-Peterson"),
                "IsValidName should not throw for correct name");
    }

    @Test
    void testIsValidUsernameWithInvalidUsername() {
        assertThrows(IllegalArgumentException.class, () -> Guard.isValidUsername("Username@123"),
                "IsValidUsername should throw for incorrect username");
    }

    @Test
    void testIsValidUsernameWithValidUsername() {
        assertDoesNotThrow(() -> Guard.isValidUsername("George.PTR"),
                "IsValidUsername should not throw for correct username");
    }

    @Test
    void testIsValidPasswordWithInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> Guard.isValidPassword("prl"),
                "IsValidPassword should throw for incorrect password");
    }

    @Test
    void testIsValidPasswordWithValidPassword() {
        assertDoesNotThrow(() -> Guard.isValidPassword("Password123"),
                "IsValidPassword should not throw for correct password");
    }

}
