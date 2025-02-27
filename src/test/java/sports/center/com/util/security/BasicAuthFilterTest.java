package sports.center.com.util.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.security.BasicAuthFilter;
import sports.center.com.service.AuthService;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicAuthFilterTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private BasicAuthFilter basicAuthFilter;

    @BeforeEach
    void setUp() {
        basicAuthFilter = new BasicAuthFilter(authService);
        when(request.getRequestURI()).thenReturn("/Sports-Centre/trainee");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void shouldAllowPostRequestsToPublicEndpoints() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authService);
    }

    @Test
    void shouldAllowGetRequestsToTrainingTypes() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/Sports-Centre/training/training-types");
        when(request.getMethod()).thenReturn("GET");

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authService);
    }

    @Test
    void shouldRejectRequestWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verifyNoInteractions(authService);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldRejectRequestWhenAuthorizationHeaderIsInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verifyNoInteractions(authService);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldRejectRequestWhenCredentialsAreInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdHVzZXI6dGVzdHBhc3M="); // Base64("testuser:testpass")
        when(authService.authenticateTrainee("testuser", "testpass")).thenReturn(false);

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldAllowRequestWhenCredentialsAreValid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdHVzZXI6dGVzdHBhc3M="); // Base64("testuser:testpass")
        when(authService.authenticateTrainee("testuser", "testpass")).thenReturn(true);

        basicAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}