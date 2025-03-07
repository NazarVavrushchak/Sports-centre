package sports.center.com.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import sports.center.com.service.AuthService;

@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        if ((requestURI.equals("/trainee") || requestURI.equals("/trainer"))
                && request.getMethod().equalsIgnoreCase("POST")) {
            return true;
        }

        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/webjars/") || requestURI.equals("/actuator/health")
                || requestURI.equals("/actuator/prometheus") || requestURI.equals("/health")) {
            return true;
        }


        if (!authService.authenticateRequest(request)) {
            log.warn("Unauthorized access attempt to {}", requestURI);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            return false;
        }
        return true;
    }
}