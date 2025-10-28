package sports.center.com.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sports.center.com.security.JwtTool;
import sports.center.com.service.LogoutService;
import sports.center.com.service.TokenBlacklistService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTool jwtTool;

    @Override
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String transactionId = MDC.get("transactionId");
        String token = extractTokenFromRequest(request);
        log.debug("Transaction [{}] - Received logout request with token: {}", transactionId, token);

        if (token == null) {
            log.warn("Transaction [{}] - Logout attempt with no token provided", transactionId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"No token provided\"}");
        }

        if (!jwtTool.validateToken(token)) {
            log.warn("Transaction [{}] - Logout attempt with invalid or expired token: {}", transactionId, token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid or expired token\"}");
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("Transaction [{}] - Logout attempt with already blacklisted token: {}", transactionId, token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Token already invalidated\"}");
        }

        tokenBlacklistService.blacklistToken(token);
        log.info("Transaction [{}] - Logout successful for token: {}", transactionId, token);
        return ResponseEntity.ok("{\"message\": \"Logout successful\"}");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}