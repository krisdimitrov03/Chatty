package bg.sofia.uni.fmi.mjt.chatty.server.service;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.SessionDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.model.dto.UserDTO;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.Repository;
import bg.sofia.uni.fmi.mjt.chatty.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static Repository<User> repo;

    private static UserServiceAPI service;

    @BeforeAll
    static void setupTests() {
        repo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes()));
        service = UserService.getInstance();
    }

    @AfterEach
    void clearRepo() {
        try {
            repo.remove(u -> true);
        } catch (ValueNotFoundException ignored) {
        }
    }

    @Test
    void testRegisterWithExistingUser() {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        repo.add(new User(firstName, lastName, username, SHA256.hashPassword(password)));

        assertThrows(UserAlreadyExistsException.class,
                () -> service.register(firstName, lastName, username, password),
                "Register should throw if user already exists");
    }

    @Test
    void testRegisterForCorrectResult() throws UserAlreadyExistsException {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));

        service.register(firstName, lastName, username, password);

        assertTrue(repo.contains(user),
                "Register should create user in case of correct input data");
    }

    @Test
    void testLoginWithNotExistingUser() {
        String username = "g.peterson";
        String password = "Password123";

        assertThrows(ValueNotFoundException.class, () -> service.login(username, password),
                "Login should throw if user does not exist");
    }

    @Test
    void testLoginForCorrectResult() throws ValueNotFoundException, UserAlreadyExistsException {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));
        service.register(firstName, lastName, username, password);

        assertEquals(new SessionDTO(new UserDTO(user.getFullName(), username), Collections.EMPTY_SET),
                service.login(username, password),
                "Login should return correct session in case of correct input data");
    }

    @Test
    void testEnsureUserExistsWithIncorrectUsername() {
        assertThrows(ValueNotFoundException.class, () -> service.ensureUserExists("invalid-username"),
                "Ensure should throw if user does not exist");
    }

    @Test
    void testEnsureUserExistsWithCorrectUsername() throws UserAlreadyExistsException, ValueNotFoundException {
        String firstName = "George";
        String lastName = "Peterson";
        String username = "g.peterson";
        String password = "Password123";

        User user = new User(firstName, lastName, username, SHA256.hashPassword(password));
        service.register(firstName, lastName, username, password);

        assertEquals(user, service.ensureUserExists(username),
                "Ensure should return the user they exist");
    }

}
