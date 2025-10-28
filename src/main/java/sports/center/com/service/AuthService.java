package sports.center.com.service;

import org.springframework.http.ResponseEntity;
import sports.center.com.dto.security.AuthResponse;

public interface AuthService {
    ResponseEntity<AuthResponse> authenticateAndGenerateToken(String username, String password, String ipAddress);
}