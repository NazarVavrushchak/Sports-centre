package sports.center.com.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sports.center.com.model.User;
import sports.center.com.storage.InMemoryStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private UserDao userDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        userDao = new UserDao(storage);
    }

    @Test
    void createUser() {
        User user = new User("John", "Doe", "john.doe", "password123", true);
        userDao.create(user);

        Optional<User> retrievedUser = userDao.findById(user.getId());
        assertTrue(retrievedUser.isPresent(), "User should be created and found");
        assertEquals("john.doe", retrievedUser.get().getUsername(), "Username should match");
    }

    @Test
    void findById() {
        User user = new User("Jane", "Smith", "jane.smith", "securePass", true);
        userDao.create(user);

        Optional<User> foundUser = userDao.findById(user.getId());
        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals("Jane", foundUser.get().getFirstName(), "First name should match");
    }

    @Test
    void updateUser() {
        User user = new User("Nazar", "Vavrushchak", "nazar.vavrushchak", "oldPass", true);
        userDao.create(user);

        user.setUsername("nazar.vavrushchak1");
        user.setPassword("newPass");
        userDao.update(user.getId(), user);

        Optional<User> updatedUser = userDao.findById(user.getId());
        assertTrue(updatedUser.isPresent(), "Updated user should be found");
        assertEquals("nazar.vavrushchak1", updatedUser.get().getUsername(), "Username should be updated");
        assertEquals("newPass", updatedUser.get().getPassword(), "Password should be updated");
    }

    @Test
    void deleteUser() {
        User user = new User("Stepan", "Giga", "stepan.giga", "pass123", true);
        userDao.create(user);

        userDao.delete(user.getId());

        Optional<User> deletedUser = userDao.findById(user.getId());
        assertFalse(deletedUser.isPresent(), "Deleted user should not be found");
    }

    @Test
    void findAllUsers() {
        User user1 = new User("Charlie", "Green", "charlie.green", "pass1", true);
        User user2 = new User("Diana", "Black", "diana.black", "pass2", true);
        userDao.create(user1);
        userDao.create(user2);

        List<User> users = userDao.findAll();
        assertEquals(2, users.size(), "There should be 2 users in the list");
    }
}