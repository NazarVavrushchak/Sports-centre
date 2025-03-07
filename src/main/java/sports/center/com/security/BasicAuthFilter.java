package sports.center.com.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sports.center.com.service.AuthService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class BasicAuthFilter implements Filter {
    private final AuthService authService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        if ((requestURI.equals("/trainee") || requestURI.equals("/trainer"))
                && httpRequest.getMethod().equalsIgnoreCase("POST")) {
            chain.doFilter(request, response);
            return;
        }
        
        if (httpRequest.getRequestURI().startsWith("/swagger-ui") || httpRequest.getRequestURI().startsWith("/v3/api-docs")
                || httpRequest.getRequestURI().startsWith("/webjars/") || httpRequest.getRequestURI().equals("/actuator/health")
                || httpRequest.getRequestURI().equals("/actuator/prometheus") || httpRequest.getRequestURI().equals("/health")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2 || (!authService.authenticateTrainee(values[0], values[1])
                && !authService.authenticateTrainer(values[0], values[1]))) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            return;
        }

        chain.doFilter(request, response);
    }
}
