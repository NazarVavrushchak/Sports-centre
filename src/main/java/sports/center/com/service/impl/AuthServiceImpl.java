package sports.center.com.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import sports.center.com.dto.security.AuthResponse;
import sports.center.com.security.BruteForceLoginProtection;
import sports.center.com.security.JwtTool;
import sports.center.com.service.AuthService;
import sports.center.com.service.UserService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final BruteForceLoginProtection bruteForceLoginProtection;
    private final JwtTool jwtTool;
    private final UserService userService;

    private final Set<String> blacklistedTokens = new HashSet<>();

    @Override
    public AuthResponse authenticateAndGenerateToken(String username, String password, String ipAddress) {
        log.info("Login attempt for username: {}", username);

        if (userService.isAccountLocked(username) && !userService.unlockWhenTimeExpired(username)) {
            log.warn("Login attempt blocked for locked account: {}", username);
            throw new AuthenticationException("Account locked due to excessive failed attempts") {
            };
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtTool.generateToken(username);
            bruteForceLoginProtection.registerSuccessfulLogin(username, ipAddress);
            log.info("JWT generated for username: {}", username);
            return new AuthResponse(token);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for username: {}", username);
            bruteForceLoginProtection.registerFailedAttempt(username, ipAddress);
            throw e;
        }
    }
}