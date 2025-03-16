package sports.center.com.util.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import sports.center.com.dto.security.AuthResponse;
import sports.center.com.security.BruteForceLoginProtection;
import sports.center.com.security.CustomUserDetailsService;
import sports.center.com.security.JwtTool;
import sports.center.com.service.UserService;
import sports.center.com.service.impl.AuthServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private BruteForceLoginProtection bruteForceLoginProtection;

    @Mock
    private JwtTool jwtTool;

    @Mock
    private UserService userService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private String username;
    private String password;
    private String ipAddress;

    @BeforeEach
    void setUp() {
        username = "testUser";
        password = "testPass";
        ipAddress = "127.0.0.1";
    }

    @Test
    void authenticateAndGenerateToken_Success_ShouldReturnToken() {
        String token = "jwt-token";
        UserDetails userDetails = new User(username, "hashedPassword", java.util.Collections.emptyList());
        when(userService.isAccountLocked(username)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
        when(jwtTool.generateToken(username)).thenReturn(token);

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        verify(userService).isAccountLocked(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(jwtTool).generateToken(username);
        verify(bruteForceLoginProtection).registerSuccessfulLogin(username, ipAddress);
        verifyNoMoreInteractions(userService, userDetailsService, passwordEncoder, jwtTool, bruteForceLoginProtection);
    }

    @Test
    void authenticateAndGenerateToken_AccountLocked_ShouldReturnUnauthorized() {
        when(userService.isAccountLocked(username)).thenReturn(true);
        when(userService.unlockWhenTimeExpired(username)).thenReturn(false);

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getToken());
        verify(userService).isAccountLocked(username);
        verify(userService).unlockWhenTimeExpired(username);
        verifyNoInteractions(userDetailsService, passwordEncoder, jwtTool, bruteForceLoginProtection);
    }

    @Test
    void authenticateAndGenerateToken_AccountLockedThenUnlocked_ShouldSucceed() {
        String token = "jwt-token";
        UserDetails userDetails = new User(username, "hashedPassword", java.util.Collections.emptyList());
        when(userService.isAccountLocked(username)).thenReturn(true);
        when(userService.unlockWhenTimeExpired(username)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
        when(jwtTool.generateToken(username)).thenReturn(token);

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        verify(userService).isAccountLocked(username);
        verify(userService).unlockWhenTimeExpired(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(jwtTool).generateToken(username);
        verify(bruteForceLoginProtection).registerSuccessfulLogin(username, ipAddress);
        verifyNoMoreInteractions(userService, userDetailsService, passwordEncoder, jwtTool, bruteForceLoginProtection);
    }

    @Test
    void authenticateAndGenerateToken_UserNotFound_ShouldReturnUnauthorized() {
        when(userService.isAccountLocked(username)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getToken());
        verify(userService).isAccountLocked(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(bruteForceLoginProtection).registerFailedAttempt(username, ipAddress);
        verifyNoInteractions(passwordEncoder, jwtTool);
    }

    @Test
    void authenticateAndGenerateToken_InvalidPassword_ShouldReturnUnauthorized() {
        UserDetails userDetails = new User(username, "hashedPassword", java.util.Collections.emptyList());
        when(userService.isAccountLocked(username)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(false);

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getToken());
        verify(userService).isAccountLocked(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(bruteForceLoginProtection).registerFailedAttempt(username, ipAddress);
        verifyNoInteractions(jwtTool);
    }

    @Test
    void authenticateAndGenerateToken_JwtGenerationFails_ShouldReturnUnauthorized() {
        UserDetails userDetails = new User(username, "hashedPassword", java.util.Collections.emptyList());
        when(userService.isAccountLocked(username)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
        when(jwtTool.generateToken(username)).thenThrow(new RuntimeException("JWT generation failed"));

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getToken());

        verify(userService).isAccountLocked(username);
        verify(userDetailsService).loadUserByUsername(username);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(jwtTool).generateToken(username);
        verify(bruteForceLoginProtection).registerFailedAttempt(username, ipAddress);
        verifyNoMoreInteractions(userService, userDetailsService, passwordEncoder, jwtTool, bruteForceLoginProtection);
    }

    @Test
    void authenticateAndGenerateToken_ThreeFailedAttempts_ShouldLockAccount() {
        UserDetails userDetails = new User(username, "hashedPassword", java.util.Collections.emptyList());
        when(userService.isAccountLocked(username)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(false);

        for (int i = 0; i < 3; i++) {
            ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody().getToken());
        }

        when(userService.isAccountLocked(username)).thenReturn(true);
        when(userService.unlockWhenTimeExpired(username)).thenReturn(false);

        ResponseEntity<AuthResponse> response = authService.authenticateAndGenerateToken(username, password, ipAddress);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody().getToken());

        verify(userService, times(4)).isAccountLocked(username);
        verify(userDetailsService, times(3)).loadUserByUsername(username);
        verify(passwordEncoder, times(3)).matches(password, "hashedPassword");
        verify(bruteForceLoginProtection, times(3)).registerFailedAttempt(username, ipAddress);
        verify(userService).unlockWhenTimeExpired(username);
        verifyNoInteractions(jwtTool);
    }
}
