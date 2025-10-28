package sports.center.com.util.service_impl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sports.center.com.security.JwtTool;
import sports.center.com.service.TokenBlacklistService;
import sports.center.com.service.impl.LogoutServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtTool jwtTool;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    private String transactionId;

    @BeforeEach
    void setUp() {
        transactionId = "test-transaction-123";
        MDC.put("transactionId", transactionId);
    }

    @Test
    void logout_Success_ShouldReturnSuccessMessage() {
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        ResponseEntity<String> response = logoutService.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"message\": \"Logout successful\"}", response.getBody());
        verify(request).getHeader("Authorization");
        verify(jwtTool).validateToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(tokenBlacklistService).blacklistToken(token);
        verifyNoMoreInteractions(request, jwtTool, tokenBlacklistService);
    }

    @Test
    void logout_NoToken_ShouldReturnBadRequest() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<String> response = logoutService.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"error\": \"No token provided\"}", response.getBody());
        verify(request).getHeader("Authorization");
        verifyNoInteractions(jwtTool, tokenBlacklistService);
    }

    @Test
    void logout_InvalidTokenFormat_ShouldReturnBadRequest() {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        ResponseEntity<String> response = logoutService.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"error\": \"No token provided\"}", response.getBody());
        verify(request).getHeader("Authorization");
        verifyNoInteractions(jwtTool, tokenBlacklistService);
    }

    @Test
    void logout_InvalidToken_ShouldReturnUnauthorized() {
        String token = "invalid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(false);

        ResponseEntity<String> response = logoutService.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"error\": \"Invalid or expired token\"}", response.getBody());
        verify(request).getHeader("Authorization");
        verify(jwtTool).validateToken(token);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void logout_BlacklistedToken_ShouldReturnUnauthorized() {
        String token = "blacklisted-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        ResponseEntity<String> response = logoutService.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{\"error\": \"Token already invalidated\"}", response.getBody());
        verify(request).getHeader("Authorization");
        verify(jwtTool).validateToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verifyNoMoreInteractions(request, jwtTool, tokenBlacklistService);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }
}
