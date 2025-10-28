package sports.center.com.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sports.center.com.service.TokenBlacklistService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTool jwtTool;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String transactionId = MDC.get("transactionId");
        String token = extractTokenFromRequest(request);
        log.debug("Transaction [{}] - Processing request with token: {}", transactionId, token);

        if (token != null && jwtTool.validateToken(token)) {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Transaction [{}] - Blocked request with blacklisted token: {}", transactionId, token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated due to logout");
                return;
            }

            String username = jwtTool.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Transaction [{}] - Authentication set for username: {}", transactionId, username);
        }
        chain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String transactionId = MDC.get("transactionId");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Transaction [{}] - Extracted token from request: {}", transactionId, token);
            return token;
        }
        log.debug("Transaction [{}] - No valid Bearer token found in request", transactionId);
        return null;
    }
}