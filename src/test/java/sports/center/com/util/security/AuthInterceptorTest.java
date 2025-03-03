package sports.center.com.util.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sports.center.com.security.AuthInterceptor;
import sports.center.com.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthInterceptorTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authInterceptor = new AuthInterceptor(authService);
    }

    @Test
    void preHandle_ShouldAllowPostRequestsToCertainEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/Sports-Centre/trainee");
        when(request.getMethod()).thenReturn("POST");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result, "POST request to /Sports-Centre/trainee should be allowed");
    }

    @Test
    void preHandle_ShouldDenyAccessIfAuthenticationFails() throws Exception {
        when(request.getRequestURI()).thenReturn("/Sports-Centre/protected");
        when(authService.authenticateRequest(request)).thenReturn(false);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result, "Unauthorized request should be denied");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
    }

    @Test
    void preHandle_ShouldAllowAccessIfAuthenticationSucceeds() throws Exception {
        when(request.getRequestURI()).thenReturn("/Sports-Centre/protected");
        when(authService.authenticateRequest(request)).thenReturn(true);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result, "Authenticated request should be allowed");
    }
}

