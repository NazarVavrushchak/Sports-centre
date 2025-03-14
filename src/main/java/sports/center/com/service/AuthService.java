package sports.center.com.service;

import sports.center.com.dto.security.AuthResponse;

public interface AuthService {
    AuthResponse authenticateAndGenerateToken(String username, String password, String ipAddress);
}