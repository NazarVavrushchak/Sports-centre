package sports.center.com.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTool {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String username) {
        String transactionId = MDC.get("transactionId");
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        log.info("Transaction [{}] - Generated token for username: {}", transactionId, username);
        return token;
    }

    public String getUsernameFromToken(String token) {
        String transactionId = MDC.get("transactionId");
        String username = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        log.debug("Transaction [{}] - Extracted username {} from token", transactionId, username);
        return username;
    }

    public boolean validateToken(String token) {
        String transactionId = MDC.get("transactionId");
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            log.debug("Transaction [{}] - Token validated successfully: {}", transactionId, token);
            return true;
        } catch (Exception e) {
            log.warn("Transaction [{}] - Token validation failed: {}", transactionId, token);
            return false;
        }
    }
}
