package sports.center.com.util.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.model.User;
import sports.center.com.repository.UserRepository;
import sports.center.com.service.impl.UserServiceImpl;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("username");
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
    }

    @Test
    void increaseFailedAttempts_UserExists_ShouldIncrement() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.increaseFailedAttempts("testuser");

        assertEquals(1, user.getFailedAttempt());
        verify(userRepository).save(user);
    }

    @Test
    void increaseFailedAttempts_UserNotFound_ShouldDoNothing() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        userService.increaseFailedAttempts("testuser");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetFailedAttempts_UserExists_ShouldReset() {
        user.setFailedAttempt(3);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.resetFailedAttempts("testuser");

        assertEquals(0, user.getFailedAttempt());
        verify(userRepository).save(user);
    }

    @Test
    void resetFailedAttempts_UserNotFound_ShouldDoNothing() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        userService.resetFailedAttempts("testuser");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void lock_UserExists_ShouldLockAccount() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.lock("testuser");

        assertFalse(user.getAccountNonLocked());
        assertNotNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void isAccountLocked_UserExistsAndLocked_ShouldReturnTrue() {
        user.setAccountNonLocked(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        boolean result = userService.isAccountLocked("testuser");

        assertTrue(result);
    }

    @Test
    void isAccountLocked_UserExistsAndNotLocked_ShouldReturnFalse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        boolean result = userService.isAccountLocked("testuser");

        assertFalse(result);
    }

    @Test
    void isAccountLocked_UserNotFound_ShouldReturnFalse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        boolean result = userService.isAccountLocked("testuser");

        assertFalse(result);
    }

    @Test
    void unlockWhenTimeExpired_UserNotFound_ShouldReturnFalse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        boolean result = userService.unlockWhenTimeExpired("testuser");

        assertFalse(result);
    }

    @Test
    void unlockWhenTimeExpired_AccountAlreadyUnlocked_ShouldReturnTrue() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        boolean result = userService.unlockWhenTimeExpired("testuser");

        assertTrue(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unlockWhenTimeExpired_TimeExpired_ShouldUnlock() {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date(System.currentTimeMillis() - UserServiceImpl.LOCK_TIME_DURATION - 1000));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.unlockWhenTimeExpired("testuser");

        assertTrue(result);
        assertTrue(user.getAccountNonLocked());
        assertNull(user.getLockTime());
        assertEquals(0, user.getFailedAttempt());
        verify(userRepository).save(user);
    }

    @Test
    void initializeNewUser_ShouldInitializeFields() {
        User newUser = new User();
        newUser.setUsername("newuser");

        User result = userService.initializeNewUser(newUser);

        assertEquals(0, result.getFailedAttempt());
        assertTrue(result.getAccountNonLocked());
        assertNull(result.getLockTime());
    }
}
