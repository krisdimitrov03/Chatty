package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.exception.ValueNotFoundException;
import bg.sofia.uni.fmi.mjt.chatty.server.model.User;
import bg.sofia.uni.fmi.mjt.chatty.server.security.SHA256;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {

    private static Repository<User> repo;

    @BeforeAll
    static void setupTests() {
        repo = UserRepository.getInstance(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testAddWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> repo.add(null),
                "Add should throw if null argument is passed");
    }

    @Test
    void testAddWithCorrectValue() {
        User user = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(user);

        assertIterableEquals(Set.of(user), repo.get(u -> u.equals(user)),
                "Add should create new entity in the repo if argument is correct");
    }

    @Test
    void testGetAllForCorrectResult() {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        User steven = new User("Steven", "Johnson", "s.jonson",
                SHA256.hashPassword("Password123"));

        User patrick = new User("Patrick", "Robertson", "p.robertson",
                SHA256.hashPassword("Password123"));

        repo.add(george);
        repo.add(steven);
        repo.add(patrick);

        assertEquals(Set.of(george, steven, patrick), new HashSet<>(repo.getAll()),
                "Get all should return all entities correctly");
    }

    @Test
    void testGetAllForUnmodifiableCollection() {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        assertThrows(UnsupportedOperationException.class, () -> repo.getAll().remove(george),
                "Get all should return unmodifiable collection");
    }

    @Test
    void testGetWithNullCriteria() {
        assertThrows(IllegalArgumentException.class, () -> repo.get(null),
                "Get with null criteria should throw");
    }

    @Test
    void testGetWithCorrectCriteria() {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        assertEquals(Set.of(george), repo.get(u -> u.equals(george)),
                "Get with correct criteria should return correct result");
    }

    @Test
    void testContainsWithNullCriteria() {
        assertThrows(IllegalArgumentException.class, () -> repo.contains((Predicate<User>) null),
                "Contains with null criteria should throw");
    }

    @Test
    void testContainsWithCorrectCriteria() {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        assertTrue(repo.contains(u -> u.equals(george)),
                "Contains with correct criteria should return correct result");
    }

    @Test
    void testContainsWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> repo.contains((User) null),
                "Contains with null value should throw");
    }

    @Test
    void testContainsWithCorrectValue() {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        assertTrue(repo.contains(george),
                "Contains with correct value should return correct result");
    }

    @Test
    void testRemoveWithNullCriteria() {
        assertThrows(IllegalArgumentException.class, () -> repo.remove((Predicate<User>) null),
                "Remove with null criteria should throw");
    }

    @Test
    void testRemoveWithCorrectCriteria() throws ValueNotFoundException {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        repo.remove(u -> u.equals(george));

        assertFalse(repo.contains(george),
                "Remove with correct criteria should return correct result");
    }

    @Test
    void testRemoveWithNotExistingValue() {
        assertThrows(ValueNotFoundException.class, () -> repo.remove(new User("", "", "", "")),
                "Remove with not existing value should throw");
    }

    @Test
    void testRemoveWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> repo.remove((User) null),
                "Remove with null value should throw");
    }

    @Test
    void testRemoveWithCorrectValue() throws ValueNotFoundException {
        User george = new User("George", "Peterson", "g.peterson",
                SHA256.hashPassword("Password123"));

        repo.add(george);

        repo.remove(george);

        assertFalse(repo.contains(george),
                "Remove with correct value should return correct result");
    }

}
