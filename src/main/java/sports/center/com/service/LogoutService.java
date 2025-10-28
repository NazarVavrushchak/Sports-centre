package sports.center.com.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface LogoutService {
    ResponseEntity<String> logout(HttpServletRequest request);
}
