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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import sports.center.com.security.CustomUserDetailsService;
import sports.center.com.security.JwtAuthenticationFilter;
import sports.center.com.security.JwtTool;
import sports.center.com.service.TokenBlacklistService;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTool jwtTool;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValidAndNotBlacklisted() throws ServletException, IOException {
        String token = "valid-token";
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtTool.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldBlockRequestWhenTokenIsBlacklisted() throws ServletException, IOException {
        String token = "blacklisted-token";

        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, responseMock, filterChain);

        verify(responseMock).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated due to logout");
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }


    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid-token";

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtTool.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenNoTokenProvided() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}