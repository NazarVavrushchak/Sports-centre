package sports.center.com.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import sports.center.com.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class BruteForceLoginProtection extends OncePerRequestFilter {
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Map<String, IpBlockInfo> ipAttempts = new ConcurrentHashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 5 * 60 * 1000;

    private static class IpBlockInfo {
        private final int attempts;
        private final long lockTime;

        public IpBlockInfo(int attempts, long lockTime) {
            this.attempts = attempts;
            this.lockTime = lockTime;
        }

        public int getAttempts() {
            return attempts;
        }

        public long getLockTime() {
            return lockTime;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String transactionId = MDC.get("transactionId");
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        String ipAddress = wrappedRequest.getRemoteAddr();
        String username = extractUsernameFromBody(wrappedRequest);
        log.debug("Transaction [{}] - Filtering request for IP: {}, username: {}",
                transactionId, ipAddress, username);

        if (isIpBlocked(ipAddress, transactionId, response)) {
            return;
        }

        chain.doFilter(wrappedRequest, response);
    }

    private boolean isIpBlocked(String ipAddress, String transactionId, HttpServletResponse response)
            throws IOException {
        IpBlockInfo ipInfo = ipAttempts.get(ipAddress);
        if (ipInfo == null) {
            return false;
        }

        int ipFailedAttempts = ipInfo.getAttempts();
        long lockTime = ipInfo.getLockTime();
        long currentTime = System.currentTimeMillis();

        if (ipFailedAttempts < MAX_FAILED_ATTEMPTS) {
            return false;
        }

        if (isLockExpired(lockTime, currentTime)) {
            log.info("Transaction [{}] - Unlocking IP: {} after timeout", transactionId, ipAddress);
            ipAttempts.remove(ipAddress);
            return false;
        }

        log.warn("Transaction [{}] - IP {} blocked due to excessive login attempts", transactionId, ipAddress);
        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "IP blocked due to excessive login attempts", ipInfo.getLockTime());
        return true;
    }

    private boolean isLockExpired(long lockTime, long currentTime) {
        return lockTime + LOCK_TIME_DURATION < currentTime;
    }

    public void registerFailedAttempt(String username, String ipAddress) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction [{}] - Registering failed login attempt for username: {} from IP: {}",
                transactionId, username, ipAddress);

        updateIpAttempts(ipAddress);
        handleUserFailedAttempt(username, ipAddress, transactionId);
    }

    private void updateIpAttempts(String ipAddress) {
        ipAttempts.compute(ipAddress, (key, oldValue) -> {
            int newAttempts = oldValue != null ? oldValue.getAttempts() + 1 : 1;
            long lockTime = oldValue != null && oldValue.getAttempts() >= MAX_FAILED_ATTEMPTS
                    ? oldValue.getLockTime()
                    : System.currentTimeMillis();
            return new IpBlockInfo(newAttempts, lockTime);
        });
    }

    private void handleUserFailedAttempt(String username, String ipAddress, String transactionId) {
        if (username == null) {
            return;
        }

        userService.increaseFailedAttempts(username);
        int attempts = ipAttempts.getOrDefault(ipAddress, new IpBlockInfo(0, 0)).getAttempts();
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userService.lock(username);
            log.info("Transaction [{}] - Locked user: {} due to excessive attempts from IP: {}",
                    transactionId, username, ipAddress);
        }
    }

    public void registerSuccessfulLogin(String username, String ipAddress) {
        String transactionId = MDC.get("transactionId");
        log.info("Transaction [{}] - Resetting failed attempts for username: {} from IP: {}",
                transactionId, username, ipAddress);
        ipAttempts.remove(ipAddress);
        if (username != null) {
            userService.resetFailedAttempts(username);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message, long lockTime)
            throws IOException {
        String transactionId = MDC.get("transactionId");
        long currentTime = System.currentTimeMillis();
        long remainingTime = (lockTime + LOCK_TIME_DURATION - currentTime) / 1000;
        String detailedMessage = String.format("%s. Please try again in %d minutes %d seconds",
                message, remainingTime / 60, remainingTime % 60);
        response.setContentType("application/json");
        response.setStatus(status);
        String jsonResponse = String.format("{\"error\": \"%s\"}", detailedMessage);
        response.getWriter().write(jsonResponse);
        log.info("Transaction [{}] - Sent error response: {}", transactionId, detailedMessage);
    }

    private String extractUsernameFromBody(ContentCachingRequestWrapper request) {
        String transactionId = MDC.get("transactionId");

        if (!isLoginRequest(request)) {
            return null;
        }

        try {
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            return extractUsernameFromJsonBody(body, transactionId);
        } catch (IOException e) {
            log.warn("Transaction [{}] - Failed to extract username from request body: {}",
                    transactionId, e.getMessage());
            return null;
        }
    }

    private boolean isLoginRequest(ContentCachingRequestWrapper request) {
        return request.getContentLength() > 0
                && "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/login");
    }

    private String extractUsernameFromJsonBody(String body, String transactionId) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(body);
        String username = jsonNode.has("username") ? jsonNode.get("username").asText() : null;
        log.debug("Transaction [{}] - Extracted username from body: {}", transactionId, username);
        return username;
    }
}