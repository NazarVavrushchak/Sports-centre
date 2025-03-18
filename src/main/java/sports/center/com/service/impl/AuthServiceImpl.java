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

        if (userService.isAccountLocked(username) && !userService.unlockWhenTimeExpired(username)) {
            log.warn("Login attempt blocked for locked account: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Account is locked"));
        }

        try {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                log.warn("User not found for username: {}", username);
                bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, "Invalid username or password"));
            }

            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.warn("Invalid password for username: {}", username);
                bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, "Invalid username or password"));
            }

            String token = jwtTool.generateToken(username);
            bruteForceLoginProtection.registerSuccessfulLogin(username, ipAddress);
            log.info("JWT generated for username: {}", username);
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (Exception e) {
            log.warn("Authentication failed for username: {}", username, e);
            bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Authentication failed: " + e.getMessage()));
        }
    }
}