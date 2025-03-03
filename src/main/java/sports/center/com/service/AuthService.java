package sports.center.com.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    boolean authenticateTrainee(String username, String password);

    boolean authenticateTrainer(String username, String password);

    boolean authenticateRequest(HttpServletRequest request);
}