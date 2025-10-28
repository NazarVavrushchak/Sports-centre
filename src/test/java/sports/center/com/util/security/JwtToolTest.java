package sports.center.com.util.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.security.JwtTool;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtToolTest {
    @InjectMocks
    private JwtTool jwtTool;

    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
    private static final String WRONG_SECRET = Base64.getEncoder().encodeToString(
            Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
    private static final long TEST_EXPIRATION = 3600 * 1000;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field secretField = JwtTool.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtTool, TEST_SECRET);
        secretField.setAccessible(false);

        Field expirationField = JwtTool.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtTool, TEST_EXPIRATION);
        expirationField.setAccessible(false);
    }

    @Test
    void generateToken_Success_ShouldGenerateValidToken() {
        String username = "testuser";
        String token = jwtTool.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedUsername = Jwts.parser()
                .setSigningKey(TEST_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertEquals(username, extractedUsername);

        Date issuedAt = Jwts.parser()
                .setSigningKey(TEST_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getIssuedAt();
        assertNotNull(issuedAt);

        Date expiration = Jwts.parser()
                .setSigningKey(TEST_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void getUsernameFromToken_ValidToken_ShouldReturnUsername() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, TEST_SECRET)
                .compact();

        String extractedUsername = jwtTool.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, TEST_SECRET)
                .compact();

        boolean isValid = jwtTool.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidSignature_ShouldReturnFalse() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, WRONG_SECRET)
                .compact();

        boolean isValid = jwtTool.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2 * TEST_EXPIRATION))
                .setExpiration(new Date(System.currentTimeMillis() - TEST_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, TEST_SECRET)
                .compact();

        boolean isValid = jwtTool.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ShouldReturnFalse() {
        String malformedToken = "invalid.token.here";

        boolean isValid = jwtTool.validateToken(malformedToken);

        assertFalse(isValid);
    }
}