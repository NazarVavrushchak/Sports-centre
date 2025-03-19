package sports.center.com.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sports.center.com.dto.security.AuthResponse;
import sports.center.com.security.BruteForceLoginProtection;
import sports.center.com.security.CustomUserDetailsService;
import sports.center.com.security.JwtTool;
import sports.center.com.service.AuthService;
import sports.center.com.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final BruteForceLoginProtection bruteForceLoginProtection;
    private final JwtTool jwtTool;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<AuthResponse> authenticateAndGenerateToken(String username, String password, String ipAddress) {
        log.info("Login attempt for username: {}", username);

        AuthResponse authResponse = processAuthentication(username, password, ipAddress);
        return buildResponse(authResponse);
    }

    private AuthResponse processAuthentication(String username, String password, String ipAddress) {
        if (isAccountLocked(username)) {
            log.warn("Login attempt blocked for locked account: {}", username);
            return new AuthResponse(null, "Account is locked");
        }

        try {
            UserDetails userDetails = loadUserDetails(username);
            if (userDetails == null) {
                return handleFailedAttempt(username, ipAddress, "Invalid username or password");
            }

            if (!isPasswordValid(password, userDetails)) {
                return handleFailedAttempt(username, ipAddress, "Invalid username or password");
            }

            return handleSuccessfulLogin(username, ipAddress);
        } catch (Exception e) {
            log.warn("Authentication failed for username: {}", username, e);
            bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
            return new AuthResponse(null, "Authentication failed: " + e.getMessage());
        }
    }

    private boolean isAccountLocked(String username) {
        return userService.isAccountLocked(username) && !userService.unlockWhenTimeExpired(username);
    }

    private UserDetails loadUserDetails(String username) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            log.warn("User not found for username: {}", username);
            return null;
        }
    }

    private boolean isPasswordValid(String password, UserDetails userDetails) {
        return passwordEncoder.matches(password, userDetails.getPassword());
    }

    private AuthResponse handleFailedAttempt(String username, String ipAddress, String errorMessage) {
        bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
        log.warn("Failed login attempt for username: {} - {}", username, errorMessage);
        return new AuthResponse(null, errorMessage);
    }

    private AuthResponse handleSuccessfulLogin(String username, String ipAddress) {
        String token = jwtTool.generateToken(username);
        bruteForceLoginProtection.registerSuccessfulLogin(username, ipAddress);
        log.info("JWT generated for username: {}", username);
        return new AuthResponse(token);
    }

    private ResponseEntity<AuthResponse> buildResponse(AuthResponse authResponse) {
        if (authResponse.getToken() != null) {
            return ResponseEntity.ok(authResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
    }
}