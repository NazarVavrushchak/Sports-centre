package sports.center.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import sports.center.com.service.TokenBlacklistService;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Set<String> blacklistedTokens = new HashSet<>();

    public void blacklistToken(String token) {
        String transactionId = MDC.get("transactionId");
        if (token == null || token.isEmpty()) {
            log.warn("Transaction [{}] - Attempt to blacklist null or empty token", transactionId);
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        blacklistedTokens.add(token);
        log.info("Transaction [{}] - Token added to blacklist: {}", transactionId, token);
    }

    public boolean isTokenBlacklisted(String token) {
        String transactionId = MDC.get("transactionId");
        boolean isBlacklisted = token != null && blacklistedTokens.contains(token);
        log.debug("Transaction [{}] - Checking if token is blacklisted: {}, result: {}",
                transactionId, token, isBlacklisted);
        return isBlacklisted;
    }
}