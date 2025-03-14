package sports.center.com.service;

import sports.center.com.model.User;

public interface UserService {
    void increaseFailedAttempts(String username);

    void resetFailedAttempts(String username);

    void lock(String username);

    boolean isAccountLocked(String username);

    boolean unlockWhenTimeExpired(String username);

    User initializeNewUser(User user);
}
