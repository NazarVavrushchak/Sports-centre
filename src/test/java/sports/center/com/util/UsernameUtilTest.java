package sports.center.com.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UsernameUtilTest {
    @Mock
    private GenericDao<User> userDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void generateUser_noExistingUsers() {
        String firstname = "Jane";
        String lastName = "Smith";
        when(userDao.findAll()).thenReturn(List.of());

        String username = UsernameUtil.generateUsername(firstname, lastName, userDao);
        assertEquals("jane.smith", username);
        verify(userDao, times(1)).findAll();
    }

    @Test
    void generateUsername_existingUsersWithoutIndex() {
        String firstName = "Jane";
        String lastName = "Smith";
        User existingUser = mock(User.class);
        when(existingUser.getUsername()).thenReturn("jane.smith");
        when(userDao.findAll()).thenReturn(List.of(existingUser));

        String username = UsernameUtil.generateUsername(firstName, lastName, userDao);

        assertEquals("jane.smith1", username);
        verify(userDao, times(1)).findAll();
    }

    @Test
    void generateUsername_existingUsersWithIndexes() {
        String firstName = "Jane";
        String lastName = "Smith";
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        when(user1.getUsername()).thenReturn("jane.smith");
        when(user2.getUsername()).thenReturn("jane.smith1");
        when(user3.getUsername()).thenReturn("jane.smith2");

        when(userDao.findAll()).thenReturn(List.of(user1, user2, user3));

        String username = UsernameUtil.generateUsername(firstName, lastName, userDao);

        assertEquals("jane.smith3", username);
        verify(userDao, times(1)).findAll();
    }

    @Test
    void generateUsername_nullFirstNameOrLastName_throwsException() {
        String firstName = null;
        String lastName = "Smith";

        assertThrows(IllegalArgumentException.class, () ->
                UsernameUtil.generateUsername(firstName, lastName, userDao));
    }

    @Test
    void deleted_username() {
        String firstName = "Jane";
        String lastName = "Smith";
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        when(user1.getUsername()).thenReturn("jane.smith");
        when(user2.getUsername()).thenReturn("jane.smith1");
        when(user3.getUsername()).thenReturn("jane.smith2");

        when(userDao.findAll()).thenReturn(List.of(user1, user3));

        String username = UsernameUtil.generateUsername(firstName, lastName, userDao);

        assertEquals("jane.smith3", username);
        verify(userDao, times(1)).findAll();
    }
}