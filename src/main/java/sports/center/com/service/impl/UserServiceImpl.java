package sports.center.com.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import sports.center.com.model.User;
import sports.center.com.repository.UserRepository;
import sports.center.com.service.UserService;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    public static final long LOCK_TIME_DURATION = 5 * 60 * 1000;

    private final UserRepository userRepository;

    public void increaseFailedAttempts(String username) {
        String transactionId = MDC.get("transactionId");
        userRepository.findByUsername(username).ifPresent(user -> {
            int newFailedAttempts = user.getFailedAttempt() != null ? user.getFailedAttempt() + 1 : 1;
            user.setFailedAttempt(newFailedAttempts);
            userRepository.save(user);
            log.debug("Transaction [{}] - Increased failed attempts for {} to {}",
                    transactionId, username, newFailedAttempts);
        });
    }

    public void resetFailedAttempts(String username) {
        String transactionId = MDC.get("transactionId");
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempt(0);
            userRepository.save(user);
            log.debug("Transaction [{}] - Reset failed attempts for {}", transactionId, username);
        });
    }

    public void lock(String username) {
        String transactionId = MDC.get("transactionId");
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountNonLocked(false);
            user.setLockTime(new Date());
            userRepository.save(user);
            log.info("Transaction [{}] - Locked account for username: {}", transactionId, username);
        });
    }

    public boolean isAccountLocked(String username) {
        String transactionId = MDC.get("transactionId");
        boolean isLocked = userRepository.findByUsername(username)
                .map(user -> !user.getAccountNonLocked())
                .orElse(false);
        log.debug("Transaction [{}] - Checking if account {} is locked: {}",
                transactionId, username, isLocked);
        return isLocked;
    }

    public boolean unlockWhenTimeExpired(String username) {
        String transactionId = MDC.get("transactionId");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Transaction [{}] - User {} not found for unlocking check", transactionId, username);
            return false;
        }

        User user = userOpt.get();
        if (user.getAccountNonLocked() || user.getLockTime() == null) {
            log.debug("Transaction [{}] - Account {} is already unlocked or has no lock time",
                    transactionId, username);
            return true;
        }

        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            userRepository.save(user);
            log.info("Transaction [{}] - Unlocked account for username: {}", transactionId, username);
            return true;
        }
        log.debug("Transaction [{}] - Account {} still locked", transactionId, username);
        return false;
    }

    public User initializeNewUser(User user) {
        String transactionId = MDC.get("transactionId");
        if (user.getFailedAttempt() == null) user.setFailedAttempt(0);
        if (user.getAccountNonLocked() == null) user.setAccountNonLocked(true);
        user.setLockTime(null);
        log.debug("Transaction [{}] - Initialized new user: {}", transactionId, user.getUsername());
        return user;
    }
}
